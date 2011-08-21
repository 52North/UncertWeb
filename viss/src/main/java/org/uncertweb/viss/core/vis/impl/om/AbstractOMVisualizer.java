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
package org.uncertweb.viss.core.vis.impl.om;

import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.opengis.coverage.grid.GridCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.Visualization;
import org.uncertweb.viss.core.vis.Visualizer;

public abstract class AbstractOMVisualizer implements Visualizer {

	protected static final Logger log = LoggerFactory
			.getLogger(AbstractOMVisualizer.class);

	private final Visualizer vis;

	public AbstractOMVisualizer(Visualizer v) {
		this.vis = v;
	}

	@Override
	public boolean isCompatible(Resource r) {
		IObservationCollection col = (IObservationCollection) r.getResource();

		TimeObject t = null;
		for (AbstractObservation ao : col.getObservations()) {
			// this visualizer can only handle observations with the same time
			if (t == null) {
				t = ao.getResultTime();
			} else if (!t.equals(ao.getResultTime())) {
				log.debug("Different result times");
				return false;
			}
			if (!(ao.getResult().getValue() instanceof Resource)) {
				log.debug("result value not instance of resource: {}", ao
						.getResult().getValue().getClass());
				return false;
			}
			if (!vis.isCompatible((Resource) ao.getResult().getValue())) {
				log.debug("underlying visualizer is not compatible");
				return false;
			}
		}
		return true;
	}

	@Override
	public JSONObject getOptions() {
		return vis.getOptions();
	}

	@Override
	public Visualization visualize(Resource r, JSONObject params) {
		IObservationCollection col = (IObservationCollection) r.getResource();
		Set<GridCoverage> coverages = Utils.set();
		Double min = null, max = null;
		String uom = null;
		for (AbstractObservation ao : col.getObservations()) {
			if (!(ao.getResult().getValue() instanceof Resource)) {
				throw VissError.internal("Resource is not compatible");
			}
			Resource rs = (Resource) ao.getResult().getValue();
			if (!vis.isCompatible(rs)) {
				throw VissError.internal("Resource is not compatible");
			}
			Visualization v = vis.visualize(rs, params);

			if (uom == null) {
				uom = v.getUom();
			} else if (!uom.equals(v.getUom())) {
				throw VissError.internal("Different UOM");
			}
			
			if (min == null || v.getMinValue() < min.doubleValue()) {
				min = v.getMinValue();
				log.debug("Setting min to {}", min);
			}
			if (max == null || v.getMaxValue() > max.doubleValue()) {
				max = v.getMaxValue();
				log.debug("Setting max to {}", max);
			}

			coverages.addAll(v.getCoverages());
		}
		return new Visualization(r.getUUID(), getId(params), this, params,
				min.doubleValue(), max.doubleValue(), uom, coverages);
	}

	@Override
	public String getId(JSONObject params) {
		return vis.getId(params);
	}

	@Override
	public Set<MediaType> getCompatibleMediaTypes() {
		return Utils.set(Constants.OM_2_TYPE);
	}

	@Override
	public String getShortName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getDescription() {
		return vis.getDescription();
	}
}
