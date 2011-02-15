package org.uncertweb.sta.wps.method.grouping.spatial;

import static org.uncertweb.sta.utils.Constants.FEATURE_COLLECTION_INPUT_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.FEATURE_COLLECTION_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.FEATURE_COLLECTION_INPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.WFS_REQUEST_INPUT_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.WFS_REQUEST_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.WFS_REQUEST_INPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.WFS_URL_INPUT_DESC;
import static org.uncertweb.sta.utils.Constants.WFS_URL_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.WFS_URL_INPUT_TITLE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import net.opengis.wfs.GetFeatureDocument;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AlgorithmParameterException;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.SamplingSurface;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.ProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.xml.binding.GetFeatureRequestBinding;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Christian Autermann
 */
public class CoverageGrouping extends SpatialGrouping {
	protected static final Logger log = LoggerFactory.getLogger(CoverageGrouping.class);
	
	protected static final ProcessInput FEATURE_COLLECTION = new ProcessInput(
			FEATURE_COLLECTION_INPUT_ID, FEATURE_COLLECTION_INPUT_TITLE, FEATURE_COLLECTION_INPUT_DESCRIPTION,
			GTVectorDataBinding.class);
	protected static final ProcessInput WFS_URL = new ProcessInput(
			WFS_URL_INPUT_ID, WFS_URL_INPUT_TITLE, WFS_URL_INPUT_DESC,
			LiteralStringBinding.class);
	protected static final ProcessInput WFS_REQUEST = new ProcessInput(
			WFS_REQUEST_INPUT_ID, WFS_REQUEST_INPUT_TITLE, WFS_REQUEST_INPUT_DESCRIPTION,
			GetFeatureRequestBinding.class);

	@Override
	public Set<ProcessInput> getAdditionalInputDeclarations() {
		return Utils.set(FEATURE_COLLECTION, WFS_URL, WFS_REQUEST);
	}

	protected class LazyMappingIterator implements Iterator<ObservationMapping<ISamplingFeature>> {
		private FeatureIterator<?> iterator;

		public LazyMappingIterator(FeatureCollection<?, ?> features) {
			this.iterator = features.features();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public ObservationMapping<ISamplingFeature> next() {
			return map(iterator.next());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterator<ObservationMapping<ISamplingFeature>> iterator() {
		FeatureCollection<?, ?> features = getFeatureCollection();
		if (features == null) {
			throw new AlgorithmParameterException("No FeatureCollection found.");
		}
		return new LazyMappingIterator(features);
	}

	protected ObservationMapping<ISamplingFeature> map(Feature feature) {
		SimpleFeature f = (SimpleFeature) feature;
		if (f.getDefaultGeometry() == null) {
			throw new NullPointerException(
					"defaultGeometry is null in feature with Id: " + f.getID());
		}
		if (!(f.getDefaultGeometry() instanceof Geometry)) {
			// if the parser failed, it will be a string...
			log.warn("Can not handle Geometry of class {}.", f
					.getDefaultGeometry().getClass().getCanonicalName());
			return null;
		}
		Geometry geom = (Geometry) f.getDefaultGeometry();
		LinkedList<Observation> result = new LinkedList<Observation>();
		for (Observation o : getObservations()) {
			if (geom.contains(o.getObservationLocation())) {
				result.add(o);
			}
		}
		return new ObservationMapping<ISamplingFeature>(
				new SamplingSurface((Geometry) f.getDefaultGeometry(), null,
						f.getID(), (f.getName() == null) ? f.getID() : f
								.getName().getLocalPart()), result);
	}

	@SuppressWarnings("unchecked")
	private FeatureCollection<FeatureType,Feature> getFeatureCollection() {
		long start = System.currentTimeMillis();
		String wfsUrl = (String) getAdditionalInput(WFS_URL);
		GetFeatureDocument wfsReq = (GetFeatureDocument) getAdditionalInput(WFS_REQUEST);
		FeatureCollection<FeatureType,Feature> paramPolColl = 
			(FeatureCollection<FeatureType,Feature>) getAdditionalInput(FEATURE_COLLECTION);
		FeatureCollection<FeatureType,Feature> requestPolColl = null;
		if (wfsUrl != null && wfsReq != null) {
			IParser p = ParserFactory.getInstance().getParser(
					Namespace.GML.SCHEMA, IOHandler.DEFAULT_MIMETYPE, IOHandler.DEFAULT_ENCODING,
					GTVectorDataBinding.class);
			if (p == null) {
				throw new NullPointerException("No Parser found to parser FeatureCollection.");
			}
			try {
				InputStream wfsResponse = Utils.sendPostRequest(wfsUrl, wfsReq.xmlText());
				requestPolColl = ((GTVectorDataBinding) p.parse(wfsResponse, IOHandler.DEFAULT_MIMETYPE)).getPayload();
			} catch (IOException e) {
				log.error("Error while retrieving FeatureCollection from "
						+ wfsUrl, e);
				throw new RuntimeException(e);
			}
		}
		FeatureCollection<FeatureType, Feature> result = null;
		if (paramPolColl != null) {
			if (requestPolColl != null) {
				paramPolColl.addAll(requestPolColl);
			}
			result = paramPolColl;
		} else if (requestPolColl != null) {
			result = requestPolColl;
		}
		log.info("Fetching of FeatureCollection took {}", Utils.timeElapsed(start));
		return result;
	}
}
