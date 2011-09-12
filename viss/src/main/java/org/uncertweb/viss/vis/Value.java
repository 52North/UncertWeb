package org.uncertweb.viss.vis;

import org.uncertml.IUncertainty;
import org.uncertweb.api.om.TimeObject;

import com.vividsolutions.jts.geom.Point;

public class Value {
	private IUncertainty value;
	private Point location;
	private TimeObject time;

	protected Value(IUncertainty value, Point location, TimeObject time) {
		this.value = value;
		this.location = location;
		this.time = time;
	}

	public IUncertainty getValue() {
		return value;
	}

	public Point getLocation() {
		return location;
	}

	public TimeObject getTime() {
		return time;
	}
}