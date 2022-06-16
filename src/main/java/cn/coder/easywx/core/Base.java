package cn.coder.easywx.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.util.JSONUtils;
import cn.coder.easywx.util.ObjectUtils;
import cn.coder.easywx.util.XMLUtils;

public abstract class Base {
	private static final Logger logger = LoggerFactory.getLogger(Base.class);

	protected static String getRandamStr() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	protected static long getTimestamp() {
		return System.currentTimeMillis() / 1000;
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
			return errcode.equals(40001L) || errcode.equals(40014L) || errcode.equals(42001L);
		return false;
	}

	protected static boolean getWechatResult(String url, SSLSocketFactory ssl, HashMap<String, Object> map) {
		String return_xml = postString(url, ssl, XMLUtils.toXML(map));
		logger.debug("[WECHAT]" + return_xml);
		HashMap<String, Object> result = XMLUtils.doXMLParse(return_xml);
		map.putAll(result);
		String returnCode = getValue(result, "return_code");
		String resultCode = getValue(result, "result_code");
		return "SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode);
	}

	protected static String toJson(List<?> list) {
		StringBuilder sb = new StringBuilder();
		for (Object obj : list) {
			sb.append(obj.toString());
			sb.append(",");
		}
		if (sb.length() > 0)
			sb.delete(sb.length() - 1, sb.length());
		return sb.toString();
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

	protected static byte[] download(String url, String paras) {
		try {
			URLConnection connection = new URL(url).openConnection();
			connection.setDoOutput(true);

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(paras.getBytes("utf-8"));
			outputStream.flush();
			outputStream.close();

			// 将微信服务器返回的输入流转换成字符串
			return ObjectUtils.input2byte(connection.getInputStream());
		} catch (IOException e) {
			logger.error("[POST]" + url + " faild,", e);
		}
		return null;
	}

	public static String postFile(String urlStr, InputStream inputFile) {
		String res = "";
		HttpURLConnection conn = null;
		logger.debug("url:" + urlStr);
		// boundary就是request头和上传文件内容的分隔符
		String BOUNDARY = "---------------------------" + System.currentTimeMillis();
		try {
			conn = (HttpURLConnection) new URL(urlStr).openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

			// file
			if (inputFile != null) {
				OutputStream out = conn.getOutputStream();
				// 没有传入文件类型，同时根据文件获取不到类型，默认采用application/octet-stream
				StringBuffer strBuf = new StringBuffer();
				strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
				strBuf.append("Content-Disposition: form-data; name=\"media\"; filename=\"test.jpg\"\r\n");
				strBuf.append("Content-Type:image/jpg\r\n\r\n");
				out.write(strBuf.toString().getBytes());

				int bytes = 0;
				byte[] bufferOut = new byte[102400];
				while ((bytes = inputFile.read(bufferOut)) != -1) {
					out.write(bufferOut, 0, bytes);
				}
				inputFile.close();

				byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
				out.write(endData);
				out.flush();
				out.close();
			}
			res = JSONUtils.readStream(conn.getInputStream());
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.error("发送POST请求出错。" + urlStr);
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

}
