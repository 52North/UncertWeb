package org.uncertweb.ems.data.profiles;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;

import org.joda.time.Interval;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.CSVEncoder;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

public class GeometryProfile extends AbstractProfile{

	public GeometryProfile(IObservationCollection activityObservations) {
		super(activityObservations);
	}
}
