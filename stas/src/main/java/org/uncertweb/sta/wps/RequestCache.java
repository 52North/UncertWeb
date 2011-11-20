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
package org.uncertweb.sta.wps;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.parser.AbstractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.sta.utils.Utils;

/**
 * TODO JavaDoc
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class RequestCache<T extends XmlObject, U> {

	private class CachedRequest implements Comparable<CachedRequest> {

		private DateTime timestamp;
		private String url;
		private T request;
		private U oc;

		public CachedRequest(String url, T request, U oc) {
			this.timestamp = new DateTime();
			this.url = url;
			this.request = request;
			this.oc = oc;
		}

		public DateTime getTimestamp() {
			return timestamp;
		}

		public String getUrl() {
			return url;
		}

		public T getRequest() {
			return request;
		}

		public U getU() {
			return oc;
		}

		@Override
		public int compareTo(CachedRequest o) {
			return this.getTimestamp().compareTo(o.getTimestamp());
		}
	}

	private static final Logger log = LoggerFactory.getLogger(RequestCache.class);

	private TreeSet<CachedRequest> allCachedRequests = new TreeSet<CachedRequest>();
	private Map<String, Map<T, CachedRequest>> cachedPostRequests = new HashMap<String, Map<T, CachedRequest>>();;
	private Map<String, CachedRequest> cachedGetRequests = new HashMap<String, CachedRequest>();
	private String schema;
	private int cachedRequests;
	private Class<? extends IData> binding;

	public RequestCache(String schema, Class<? extends IData> binding, int cachedRequests) {
		this.schema = schema;
		this.cachedRequests = cachedRequests;
		this.binding = binding;

	}

	public synchronized U getResponse(String url, T request, boolean dropCache) {
		CachedRequest c = getCachedRequest(url, request);
		if (dropCache || c == null) {
			InputStream is = null;
			try {
				long start = System.currentTimeMillis();
				if (request == null) {
					is = Utils.sendGetRequest(url);
				} else {
					is = Utils.sendPostRequest(url, request.xmlText());
				}
				putCachedRequest(c = new CachedRequest(url, request, parse(is)));
				log.info("Fetching took {}.", Utils.timeElapsed(start));
			} catch (IOException e) {
				log.error("Error while fetching response from " + url, e);
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(is);
			}
		} else {
			log.info("Using cached response.");
		}
		return c.getU();
	}

	private CachedRequest getCachedRequest(String url, T request) {
		if (request == null) {
			return this.cachedGetRequests.get(url);
		} else {
			if (this.cachedPostRequests.get(url) != null) {
				return this.cachedPostRequests.get(url).get(request);
			} else {
				return null;
			}
		}
	}

	private void putCachedRequest(CachedRequest c) {
		if (c.getRequest() == null) {
			this.cachedGetRequests.put(c.getUrl(), c);
		} else {
			if (this.cachedPostRequests.get(c.getUrl()) == null) {
				this.cachedPostRequests
						.put(c.getUrl(), new HashMap<T, CachedRequest>());
			}
			this.cachedPostRequests.get(c.getUrl()).put(c.getRequest(), c);
		}
		this.allCachedRequests.add(c);
		assureRestrictions();
	}

	private void assureRestrictions() {
		int quantity = this.allCachedRequests.size() - this.cachedRequests;
		if (quantity > 0) {
			LinkedList<CachedRequest> toDelete = new LinkedList<CachedRequest>();
			Iterator<CachedRequest> i = this.allCachedRequests.iterator();
			while (toDelete.size() < quantity) {
				toDelete.add(i.next());
			}
			for (CachedRequest c : toDelete) {
				dropCachedRequest(c);
			}
		}
	}

	private void dropCachedRequest(CachedRequest toDelete) {
		CachedRequest c = null;
		if (toDelete.getRequest() == null) {
			c = this.cachedGetRequests.remove(toDelete.getUrl());
		} else if (this.cachedPostRequests.get(toDelete.getUrl()) != null) {
			c = this.cachedPostRequests.get(toDelete.getUrl())
					.remove(toDelete.getRequest());
			if (this.cachedPostRequests.get(toDelete.getUrl()).isEmpty()) {
				this.cachedPostRequests.remove(toDelete.getUrl());
			}
		}
		this.allCachedRequests.remove(c);
	}

	@SuppressWarnings("unchecked")
	private U parse(InputStream is) {
		AbstractParser p = (AbstractParser) ParserFactory.getInstance()
			.getParser(this.schema, "text/xml", 
						IOHandler.DEFAULT_ENCODING, this.binding);
		return (U) p.parse(is, "text/xml", IOHandler.DEFAULT_ENCODING);
	}

}
