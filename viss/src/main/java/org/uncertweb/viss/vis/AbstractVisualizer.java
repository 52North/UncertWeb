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
package org.uncertweb.viss.vis;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.time.ITemporalExtent.CanBeBoth;
import org.uncertweb.viss.core.resource.time.ITemporalExtent.CanBeInstant;
import org.uncertweb.viss.core.resource.time.ITemporalExtent.CanBeInterval;
import org.uncertweb.viss.core.resource.time.NoTemporalExtent;
import org.uncertweb.viss.core.util.JSONSchema.Format;
import org.uncertweb.viss.core.util.JSONSchema.Key;
import org.uncertweb.viss.core.util.JSONSchema.Type;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.IVisualizer;

public abstract class AbstractVisualizer implements IVisualizer {
	
	public static final String TIME_OPTION = "time";
	public static final String TIME_OPTION_DESCRIPTION_BOTH = "The time to visualize; either a datetime or an interval represented by two datetimes";
	public static final String TIME_OPTION_DESCRIPTION_INSTANT = "The time to visualize; a datetime";
	public static final String TIME_OPTION_DESCRIPTION_INTERVAL = "The time to visualize; an interval represented by two datetimes";
	protected static final Logger log = LoggerFactory.getLogger(AbstractVisualizer.class);
	private Set<MediaType> compatibleTypes;
	private IDataSet resource;
	private JSONObject params;

	public AbstractVisualizer(MediaType... compatibleTypes) {
		this.compatibleTypes = Collections.unmodifiableSet(UwCollectionUtils
		    .set(compatibleTypes));
	}

	@Override
	public Set<MediaType> getCompatibleMediaTypes() {
		return this.compatibleTypes;
	}

	@Override
	public String getShortName() {
		return this.getClass().getName()
		    .replace(getClass().getPackage().getName() + ".", "").replace('$', '.');
	}

	@Override
	public String getId(JSONObject params) {
		return getShortName() + "-" + new ObjectId().toString();
	}

	@Override
	public void setDataSet(IDataSet r) {
		this.resource = r;
	}

	@Override
	public IDataSet getDataSet() {
		return this.resource;
	}

	protected JSONObject getParams() {
		return this.params;
	}

	protected void setParams(JSONObject params) {
		this.params = params;
	}

	@Override
	public Map<String, JSONObject> getOptions() {
		return UwCollectionUtils.map();
	}

	@Override
	public Map<String, JSONObject> getOptionsForDataSet(IDataSet r) {
		Map<String, JSONObject> options = getOptions();
		if (isTimeAware(r)) {
			try {
				if (r.getTemporalExtent() instanceof CanBeInstant) {
					JSONObject time = new JSONObject()
						.put(Key.DESCRIPTION, TIME_OPTION_DESCRIPTION_INSTANT)
						.put(Key.TYPE, Type.STRING)
						.put(Key.FORMAT, Format.DATETIME);
					options.put(TIME_OPTION, time);
				} else if (r.getTemporalExtent() instanceof CanBeInterval) {
					JSONObject time = new JSONObject()
						.put(Key.DESCRIPTION, TIME_OPTION_DESCRIPTION_INTERVAL)
						.put(Key.TYPE, Type.ARRAY)
						.put(Key.ITEMS, new JSONObject()
							.put(Key.TYPE, Type.STRING)
							.put(Key.FORMAT, Format.DATETIME))
						.put(Key.MIN_ITEMS, 2)
						.put(Key.MAX_ITEMS, 2);
					options.put(TIME_OPTION, time);
				} else if (r.getTemporalExtent() instanceof CanBeBoth) {
					JSONObject time = new JSONObject()
						.put(Key.DESCRIPTION, TIME_OPTION_DESCRIPTION_BOTH)
						.put(Key.TYPE, new JSONArray()
							.put(Type.STRING).put(Type.ARRAY))
						.put(Key.FORMAT, Format.DATETIME)
						.put(Key.ITEMS, new JSONObject()
							.put(Key.TYPE, Type.STRING)
							.put(Key.FORMAT, Format.DATETIME))
						.put(Key.MIN_ITEMS, 2)
						.put(Key.MAX_ITEMS, 2);
					options.put(TIME_OPTION, time);
				}
			} catch (JSONException e) {
				throw VissError.internal(e);
			}
		}
		return options;
	}
	
	protected TimeObject getSelectedTime() {
		try {
			Object to = getParams().opt(TIME_OPTION);
			if (to == null) {
				return null;
			} else if (to instanceof String) {
				return new TimeObject((String) to);
			} else if (to instanceof JSONArray) {
				JSONArray a = (JSONArray) to;
				return new TimeObject(a.getString(0), a.getString(1));
			} else {
				throw VissError.invalidParameter(TIME_OPTION);
			}
		} catch (JSONException e) {
			throw VissError.invalidParameter(TIME_OPTION);
		}
	}
	
	protected boolean isTimeAware() {
		return isTimeAware(getDataSet());
	}
	
	protected boolean isTimeAware(IDataSet ds) {
		return !(ds.getTemporalExtent() == null || ds.getTemporalExtent() instanceof NoTemporalExtent);
	}
	
	@Override
	public IVisualization visualize(IDataSet r, JSONObject params) {
		setDataSet(r);
		setParams(params);
		return visualize();
	}

	protected String getCoverageName() {
		return this.getId(getParams());
	}

	protected abstract IVisualization visualize();
}