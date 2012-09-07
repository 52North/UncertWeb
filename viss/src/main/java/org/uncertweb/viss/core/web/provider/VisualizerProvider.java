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
package org.uncertweb.viss.core.web.provider;

import static org.uncertweb.utils.UwJsonConstants.ID_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.DESCRIPTION_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.OPTIONS_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.SUPPORTED_UNCERTAINTIES_KEY;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZER_TYPE;

import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.uncertweb.netcdf.NcUwUncertaintyType;
import org.uncertweb.viss.core.vis.IVisualizer;

@Provider
public class VisualizerProvider extends
		AbstractJsonSingleWriterProvider<IVisualizer> {

	protected VisualizerProvider() {
		super(IVisualizer.class, JSON_VISUALIZER_TYPE);
	}

	@Override
	protected JSONObject encode(IVisualizer v) throws JSONException {
		JSONObject j = new JSONObject().putOpt(DESCRIPTION_KEY,
				v.getDescription()).put(ID_KEY, v.getShortName());
		JSONArray ar = new JSONArray();
		for (NcUwUncertaintyType ut : v.getCompatibleUncertaintyTypes()) {
			ar.put(ut.getUri());
		}
		j.put(SUPPORTED_UNCERTAINTIES_KEY, ar);
		if (v.getDataSet() != null) {
			j.putOpt(OPTIONS_KEY, v.getOptionsForDataSet(v.getDataSet()));
		} else {
			LoggerFactory.getLogger(getClass()).info("{}", v.getOptions());
			j.putOpt(OPTIONS_KEY, v.getOptions());
		}
		return j;
	}

}
