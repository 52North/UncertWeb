package org.uncertweb.sta.utils;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.sta.wps.STASException;

import com.vividsolutions.jts.geom.Geometry;

/**
 * helper class for caching simple features from a WFS
 * 
 * @author staschc
 *
 */
public class FeatureCache {
	
	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory.getLogger(FeatureCache.class);
	
	/**
	 * Geotools parser used to parse the GML
	 */
	private Parser parser = new Parser(new GMLConfiguration());
	
	/**caches the features that are already retrieved from the WFS*/
	private HashMap<URL,SpatialSamplingFeature> feature4WfsURL= new HashMap<URL,SpatialSamplingFeature>(10000);
	
	/**
	 * adds a new feature and adds it to the cache; if it is already contained in cache, it is directly returned.
	 * 
	 * @param url
	 * 			Get URL to feature in WFS
	 * @return SpatialSamplingFeature
	 * @throws STASException
	 */
	public SpatialSamplingFeature getFeatureFromWfs(URL url) throws STASException {
		
		//feature is already contained in cache
		if (feature4WfsURL.containsKey(url)){
			return feature4WfsURL.get(url);
		}
		
		//geometry needs to be retrieved from WFS
		SpatialSamplingFeature feature = null;
		InputStream is = null;
		try {
			is = url.openConnection().getInputStream();
		} catch (Exception e) {
			log.info("Error while retrieving features from WFS: "+e.getLocalizedMessage());
			throw new STASException("Error while retrieving features from WFS: "+e.getLocalizedMessage());
		} 
		
		
		try {
			DefaultFeatureCollection featCol = (DefaultFeatureCollection)parser.parse(is);
			SimpleFeatureIterator features = featCol.features();
			while (features.hasNext()){
				SimpleFeature simpleFeat = features.next();
				String identifier = simpleFeat.getID();
				Geometry shape = (Geometry) simpleFeat.getDefaultGeometry();
				feature = new SpatialSamplingFeature(new Identifier(new URI("http://www.uncertweb.org"),identifier),null,shape);
				feature4WfsURL.put(url, feature);
			}
		} catch (Exception e) {
			log.info("Error while parsing features from WFS: "+e.getLocalizedMessage());
			throw new STASException("Error while parsing features from WFS: "+e.getLocalizedMessage());
		}
		return feature;
	}
}
