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
import static org.uncertweb.viss.core.util.JSONConstants.DATASETS_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.PHENOMENON_KEY;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_DATASET_LIST_TYPE;

import java.net.URI;

import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.web.RESTServlet;

@Provider
public class DataSetCollectionProvider extends
		AbstractJsonCollectionWriterProvider<IDataSet> {

	public DataSetCollectionProvider() {
		super(IDataSet.class, JSON_DATASET_LIST_TYPE, DATASETS_KEY);
	}

	@Override
	protected JSONObject encode(IDataSet r) throws JSONException {
		URI uri = getUriInfo().getBaseUriBuilder().path(RESTServlet.DATASET)
				.build(r.getResource().getId(), r.getId());
		return new JSONObject().put(ID_KEY, r.getId())
				.put(PHENOMENON_KEY, r.getPhenomenon()).put(HREF_KEY, uri);
	}
}