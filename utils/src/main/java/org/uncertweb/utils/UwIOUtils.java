/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
