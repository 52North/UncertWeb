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

import static org.uncertweb.utils.UwJsonConstants.HREF_KEY;
import static org.uncertweb.utils.UwJsonConstants.ID_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.VISUALIZATIONS_KEY;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZATION_LIST_TYPE;

import java.net.URI;

import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.web.RESTServlet;

@Provider
public class VisualizationCollectionProvider extends
		AbstractJsonCollectionWriterProvider<IVisualization> {

	protected VisualizationCollectionProvider() {
		super(IVisualization.class, JSON_VISUALIZATION_LIST_TYPE,
				VISUALIZATIONS_KEY);
	}

	@Override
	protected JSONObject encode(IVisualization v) throws JSONException {
		URI uri = getUriInfo()
				.getBaseUriBuilder()
				.path(RESTServlet.VISUALIZATION)
				.build(v.getDataSet().getResource().getId(),
						v.getDataSet().getId(), v.getId());
		return new JSONObject().put(ID_KEY, v.getId()).put(HREF_KEY, uri);
	}
}
