package cn.coder.easywx.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.util.JSONUtils;

public class Base {
	static final Logger logger = LoggerFactory.getLogger(Base.class);

	protected static String getRandamStr() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	protected static long getTimestamp() {
		return new Date().getTime() / 1000;
	}

	protected static String getValue(Map<String, Object> result, String key) {
		if (result == null || key == null)
			return null;
		if (result.containsKey(key))
			return result.get(key).toString();
		return null;
	}

	protected static String getJSON(String url) {
		StringBuilder sb = new StringBuilder();
		try {
			JSONUtils.readStream(sb, new URL(url).openStream());
		} catch (IOException e) {
			logger.error("[GET]" + url + " faild", e);
		}
		return sb.toString();
	}

	protected static String postString(String url, String paras) {
		return postString(url, null, paras);
	}

	protected static String postString(String url, SSLSocketFactory ssl, String paras) {
		StringBuilder sb = new StringBuilder();
		try {
			URLConnection connection = new URL(url).openConnection();
			if (ssl != null) {
				((HttpsURLConnection) connection).setSSLSocketFactory(ssl);
			}
			connection.setDoOutput(true);

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(paras.getBytes("utf-8"));
			outputStream.flush();
			outputStream.close();

			// 将微信服务器返回的输入流转换成字符串
			JSONUtils.readStream(sb, connection.getInputStream());
		} catch (IOException e) {
			logger.error("[POST]" + url + " faild,", e);
		}
		return sb.toString();
	}
}
