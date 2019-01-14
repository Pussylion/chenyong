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
 * Http���󹤾�
 * 
 * @author:chenYong 2019��1��14�� ����10:41:28
 */
public class HttpClientUtils {
	private HttpClientUtils() {
	}

	private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

	/**
	 * ����Ϊ:json/xml������: contentType: application/json,text/xml
	 * 
	 * @author:chenYong 2019��1��14�� ����10:58:00
	 */
	public static String sendJsonRequest(String url, String charSet, int httpTimeout, String jsonStr,
			String contentType) throws Exception {
		logger.info(" Http���� ��    ַ -> {}", url);
		logger.info(" Http���� �ַ��� -> {}", charSet);
		logger.info(" Http���� ��    ʱ -> {}", httpTimeout);
		logger.info(" Http���� ��    �� -> {}", jsonStr);
		long time = System.currentTimeMillis();
		// ��Ϊ��ֹ��https������
		ProtocolSocketFactory fcty = new MySecureProtocolSocketFactory();
		Protocol.registerProtocol("https", new Protocol("https", fcty, 443));
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(httpTimeout);
		client.getHttpConnectionManager().getParams().setSoTimeout(httpTimeout);
		PostMethod method = new PostMethod(url);
		try {
			StringRequestEntity entity = new StringRequestEntity(jsonStr, contentType, charSet);// ���������������
			method.setRequestEntity(entity);
			method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, charSet);
			int statusCode = client.executeMethod(method);
			// HttpClient����Ҫ����ܺ�̷����������POST��PUT�Ȳ����Զ�����ת��
			// 301����302
			if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
				// ��ͷ��ȡ��ת��ĵ�ַ
				Header locationHeader = method.getResponseHeader("location");
				String location = null;
				if (locationHeader != null) {
					location = locationHeader.getValue();
					method = new PostMethod(location);
					method.setRequestEntity(new StringRequestEntity(jsonStr, contentType, charSet));
					client.executeMethod(method);
				} else {
					logger.error(" Http���󱾵���תʧ��·��Ϊ��");
				}
			}
		} catch (Exception e) {
			throw new Exception(e);
		}

		StatusLine statusLine = method.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			// ��ӡ���ص���Ϣ
			try {
				String result = method.getResponseBodyAsString();
				// �ͷ�����
				method.releaseConnection();
				logger.info(" Http���� ��        ʱ -> {}", (System.currentTimeMillis() - time));
				logger.info(" Http���� Ӧ������ -> {}", result);
				return result;
			} catch (Exception e) {
				throw new Exception(e);
			}
		} else {
			throw new Exception("Http��Ӧ״̬����,StatusCode : " + statusLine.getStatusCode());
		}

	}

	/**
	 * httpģ�������
	 * 
	 * @author:chenYong 2019��1��14�� ����11:16:44
	 */
	public static String sendHttpClient(String methodUrl, NameValuePair[] params, String charSet, int httpTimeout)
			throws Exception {
		logger.info(" Http���� ��    ַ -> {}", methodUrl);
		logger.info(" Http���� �ַ��� -> {}", charSet);
		logger.info(" Http���� ��    ʱ -> {}", httpTimeout);
		logger.info(" Http���� ��    �� -> ");
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
			// HttpClient����Ҫ����ܺ�̷����������POST��PUT�Ȳ����Զ�����ת��
			// 301����302
			if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
				// ��ͷ��ȡ��ת��ĵ�ַ
				Header locationHeader = method.getResponseHeader("location");
				String location = null;
				if (locationHeader != null) {
					location = locationHeader.getValue();
					method = new PostMethod(location);
					method.setRequestBody(params);
					client.executeMethod(method);
				} else {
					logger.error(" Http���󱾵���תʧ��·��Ϊ��");
				}
			}
		} catch (Exception e) {
			throw new Exception(e);
		}

		StatusLine statusLine = method.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			// ��ӡ���ص���Ϣ
			try {
				String result = method.getResponseBodyAsString();
				logger.info(" HttpӦ�� �ַ��� -> {}", method.getResponseCharSet());
				// �ͷ�����
				method.releaseConnection();
				logger.info(" Http���� ��        ʱ -> {}", (System.currentTimeMillis() - time));
				logger.info(" Http���� Ӧ������ -> {}", result);
				return result;
			} catch (Exception e) {
				throw new Exception(e);
			}
		} else {
			throw new Exception("Http��Ӧ״̬����,StatusCode : " + statusLine.getStatusCode());
		}

	}

	/**
	 * ������������ʽ
	 * 
	 * @author:chenYong 2019��1��14�� ����11:25:54
	 */
	public static String sendHttpClientMultipartFormData(String methodUrl, Map<String, Object> data, String charset,
			int httpTimeout) throws Exception {
		logger.info(" Http���� ��    ַ -> {}", methodUrl);
		logger.info(" Http���� �ַ��� -> {}", charset);
		logger.info(" Http���� ��    ʱ -> {}", httpTimeout);
		logger.info(" Http���� ��    �� -> ");
		long time = System.currentTimeMillis();
		ProtocolSocketFactory fcty = new MySecureProtocolSocketFactory();
		Protocol.registerProtocol("https", new Protocol("https", fcty, 443));
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(httpTimeout);
		client.getHttpConnectionManager().getParams().setSoTimeout(httpTimeout);
		// ʹ�� GET ���� �������������Ҫͨ�� HTTPS ���ӣ���ֻ��Ҫ������ URL �е� http ���� https
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
					logger.error("δ֪�ı�����Ϣ��" + part.getValue());
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
			// ��ӡ���ص���Ϣ
			try {
				String result = postMethod.getResponseBodyAsString();
				logger.info(" HttpӦ�� �ַ��� -> {}", postMethod.getResponseCharSet());
				// �ͷ�����
				postMethod.releaseConnection();
				logger.info(" Http���� ��        ʱ -> {}", (System.currentTimeMillis() - time));
				logger.info(" Http���� Ӧ������ -> {}", result);
				return result;
			} catch (Exception e) {
				throw new Exception(e);
			}
		} else {
			throw new Exception("Http��Ӧ״̬����,StatusCode : " + statusLine.getStatusCode());
		}

	}
}
