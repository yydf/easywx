package cn.coder.easywx.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JSONUtils {

	public static String getString(String json, String key) {
		if (json == null || key == null)
			return null;
		int startIndex = json.indexOf(key);
		if (startIndex == -1)
			return null;
		int start = json.indexOf("\"", startIndex + key.length() + 2);
		int end = json.indexOf("\"", start + 1);
		return json.substring(start + 1, end);
	}

	public static Long getLong(String json, String key) {
		if (json == null || key == null)
			return null;
		int startIndex = json.indexOf(key);
		if (startIndex == -1)
			return null;
		int start = startIndex + key.length() + 1;
		int end = json.indexOf(",", start);
		if (end == -1)
			end = json.indexOf("}", start);
		return Long.parseLong(json.substring(start + 1, end));
	}

	public static void readStream(StringBuilder sb, InputStream inputStream) throws IOException {
		final char[] temp = new char[1024];
		InputStreamReader reader = new InputStreamReader(inputStream, "utf-8");
		int len;
		while ((len = reader.read(temp)) > 0) {
			sb.append(new String(temp, 0, len));
		}
		reader.close();
		// 释放资源
		inputStream.close();
	}

	public static String readStream(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		readStream(sb, inputStream);
		return sb.toString();
	}

}
