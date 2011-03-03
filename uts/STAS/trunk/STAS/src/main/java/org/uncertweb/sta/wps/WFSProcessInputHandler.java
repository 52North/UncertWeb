package org.uncertweb.sta.wps;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.opengis.wfs.GetFeatureDocument;

import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;

public class WFSProcessInputHandler extends ProcessInputHandler<FeatureCollection<FeatureType, Feature>> {
	
	@Override
	@SuppressWarnings("unchecked")
	protected FeatureCollection<FeatureType, Feature> processInputs(Map<String, List<IData>> inputs) {
		long start = System.currentTimeMillis();
		
		String wfsUrl = Constants.Process.Inputs.WFS_URL.handle(inputs);
		GetFeatureDocument wfsReq = Constants.Process.Inputs.WFS_REQUEST.handle(inputs);
		FeatureCollection<FeatureType,Feature> paramPolColl = Constants.Process.Inputs.FEATURE_COLLECTION.handle(inputs);
		
		FeatureCollection<FeatureType,Feature> requestPolColl = null;
		
		if (wfsUrl != null && wfsReq != null) {
			IParser p = ParserFactory.getInstance().getParser(
					Namespace.GML.SCHEMA, IOHandler.DEFAULT_MIMETYPE, IOHandler.DEFAULT_ENCODING,
					GTVectorDataBinding.class);
			if (p == null) {
				throw new NullPointerException("No Parser found to parse FeatureCollection.");
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
