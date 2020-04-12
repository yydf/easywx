package cn.coder.easywx.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectUtils {
	private static final Logger logger = LoggerFactory.getLogger(ObjectUtils.class);

	public static boolean writeObject(File file, Object obj) {
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file, false));
			output.writeObject(obj);
			output.close();
			return true;
		} catch (IOException e) {
			logger.error("Write object to '" + file + "' faild", e);
			return false;
		}
	}

	public static Object readObject(File file) {
		Object obj = null;
		if (file.exists()) {
			try {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
				obj = input.readObject();
				input.close();
			} catch (IOException | ClassNotFoundException e) {
				logger.error("Read object from '" + file.getAbsolutePath() + "' faild", e);
			}
		}
		return obj;
	}

	public static String readString(File file) {
		String str = null;
		if (file.exists()) {
			FileInputStream fr;
			try {
				fr = new FileInputStream(file);
				byte[] temp = new byte[fr.available()];
				fr.read(temp);
				str = new String(temp, "UTF-8");
			} catch (IOException e) {
				logger.warn("Read String faild", e);
			}
		}
		return str;
	}

	public static boolean writeString(File file, String str) {
		try {
			FileWriter fw = new FileWriter(file, false);
			fw.write(str);
			fw.close();
			return true;
		} catch (IOException e) {
			logger.warn("Write String faild", e);
			return false;
		}
	}

	public static byte[] input2byte(InputStream input) throws IOException {
		if (input == null)
			return null;
		byte[] temp = new byte[1024];
		ByteArrayOutputStream swapStream = null;
		try {
			swapStream = new ByteArrayOutputStream();
			int rc = 0;
			while ((rc = input.read(temp, 0, temp.length)) > 0) {
				swapStream.write(temp, 0, rc);
			}
			return swapStream.toByteArray();
		} catch (Exception e) {
			return null;
		} finally {
			input.close();
			if (swapStream != null)
				swapStream.close();
		}
	}

}
