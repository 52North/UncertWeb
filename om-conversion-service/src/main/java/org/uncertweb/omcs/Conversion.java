/*
 * Copyright (C) 2012 52° North Initiative for Geospatial Open Source Software
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
package org.uncertweb.omcs;

import static org.uncertweb.omcs.Constants.CSV_TYPE;
import static org.uncertweb.omcs.Constants.JSON_TYPE;
import static org.uncertweb.omcs.Constants.XML_TYPE;

import java.io.InputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.uncertweb.api.om.io.CSVEncoder;
import org.uncertweb.api.om.io.IObservationEncoder;
import org.uncertweb.api.om.io.IObservationParser;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.JSONObservationParser;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

public enum Conversion {

	JSON_TO_CSV(JSON_TYPE, CSV_TYPE) {
		@Override
		public Response conv(InputStream i) {
			return toCsv(fromJson(i));
		}
	},
	XML_TO_CSV(XML_TYPE, CSV_TYPE) {
		@Override
		public Response conv(InputStream i) {
			return toCsv(fromXml(i));
		}
	},
	XML_TO_JSON(XML_TYPE, JSON_TYPE) {
		@Override
		public Response conv(InputStream i) {
			return toJson(fromXml(i));
		}
	},
	JSON_TO_XML(JSON_TYPE, XML_TYPE) {
		@Override
		public Response conv(InputStream i) {
			return toXml(fromJson(i));
		}
	};

	private MediaType from, to;

	private Conversion(MediaType from, MediaType to) {
		this.from = from;
		this.to = to;
	}

	protected IObservationCollection decode(IObservationParser d, InputStream i) {
		try {
			return d.parse(IOUtils.toString(i));
		} catch (Exception e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} finally {
			IOUtils.closeQuietly(i);
		}
	}

	protected Response encode(MediaType m, IObservationEncoder e,
			IObservationCollection c) {
		try {
			return Response.ok().type(m)
					.entity(e.encodeObservationCollection(c)).build();
		} catch (Exception x) {
			throw new WebApplicationException(x, Status.INTERNAL_SERVER_ERROR);
		}
	}

	protected Response toXml(IObservationCollection c) {
		return encode(XML_TYPE, new XBObservationEncoder(), c);
	}

	protected Response toJson(IObservationCollection c) {
		return encode(JSON_TYPE, new JSONObservationEncoder(), c);
	}

	protected Response toCsv(IObservationCollection c) {
		return encode(CSV_TYPE, new CSVEncoder(), c);
	}

	protected IObservationCollection fromXml(InputStream i) {
		return decode(new XBObservationParser(), i);
	}

	protected IObservationCollection fromJson(InputStream i) {
		return decode(new JSONObservationParser(), i);
	}

	public static Conversion fromMediaTypes(MediaType from, MediaType to) {
		for (Conversion c : values()) {
			if (c.from.isCompatible(from) && c.to.isCompatible(to)) {
				return c;
			}
		}
		return null;
	}

	public abstract Response conv(InputStream i);
}
