package org.uncertweb.sta.wps.sos;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import net.opengis.sos.x10.GetObservationDocument;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;


public class GetObservationRequestCache {
	private class CachedGetObservationRequest implements Comparable<CachedGetObservationRequest> {
		private DateTime timestamp;
		private String url;
		private GetObservationDocument request;
		private ObservationCollection oc;

		
		public CachedGetObservationRequest(String url, GetObservationDocument request, ObservationCollection oc) {
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

		public GetObservationDocument getRequest() {
			return request;
		}

		public ObservationCollection getObservationCollection() {
			return oc;
		}

		@Override
		public int compareTo(CachedGetObservationRequest o) {
			return this.getTimestamp().compareTo(o.getTimestamp());
		}
	}
	
	private static final Logger log = LoggerFactory.getLogger(GetObservationRequestCache.class);
	
	private static GetObservationRequestCache instance;

	public synchronized static GetObservationRequestCache getInstance() {
		if (instance == null) {
			instance = new GetObservationRequestCache();
		}
		return instance;
	}
	
	private TreeSet<CachedGetObservationRequest> allCachedRequests = new TreeSet<CachedGetObservationRequest>();
	private Map<String, Map<GetObservationDocument, CachedGetObservationRequest>> cachedPostRequests 
			= new HashMap<String, Map<GetObservationDocument, CachedGetObservationRequest>>();;
	private Map<String, CachedGetObservationRequest> cahcedGetRequests = new HashMap<String, CachedGetObservationRequest>();
	
	private GetObservationRequestCache() {}
	
	public synchronized ObservationCollection getObservationCollection(String url, GetObservationDocument request, boolean dropCache) {
		CachedGetObservationRequest c = getCachedRequest(url, request);
		if (dropCache || c == null) {
			InputStream is = null;
			try {
				long start = System.currentTimeMillis();
				if (request == null) {
					is = Utils.sendGetRequest(url);
				} else {
					is = Utils.sendPostRequest(url, request.xmlText());
				}
				putCachedRequest(c = new CachedGetObservationRequest(url, request, parse(is)));
				log.info("Fetching of ObservationCollection took {}.", Utils.timeElapsed(start));
			} catch (IOException e) {
				log.error("Error while retrieving ObservationCollection from " + url, e);
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(is);
			}
		} else {
			log.info("Using cached ObservationCollection.");
		}
		return c.getObservationCollection();
	}
	
	private CachedGetObservationRequest getCachedRequest(String url, GetObservationDocument request) {
		if (request == null) {
			return this.cahcedGetRequests.get(url);
		} else {
			if (this.cachedPostRequests.get(url) != null) {
				return this.cachedPostRequests.get(url).get(request);
			} else {
				return null;
			}
		}
	}
	
	private void putCachedRequest(CachedGetObservationRequest c) {
		if (c.getRequest() == null) {
			this.cahcedGetRequests.put(c.getUrl(), c);
		} else {
			if (this.cachedPostRequests.get(c.getUrl()) == null) {
				this.cachedPostRequests.put(c.getUrl(), new HashMap<GetObservationDocument, CachedGetObservationRequest>());
			}
			this.cachedPostRequests.get(c.getUrl()).put(c.getRequest(), c);
		}
		this.allCachedRequests.add(c);
		assureRestrictions();
	}
	
	private void assureRestrictions() {
		int quantity = this.allCachedRequests.size() - Constants.MAX_CACHED_REQUESTS;
		if (quantity > 0) {
			LinkedList<CachedGetObservationRequest> toDelete = new LinkedList<CachedGetObservationRequest>();
			Iterator<CachedGetObservationRequest> i = this.allCachedRequests.iterator();
			while (toDelete.size() < quantity) {
				toDelete.add(i.next());
			}
			for (CachedGetObservationRequest c : toDelete) {
				dropCachedRequest(c);
			}
		}
	}

	private void dropCachedRequest(CachedGetObservationRequest toDelete) {
		CachedGetObservationRequest c = null;
		if (toDelete.getRequest() == null) {
			c = this.cahcedGetRequests.remove(toDelete.getUrl());
		} else if (this.cachedPostRequests.get(toDelete.getUrl()) != null) {
			c = this.cachedPostRequests.get(toDelete.getUrl()).remove(toDelete.getRequest());
			if (this.cachedPostRequests.get(toDelete.getUrl()).isEmpty()) {
				this.cachedPostRequests.remove(toDelete.getUrl());
			}
		}
		this.allCachedRequests.remove(c);
	}
	
	private ObservationCollection parse(InputStream is) {
		AbstractXMLParser p = (AbstractXMLParser) ParserFactory.getInstance()
				.getParser(Namespace.OM.SCHEMA, IOHandler.DEFAULT_MIMETYPE,
						IOHandler.DEFAULT_ENCODING,
						ObservationCollectionBinding.class);
		return ((ObservationCollectionBinding) p.parseXML(is)).getPayload();
	}
	
}
