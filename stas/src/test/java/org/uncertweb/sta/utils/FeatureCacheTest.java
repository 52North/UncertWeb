package org.uncertweb.sta.utils;

import java.net.MalformedURLException;

import org.junit.Test;
import org.uncertweb.sta.wps.STASException;

public class FeatureCacheTest {
    private static final String URL1
            = "http://uncertdata.aston.ac.uk:8080/geoserver/UncertWeb/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=UncertWeb:feature_of_interest&featureID=FERA_Anglia_fields_TL17378558";
    private static final String URL2
            = "http://uncertdata.aston.ac.uk:8080/geoserver/UncertWeb/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=UncertWeb:feature_of_interest&featureID=FERA_Anglia_fields_TL72664141";

    @Test
    public void testGetGeometryFromWfs()
            throws STASException, MalformedURLException {
        FeatureCache fc = new FeatureCache();
        fc.getFeatureFromWfs(URL1);
        fc.getFeatureFromWfs(URL2);
        fc.getFeatureFromWfs(URL1);
    }

}
