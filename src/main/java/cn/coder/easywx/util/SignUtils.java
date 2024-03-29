package cn.coder.easywx.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignUtils {
	private static final Logger logger = LoggerFactory.getLogger(SignUtils.class);
	private static MessageDigest sha1MD;

	public static String decryptData(byte[] data, byte[] key) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			return new String(cipher.doFinal(data), "utf-8");
		} catch (Exception e) {
			logger.error("Decrypt data faild", e);
			return null;
		}
	}

	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	public static String encodeByMD5(String str) {
		if (str == null)
			return null;
		try {
			// 创建具有指定算法名称的信息摘要
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
			byte[] results = md.digest(str.getBytes("utf-8"));
			// 将得到的字节数组变成字符串返回
			return byteToHex(results);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String getSign(HashMap<String, Object> map, String apiKey) {
		StringBuffer sb = new StringBuffer();
		TreeMap<String, Object> sortmap = new TreeMap<>(map);
		Object obj;
		for (String key : sortmap.keySet()) {
			obj = sortmap.get(key);
			if (obj == null || "".equals(obj.toString()))
				continue;
			sb.append(key);
			sb.append("=");
			sb.append(obj);
			sb.append("&");
		}
		sb.append("key=");
		sb.append(apiKey);
		return encodeByMD5(sb.toString()).toUpperCase();
	}

	public static String SHA1(String str) {
		if (null == sha1MD) {
			try {
				sha1MD = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				return null;
			}
		}
		try {
			sha1MD.update(str.getBytes("utf-8"), 0, str.length());
		} catch (UnsupportedEncodingException e) {
			sha1MD.update(str.getBytes(), 0, str.length());
		}
		return byteToHex(sha1MD.digest());
	}

	public static SSLSocketFactory getSSLSocketFactory(String p12, String p12Pass) {
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			char[] password = p12Pass.toCharArray();
			InputStream inputStream;
			//如果文件中发现路径
			if (p12.contains("/") || p12.contains("\\"))
				inputStream = new FileInputStream(p12);
			else
				inputStream = SignUtils.class.getClassLoader().getResourceAsStream(p12);
			ks.load(inputStream, password);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, password);
			SSLContext ssl = SSLContext.getInstance("TLS");
			ssl.init(kmf.getKeyManagers(), null, null);
			return ssl.getSocketFactory();
		} catch (Exception e) {
			logger.error("加载证书失败" + p12, e);
			return null;
		}
	}

}
