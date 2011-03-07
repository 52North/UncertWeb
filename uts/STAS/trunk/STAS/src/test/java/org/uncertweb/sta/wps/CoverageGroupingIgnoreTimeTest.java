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
package org.uncertweb.sta.wps;

import java.io.InputStream;

import net.opengis.wfs.GetFeatureDocument;

import org.joda.time.DateTime;
import org.junit.Test;
import org.uncertweb.intamap.utils.TimeUtils;
import org.uncertweb.sta.wps.method.aggregation.impl.ArithmeticMeanAggregation;
import org.uncertweb.sta.wps.method.grouping.impl.CoverageGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.IgnoreTimeGrouping;
import org.uncertweb.sta.wps.testutils.ProcessTester;


public class CoverageGroupingIgnoreTimeTest {

	private static final String BEGIN_DATE = "2001-01-01T01:30:00.000+00:00";
	private static final String DURATION = "PT1H";
	private static final String OFFERING = "O3";
	private static final String OBSERVED_PROPERTY = "http://giv-genesis.uni-muenster.de:8080/SOR/REST/phenomenon/OGC/Concentration[" + OFFERING + "]";
	private static final String WFS_URL = "http://giv-uw.uni-muenster.de:8080/geoserver/wfs/GetFeature";
	private static final String WFS_REQUEST_LOCATION = "/example-wfs-request.xml";
	private static final String SOURCE_SOS = "http://giv-uw.uni-muenster.de:8080/AQE/sos";
	private static final String DESTINATION_SOS = "http://localhost:8080/STAS-SOS/sos";
	private static final String STAS_URL = "http://localhost:8080/stas/WebProcessingService";
	
	private static GetFeatureDocument getWFSRequest() {
		try {
			InputStream is = CoverageGroupingIgnoreTimeTest.class.getResourceAsStream(WFS_REQUEST_LOCATION);
			if (is == null)
				throw new NullPointerException();
			GetFeatureDocument doc = GetFeatureDocument.Factory.parse(is);
			return doc;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void test() throws Exception {
		ProcessTester t = new ProcessTester();
		t.selectAlgorithm(CoverageGrouping.class, IgnoreTimeGrouping.class);
		t.setSpatialAggregationMethod(ArithmeticMeanAggregation.class);
		t.setTemporalAggregationMethod(ArithmeticMeanAggregation.class);
		t.setSosSourceUrl(SOURCE_SOS);
		t.setSosDestinationUrl(DESTINATION_SOS);
		t.setGroupByObservedProperty(true);
		
		t.setTemporalBeforeSpatialAggregation(true);
		DateTime b = TimeUtils.parseDateTime(BEGIN_DATE);
		DateTime e = b.plus(TimeUtils.parsePeriod(DURATION));
		t.setSosRequest(OFFERING, OBSERVED_PROPERTY, b, e);

		t.setWfsUrl(WFS_URL);
		t.setWfsRequest(getWFSRequest());
		
		t.execute(STAS_URL);
//		t.getOutput();
//		t.getReferenceOutput();
	}

	public static void main(String[] args) throws Exception {
		try { new CoverageGroupingIgnoreTimeTest().test(); }
		catch (Throwable t) {t.printStackTrace();}
		System.exit(0);
	}
}
