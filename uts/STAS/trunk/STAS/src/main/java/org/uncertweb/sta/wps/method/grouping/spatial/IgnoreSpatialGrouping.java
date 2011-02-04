package org.uncertweb.sta.wps.method.grouping.spatial;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.SamplingSurface;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.ProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class IgnoreSpatialGrouping extends SpatialGrouping {
	private static final Random random = new Random();

	@Override
	public Iterator<ObservationMapping<ISamplingFeature>> iterator() {

		List<Observation> obs = getObservations();
		ISamplingFeature f = null;
		int size = obs.size();

		switch (size) {
		case 0:
			return new LinkedList<ObservationMapping<ISamplingFeature>>()
					.iterator();
		case 1:
			f = obs.get(0).getFeatureOfInterest();
			break;
		default:
			ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
			for (Observation o : obs) {
				for (Coordinate c : o.getObservationLocation().getCoordinates()) {
					coordinates.add(c);
				}
			}
			GeometryFactory gf = new GeometryFactory();
			Geometry ch = gf.createMultiPoint(coordinates.toArray(new Coordinate[0])).convexHull();
			long n = random.nextLong();
			if (n == Long.MIN_VALUE) {
				n = 0;
			} else {
				n = Math.abs(n);
			}
			String id = "foi_" + Long.toString(n);
			f = new SamplingSurface(ch, Constants.NULL_URN, id, id);
		}

		return Utils.mutableSingletonList(new ObservationMapping<ISamplingFeature>(f, obs)).iterator();
	}
	
	@Override
	public Set<ProcessInput> getAdditionalInputDeclarations() {
		return Utils.set();
	}


}
