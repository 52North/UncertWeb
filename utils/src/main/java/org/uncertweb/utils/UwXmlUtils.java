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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlOptions;

/**
 * @author Christian Autermann
 */
public class UwXmlUtils {

	public enum Namespace {
		OM, SLD, SOS, OGC, SWE, SA, OWS, SML, GML, XSI, XLINK, WPS, UNCERTML, WFS, PARAM;
		private static final String XML_PROPERTIES_FILE = "/xml.properties";
		private static Properties xmlProps = null;
		public final String URI, PREFIX, SCHEMA;

		private static String getProperty(String key) {
			if (xmlProps == null) {
				xmlProps = new Properties();
				try {
					InputStream is = UwXmlUtils.class
							.getResourceAsStream(XML_PROPERTIES_FILE);
					if (is == null) {
                        throw new FileNotFoundException("XML Properties not found.");
                    }
					xmlProps.load(is);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return xmlProps.getProperty(key);
		}


		private Namespace() {
			String propPrefix = "xml." + this.toString().toLowerCase();
			this.URI = getProperty(propPrefix + ".uri");
			this.PREFIX = getProperty(propPrefix + ".prefix");
			this.SCHEMA = getProperty(propPrefix + ".schema");
		}

		public static final String SML_VERSION = SML.URI.substring(SML.URI
				.lastIndexOf('/') + 1);

		public QName q(String type) {
			return new QName(this.URI, type);
		}

	}

	public static XmlOptions defaultOptions() {
		HashMap<String, String> map = new HashMap<String, String>();
		for (Namespace ns : Namespace.values()) {
            map.put(ns.URI, ns.PREFIX);
        }
		return new XmlOptions().setSaveSuggestedPrefixes(map)
				.setSavePrettyPrint().setSaveAggressiveNamespaces();
	}
}
