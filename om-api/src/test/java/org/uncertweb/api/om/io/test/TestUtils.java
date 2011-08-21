package org.uncertweb.api.om.io.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestUtils {
	public static String readXmlFile(String filePath) throws IOException {
		String result = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath)));
		try {
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} finally {
			in.close();
		}
		return result;
	}
}