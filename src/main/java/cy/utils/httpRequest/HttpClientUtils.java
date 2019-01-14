package cy.utils.httpRequest;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * Http请求工具
 * 
 * @author:chenYong 2019年1月14日 下午10:41:28
 */
public class HttpClientUtils {
	private HttpClientUtils() {
	}

	private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

	/**
	 * 参数为:json/xml的请求: contentType: application/json,text/xml
	 * 
	 * @author:chenYong 2019年1月14日 下午10:58:00
	 */
	public static String sendJsonRequest(String url, String charSet, int httpTimeout, String jsonStr,
			String contentType) throws Exception {
		logger.info(" Http请求 地    址 -> {}", url);
		logger.info(" Http请求 字符集 -> {}", charSet);
		logger.info(" Http请求 限    时 -> {}", httpTimeout);
		logger.info(" Http请求 参    数 -> {}", jsonStr);
		long time = System.currentTimeMillis();
		// 此为防止是https的请求
		ProtocolSocketFactory fcty = new MySecureProtocolSocketFactory();
		Protocol.registerProtocol("https", new Protocol("https", fcty, 443));
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(httpTimeout);
		client.getHttpConnectionManager().getParams().setSoTimeout(httpTimeout);
		PostMethod method = new PostMethod(url);
		try {
			StringRequestEntity entity = new StringRequestEntity(jsonStr, contentType, charSet);// 解决中文乱码问题
			method.setRequestEntity(entity);
			method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, charSet);
			int statusCode = client.executeMethod(method);
			// HttpClient对于要求接受后继服务的请求，像POST和PUT等不能自动处理转发
			// 301或者302
			if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
				// 从头中取出转向的地址
				Header locationHeader = method.getResponseHeader("location");
				String location = null;
				if (locationHeader != null) {
					location = locationHeader.getValue();
					method = new PostMethod(location);
					method.setRequestEntity(new StringRequestEntity(jsonStr, contentType, charSet));
					client.executeMethod(method);
				} else {
					logger.error(" Http请求本地跳转失败路径为空");
				}
			}
		} catch (Exception e) {
			throw new Exception(e);
		}

		StatusLine statusLine = method.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			// 打印返回的信息
			try {
				String result = method.getResponseBodyAsString();
				// 释放连接
				method.releaseConnection();
				logger.info(" Http请求 耗        时 -> {}", (System.currentTimeMillis() - time));
				logger.info(" Http请求 应答内容 -> {}", result);
				return result;
			} catch (Exception e) {
				throw new Exception(e);
			}
		} else {
			throw new Exception("Http响应状态错误,StatusCode : " + statusLine.getStatusCode());
		}

	}

	/**
	 * http模拟表单请求
	 * 
	 * @author:chenYong 2019年1月14日 下午11:16:44
	 */
	public static String sendHttpClient(String methodUrl, NameValuePair[] params, String charSet, int httpTimeout)
			throws Exception {
		logger.info(" Http请求 地    址 -> {}", methodUrl);
		logger.info(" Http请求 字符集 -> {}", charSet);
		logger.info(" Http请求 限    时 -> {}", httpTimeout);
		logger.info(" Http请求 参    数 -> ");
		logger.info(JSONObject.toJSONString(params));
		long time = System.currentTimeMillis();
		ProtocolSocketFactory fcty = new MySecureProtocolSocketFactory();
		Protocol.registerProtocol("https", new Protocol("https", fcty, 443));
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(httpTimeout);
		client.getHttpConnectionManager().getParams().setSoTimeout(httpTimeout);
		PostMethod method = new PostMethod(methodUrl);
		try {
			method.setRequestBody(params);
			method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, charSet);
			int statusCode = client.executeMethod(method);
			// HttpClient对于要求接受后继服务的请求，像POST和PUT等不能自动处理转发
			// 301或者302
			if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
				// 从头中取出转向的地址
				Header locationHeader = method.getResponseHeader("location");
				String location = null;
				if (locationHeader != null) {
					location = locationHeader.getValue();
					method = new PostMethod(location);
					method.setRequestBody(params);
					client.executeMethod(method);
				} else {
					logger.error(" Http请求本地跳转失败路径为空");
				}
			}
		} catch (Exception e) {
			throw new Exception(e);
		}

		StatusLine statusLine = method.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			// 打印返回的信息
			try {
				String result = method.getResponseBodyAsString();
				logger.info(" Http应答 字符集 -> {}", method.getResponseCharSet());
				// 释放连接
				method.releaseConnection();
				logger.info(" Http请求 耗        时 -> {}", (System.currentTimeMillis() - time));
				logger.info(" Http请求 应答内容 -> {}", result);
				return result;
			} catch (Exception e) {
				throw new Exception(e);
			}
		} else {
			throw new Exception("Http响应状态错误,StatusCode : " + statusLine.getStatusCode());
		}

	}

	/**
	 * 含附件的请求方式
	 * 
	 * @author:chenYong 2019年1月14日 下午11:25:54
	 */
	public static String sendHttpClientMultipartFormData(String methodUrl, Map<String, Object> data, String charset,
			int httpTimeout) throws Exception {
		logger.info(" Http请求 地    址 -> {}", methodUrl);
		logger.info(" Http请求 字符集 -> {}", charset);
		logger.info(" Http请求 限    时 -> {}", httpTimeout);
		logger.info(" Http请求 参    数 -> ");
		long time = System.currentTimeMillis();
		ProtocolSocketFactory fcty = new MySecureProtocolSocketFactory();
		Protocol.registerProtocol("https", new Protocol("https", fcty, 443));
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(httpTimeout);
		client.getHttpConnectionManager().getParams().setSoTimeout(httpTimeout);
		// 使用 GET 方法 ，如果服务器需要通过 HTTPS 连接，那只需要将下面 URL 中的 http 换成 https
		PostMethod postMethod = new PostMethod(methodUrl);
		try {
			Part[] parts = new Part[data.size()];
			int index = 0;
			for (Entry<String, Object> part : data.entrySet()) {
				if (part.getValue() instanceof File) {
					parts[index] = new FilePart(part.getKey(), (File) part.getValue());
				} else if (part.getValue() instanceof String) {
					StringPart stringPart = new StringPart(part.getKey(), (String) part.getValue(), charset);
					stringPart.setTransferEncoding(null);
					stringPart.setContentType(null);
					parts[index] = stringPart;
				} else {
					logger.error("未知的报文信息：" + part.getValue());
				}
				index++;
			}
			MultipartRequestEntity mre = new MultipartRequestEntity(parts, postMethod.getParams());
			postMethod.setRequestEntity(mre);
			postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, charset);
			client.executeMethod(postMethod);
		} catch (Exception e) {
			throw new Exception(e);
		}

		StatusLine statusLine = postMethod.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			// 打印返回的信息
			try {
				String result = postMethod.getResponseBodyAsString();
				logger.info(" Http应答 字符集 -> {}", postMethod.getResponseCharSet());
				// 释放连接
				postMethod.releaseConnection();
				logger.info(" Http请求 耗        时 -> {}", (System.currentTimeMillis() - time));
				logger.info(" Http请求 应答内容 -> {}", result);
				return result;
			} catch (Exception e) {
				throw new Exception(e);
			}
		} else {
			throw new Exception("Http响应状态错误,StatusCode : " + statusLine.getStatusCode());
		}

	}
}
