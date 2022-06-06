package org.trade.utils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpUtils {
	private static final Logger log = LogManager.getLogger(HttpUtils.class);

	public static String getStringResponse(HttpRequestBase request) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);
		HttpEntity entity = response.getEntity();
		log.debug("response headers from " + request.getURI() + " = " + JsonUtils.getString(response.getAllHeaders()));
		return EntityUtils.toString(entity);
	}
}
