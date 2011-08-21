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
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.xmlbeans.XmlOptions;
import org.joda.time.Period;
import org.uncertml.UncertML;
import org.uncertml.distribution.continuous.NormalDistribution;

public class Constants {

	public static final URI NORMAL_DISTRIBUTION = URI.create(UncertML.getURI(NormalDistribution.class));
	public static final URI NORMAL_DISTRIBUTION_MEAN = URI.create(Utils.join("#", NORMAL_DISTRIBUTION.toString(), "mean"));
	public static final URI NORMAL_DISTRIBUTION_VARIANCE = URI.create(Utils.join("#", NORMAL_DISTRIBUTION.toString(), "variance"));

	public static final String NETCDF = "application/netcdf";
	public static final String X_NETCDF = "application/x-netcdf";
	public static final String GEOTIFF = "image/geotiff";
	public static final String OM_2 = "application/vnd.ogc.om+xml";
	public static final String STYLED_LAYER_DESCRIPTOR = "application/vnd.ogc.sld+xml";

	public static final MediaType NETCDF_TYPE = MediaType.valueOf(NETCDF);
	public static final MediaType X_NETCDF_TYPE = MediaType.valueOf(X_NETCDF);
	public static final MediaType GEOTIFF_TYPE = MediaType.valueOf(GEOTIFF);
	public static final MediaType OM_2_TYPE = MediaType.valueOf(OM_2);
	public static final MediaType STYLED_LAYER_DESCRIPTOR_TYPE = MediaType.valueOf(STYLED_LAYER_DESCRIPTOR);

	public static final String WORKING_DIR = get("workingDir");

	public static final String RESOURCE_PATH = Utils.join(File.separator, WORKING_DIR, "resources");
	public static final String HSQLDB_PATH = Utils.join(File.separator, WORKING_DIR, "database");

	public static final Period CLEAN_UP_INTERVAL = new Period(get("cleanup.interval", "PT2H"));
	public static final Period DELETE_OLDER_THAN_PERIOD = new Period(get("cleanup.deleteBefore", "P1D"));
	
	public static final String RESOURCE_STORE_KEY = "implementation.resourceStore";
	public static final String WMS_ADAPTER_KEY = "implementation.wmsAdapter";
	public static final String SEARCH_PACKAGES_KEY = "visualizerSearchPackages";
	
	public static final boolean PRETTY_PRINT_IO = Boolean.valueOf(get("prettyPrintIO", "false")); 
	
	public static final XmlOptions XML_OPTIONS = PRETTY_PRINT_IO ? 
			new XmlOptions().setSavePrettyPrint()
				.setLoadStripWhitespace()
				.setLoadStripProcinsts()
				.setLoadStripComments()
				.setLoadTrimTextBuffer()
				.setSaveAggressiveNamespaces() :
			new XmlOptions()
				.setLoadStripWhitespace()
				.setLoadStripProcinsts()
				.setLoadStripComments()
				.setLoadTrimTextBuffer()
				.setSaveAggressiveNamespaces();

	
	public static final String CONFIG_FILE = "/viss.properties";

	public static final String VISUALIZER_CONFIG_FILE = "/visualizers.rc";
	
	private static Properties p;

	public synchronized static String get(String key) {
		if (p == null) {
			try {
				p = new Properties();
				p.load(Constants.class.getResourceAsStream(CONFIG_FILE));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		String s = p.getProperty(key);

		return (s == null || s.trim().isEmpty()) ? null : s;
	}

	private static String get(String key, String defauld) {
		String s = get(key);
		return (s == null || s.trim().isEmpty()) ? defauld : s;
	}

}