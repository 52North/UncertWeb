package org.uncertweb.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

public class UwIOUtils extends UwUtils {
	public static boolean deleteRecursively(File path) {
		if (path != null && path.exists()) {
			log.debug("Deleting {}", path.getAbsolutePath());
			if (path.isDirectory()) {
				for (File f : path.listFiles()) {
					if (f.isDirectory())
						deleteRecursively(f);
					else
						f.delete();
				}
			}
			return path.delete();
		} else
			return false;
	}

	public static void saveToFile(File f, String s) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(f);
			IOUtils.write(s, os);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	public static void saveToFile(File f, InputStream is) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(f);
			IOUtils.copy(is, os);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}

	public static void saveToFile(File f, URL url) throws IOException {
		final URLConnection con = url.openConnection();
		con.setDoOutput(false);
		con.setDoInput(true);
		saveToFile(f, con.getInputStream());
	}
}
