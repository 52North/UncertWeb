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
package org.uncertweb.viss.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.opengis.coverage.grid.GridCoverage;
import org.uncertml.UncertML;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;

public class Utils {


	public static File to(File dir, String sub) {
		if (dir == null || sub == null || !dir.isDirectory())
			return null;
		return new File(dir.getAbsolutePath() + File.separator + sub);
	}



	public static long saveToFileWithChecksum(File f, InputStream is)
	    throws IOException {
		CheckedOutputStream cos = null;
		try {
			cos = new CheckedOutputStream(new FileOutputStream(f), new CRC32());
			IOUtils.copy(is, cos);
			return cos.getChecksum().getValue();
		} finally {
			IOUtils.closeQuietly(cos);
		}
	}

	public static void writeAsGeoTIFF(GridCoverage coverage, String file) {
		writeAsGeoTIFF(coverage, new File(file));
	}

	public static void writeAsGeoTIFF(GridCoverage coverage, File file) {
		try {
			writeAsGeoTIFF(coverage, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw VissError.internal(e);
		}
	}

	public static void writeAsGeoTIFF(GridCoverage coverage, OutputStream out) {
		try {
			new GeoTiffWriter(out).write(coverage, null);
		} catch (IOException e) {
			throw VissError.internal(e);
		} finally {
		}
	}

	public static URI getUncertURI(Class<?> c) {
		try {
			return new URI(UncertML.getURI(c));
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public static String stringifyJson(JSONObject json) throws JSONException {
		if (VissConfig.getInstance().doPrettyPrint())
			return json.toString(4);
		else
			return json.toString();
	}

	

	/**
	 * 
	 * <pre>
	 * flatJSON(&quot;one&quot;, &quot;two&quot;, &quot;three&quot;, &quot;four&quot;)
	 * </pre>
	 * creates <pre> { "one": { "two": { "three": "four" } } } </pre>
	 * 
	 * @param s
	 * @return
	 * @throws JSONException
	 */
	public static final JSONObject flatJSON(String... s) throws JSONException {
		int l = s.length;
		if (s.length < 2)
			throw VissError.internal("Invalid parameter number");
		JSONObject j = new JSONObject().put(s[l - 2], s[l - 1]);
		for (int i = l - 3; i >= 0; --i) {
			j = new JSONObject().put(s[i], j);
		}
		return j;
	}

	
}
