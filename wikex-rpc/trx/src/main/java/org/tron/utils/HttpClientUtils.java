package org.tron.utils;

import io.netty.handler.codec.http.HttpMethod;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * HTTP clients
 *
 * @Author TrickyZh 2021/1/20
 * @Date 2021-01-20 18:46:36
 */
public class HttpClientUtils {
	private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

	/**
	 * POST with parameters (form-encoded)
	 *
	 * @param url   request URL
	 * @param param parameter map
	 * @return response body as String
	 * @throws Exception on request errors
	 */
	public static String doPost(String url, Map<String, String> param) throws Exception {
		HttpPost httpPost = new HttpPost(url);
		ArrayList<BasicNameValuePair> arrayList = new ArrayList<BasicNameValuePair>();
		Set<String> keySet = param.keySet();
		for (String key : keySet) {
			arrayList.add(new BasicNameValuePair(key, param.get(key)));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(arrayList));
		return execute(httpPost);
	}

	/**
	 * Execute HTTP request
	 *
	 * @param request request object
	 * @return response body as String
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private static String execute(HttpRequestBase request) throws IOException, ClientProtocolException {
		RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
		CloseableHttpResponse response = httpclient.execute(request);
		if (200 == response.getStatusLine().getStatusCode()) {
			return EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
		} else {
			String data = EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
			logger.debug(data);
			return data;
		}
	}

	/**
	 * POST JSON payload
	 *
	 * @param url      request URL
	 * @param jsonBody JSON body string
	 * @return response body as String
	 */
	public static String postJson(String url, String jsonBody) throws IOException {
		String result = "";
		HttpPost httpPost = new HttpPost(url);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			BasicResponseHandler handler = new BasicResponseHandler();
			// solve garbled characters for non-ASCII text
			StringEntity entity = new StringEntity(jsonBody, "utf-8");
			entity.setContentEncoding("UTF-8");
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			result = httpClient.execute(httpPost, handler);
			return result;
		} catch (Exception e) {
			logger.error(String.format("post error:url=%s body=%s", url, jsonBody == null ? "" : jsonBody), e);
		} finally {
			try {
				httpClient.close();
			} catch (Exception e) {
				logger.error(String.format("post error:url=%s body=%s", url, jsonBody == null ? "" : jsonBody), e);
			}
		}
		return result;
	}

	/**
	 * Send HTTP request (requestUrl already contains query string)
	 *
	 * @param requestUrl full request URL (with query parameters if any)
	 * @param httpMethod HTTP method
	 * @return response body as String
	 * @throws IOException
	 */
	public static String request(String requestUrl, HttpMethod httpMethod) throws IOException {
		URL url = new URL(requestUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(httpMethod.name());
		connection.setDoOutput(true);
		connection.setReadTimeout(5000);
		connection.setConnectTimeout(5000);
		connection.connect();
		InputStream in = connection.getInputStream();
		Reader reader = new InputStreamReader(in, "UTF-8");
		BufferedReader bufferedReader = new BufferedReader(reader);
		StringBuffer stringBuffer = new StringBuffer();
		String temporaryString = null;
		while ((temporaryString = bufferedReader.readLine()) != null) {
			stringBuffer.append(temporaryString);
		}
		bufferedReader.close();
		reader.close();
		in.close();
		connection.disconnect();
		return stringBuffer.toString();
	}
}
