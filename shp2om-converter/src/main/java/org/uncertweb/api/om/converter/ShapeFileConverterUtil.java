package org.uncertweb.api.om.converter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

/**
 * Util class which contains some helper methods which are used in several classes
 * 
 * @author staschc
 *
 */
public class ShapeFileConverterUtil {

	
	/**
	 * parses a timestring which can contain either just one single ISO 8601 String or two comma-seperated time strings
	 *
	 * 
	 * @param phenTime
	 * 			timestring which can contain either just one single ISO 8601 String or two comma-seperated time strings
	 * @return 
	 * 			represents the timeobject which has been parsed
	 * @throws IOException 
	 * 			if timestring contains more than two time strings or if phenTime is empty string
	 */
	public static  TimeObject parsePhenTime(String phenTime) throws IOException {
		if (phenTime!=null&&!phenTime.equals("")){
			TimeObject to = null;
			String[] times = phenTime.split(",");
			if (times.length==1){
				to = new TimeObject(times[0]);
				return to;
			}
			else if (times.length==2){
				to = new TimeObject(times[0],times[1]);
				return to;
			}
			else {
				throw new IOException("Phenomenon Time can only be time instant or time period!");
			}
		}
		else {
			throw new IOException("PHENTIME property needs to be specified in properties file!!");
		}
		
	}
	
	/**
	 * helper method for creating SpatialSamplingFeature from featureID and JTS
	 * geometry
	 * 
	 * @param id
	 *            gml id of the geometry
	 * @param geom
	 *            JTS geometry of the sampling feature
	 * @return POJO representation of the SamplingFeature
	 * @throws URISyntaxException 
	 *             if parsing fails
	 */
	public static SpatialSamplingFeature createSamplingFeature(String id,
			Geometry geom) throws URISyntaxException {
		SpatialSamplingFeature sf = null;
		int srid = geom.getSRID();
		int multiGeomCounter=0;
		if (geom instanceof MultiLineString) {
			MultiLineString mls = (MultiLineString) geom;
			int size = mls.getNumGeometries();
			LineString[] lsArray = new LineString[size];
			for (int i = 0; i < size; i++) {
				lsArray[i] = new GeometryFactory().createLineString(((LineString) mls
						.getGeometryN(i)).getCoordinateSequence());
				lsArray[i].setSRID(srid);
				multiGeomCounter++;
			}
			MultiLineString gmlLineString =  new GeometryFactory().createMultiLineString(lsArray);
			Identifier identifier = new Identifier(new URI("http://www.uncertweb.org"),id);
			sf = new SpatialSamplingFeature(identifier,null, gmlLineString);
		}
		else if (geom instanceof MultiPolygon) {
//			MultiPolygon mp = (MultiPolygon) geom;
//			int size = mp.getNumGeometries();
//			LineString[] lsArray = new LineString[size];
//			for (int i = 0; i < size; i++) {
//				lsArray[i] = new GeometryFactory().createLineString(((MultiPolygon) mls
//						.getGeometryN(i)).getCoordinateSequence());
//				lsArray[i].setSRID(srid);
//				multiGeomCounter++;
//			}
//			MultiPolygon
//			MultiLineString gmlLineString =  new GeometryFactory().createMultiLineString(lsArray);
			Identifier identifier = new Identifier(new URI("http://www.uncertweb.org"),id);
			sf = new SpatialSamplingFeature(identifier,null, geom);
		}
		else if (geom instanceof Point) {
			Identifier identifier = new Identifier(new URI("http://www.uncertweb.org"),id);
			sf = new SpatialSamplingFeature(identifier,null, geom);
		}
		// TODO add further geometry types	
		return sf;
	}
}
