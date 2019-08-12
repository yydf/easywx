package cn.coder.easywx.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.util.JSONUtils;
import cn.coder.easywx.util.XMLUtils;

public class Base {
	private static final Logger logger = LoggerFactory.getLogger(Base.class);

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

	protected static boolean valid(String json, String key) {
		if (json == null || "".equals(json))
			return false;
		return json.contains("\"" + key + "\"");
	}

	protected static boolean invalidToken(String json) {
		if (json == null || "".equals(json))
			return false;
		Long errcode = JSONUtils.getLong(json, "errcode");
		if (errcode != null)
			return errcode.equals(40001) || errcode.equals(40014) || errcode.equals(42001);
		return false;
	}

	protected static boolean getWechatResult(String url, SSLSocketFactory ssl, HashMap<String, Object> map) {
		String return_xml = postString(url, ssl, XMLUtils.toXML(map));
		logger.debug("[WECHAT]" + return_xml);
		map = XMLUtils.doXMLParse(return_xml);
		String returnCode = getValue(map, "return_code");
		String resultCode = getValue(map, "result_code");
		return "SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode);
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
				if (logger.isDebugEnabled())
					logger.debug("URL connection with ssl");
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
