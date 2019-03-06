package cn.coder.easywx.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignUtils {
	private static final Logger logger = LoggerFactory.getLogger(SignUtils.class);
	private static MessageDigest sha1MD;

	public static String decryptData(byte[] data, byte[] key) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			return new String(cipher.doFinal(data));
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

}
