package cn.coder.easywx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectUtils {
	private static final Logger logger = LoggerFactory.getLogger(ObjectUtils.class);

	public static boolean writeObject(String file, Object obj) {
		try {
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(new File(file), false));
			output.writeObject(obj);
			output.close();
			return true;
		} catch (IOException e) {
			logger.error("Write object to '" + file + "' faild", e);
			return false;
		}
	}

	public static Object readObject(String filename) {
		Object obj = null;
		File file = new File(filename);
		if (file.exists()) {
			try {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
				obj = input.readObject();
				input.close();
			} catch (IOException | ClassNotFoundException e) {
				logger.error("Read object from '" + filename + "' faild", e);
			}
		}
		return obj;
	}

}
