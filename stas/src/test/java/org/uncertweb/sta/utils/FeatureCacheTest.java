package org.uncertweb.sta.utils;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.joda.time.Period;
import org.junit.Test;
import org.uncertweb.sta.wps.STASException;
import org.uncertweb.utils.UwTimeUtils;

public class FeatureCacheTest {
	static String TEST_URL = "http://uncertdata.aston.ac.uk:8080/geoserver/UncertWeb/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=UncertWeb:feature_of_interest&featureID=FERA_Anglia_fields_TL17378558";
	
	@Test
	public void testGetGeometryFromWfs() {
		try {
			URL url = new URL(TEST_URL);
			FeatureCache fc = new FeatureCache();
			fc.getFeatureFromWfs(url);
			URL url2= new URL("http://uncertdata.aston.ac.uk:8080/geoserver/UncertWeb/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=UncertWeb:feature_of_interest&featureID=FERA_Anglia_fields_TL72664141");
			fc.getFeatureFromWfs(url2);
			fc.getFeatureFromWfs(url);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (STASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
