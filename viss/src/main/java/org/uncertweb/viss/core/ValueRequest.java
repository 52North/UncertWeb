package org.uncertweb.viss.core;

import org.uncertweb.api.om.TimeObject;

import com.vividsolutions.jts.geom.Geometry;

public class ValueRequest {
	private Geometry location;
	private TimeObject time;

	public ValueRequest(Geometry location, TimeObject time) {
		setLocation(location);
		setTime(time);
	}

	public Geometry getLocation() {
		return location;
	}

	public void setLocation(Geometry location) {
		this.location = location;
	}

	public TimeObject getTime() {
		return time;
	}

	public void setTime(TimeObject time) {
		this.time = time;
	}
}