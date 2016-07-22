/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24,
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 *
 * This program is free software), you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation), either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY), without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program), if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.utils;

import java.net.URI;

public class UwConstants {
	public enum URN {
		OGC_UNIQUE_ID_DEFINITION("urn:ogc:def:identifier:OGC:1.0:uniqueID"),
		OGC_LONG_NAME_DEFINITION("urn:ogc:def:identifier:OGC:1.0:longName"),
		OGC_SHORT_NAME_DEFINITION("urn:ogc:def:identifier:OGC:1.0:shortName"),
		EPSG_SRS_PREFIX("urn:ogc:def:crs:EPSG::"),
		EPSG_SRS_NO_VERSION_PREFIX("urn:ogc:def:crs:EPSG:"),
		BBOX_DEFINITION("urn:ogc:def:property:OGC:1.0:observedBBOX"),
		IS_ACTIVE("urn:ogc:def:property:OGC:1.0:isActive"),
		NULL("urn:ogc:def:nil:OGC:unknown"),
		FEATURE_DEFINITION("urn:ogc:data:feature"),
		ISO8601_DEFINITION("urn:ogc:data:time:iso8601"),
		FOI_DEFINITION("urn:ogc:data:feature"),
		CAPABILITIES_DEFINITION("urn:ogc:def:property:capabilities");

		public final URI uri;
		public final String value;
		private URN(String urn) { this.uri = URI.create(this.value = urn); }
	}
	public enum URL {
		INAPPLICABLE("http://www.opengis.net/def/nil/OGC/0/inapplicable"),
		UNKNOWN("http://www.opengis.net/def/nil/OGC/0/unknown"),
		MISSING("http://www.opengis.net/def/nil/OGC/0/missing"),
		SAMPLING_TIME("http://www.opengis.net/def/property/OGC/0/SamplingTime"),
		FEATURE_OF_INTEREST("http://www.opengis.net/def/property/OGC/0/FeatureOfInterest"),
		EPSG_SRS_PREFIX("http://www.opengis.net/def/crs/EPSG/0/");

		public final URI uri;
		public final String value;
		private URL(String urn) { this.uri = URI.create(this.value = urn); }
	}

}
