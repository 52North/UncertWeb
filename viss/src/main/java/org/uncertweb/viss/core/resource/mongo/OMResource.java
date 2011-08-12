package org.uncertweb.viss.core.resource.mongo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.ReferenceObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.Utils;

import com.google.code.morphia.annotations.Polymorphic;
import com.google.code.morphia.annotations.Transient;

@Polymorphic
public class OMResource extends AbstractMongoResource {

	public OMResource() {
		super(Constants.OM_2_TYPE);
	}

	@Transient
	private Set<AbstractMongoResource> resources = null;
	private Map<String, File> resourceFiles;

	@Override
	public void load() throws IOException {
		IObservationCollection col = parseCollection();
		setContent(col);

		if (resourceFiles == null)
			resources = Utils.set();
		if (resourceFiles == null)
			resourceFiles = Utils.map();

		for (AbstractObservation ao : col.getObservations()) {
			if (ao instanceof ReferenceObservation) {
				processReference((ReferenceResult) ao.getResult());
			}
		}

		for (Resource r : resources) {
			r.load();
		}
	}

	private void processReference(ReferenceResult rr) throws IOException {

		if (rr.getValue() != null) {
			/* reference is already resolved */
			if (!resources.contains(rr.getValue())) {
				resources.add((AbstractMongoResource) rr.getValue());
			}
			return;
		}

		MongoResourceStore mrs = (MongoResourceStore) VissConfig.getInstance()
				.getResourceStore();
		MediaType mt = MediaType.valueOf(rr.getRole());

		/* check whether we can resolve the reference */
		AbstractMongoResource r = mrs.getResourceForMediaType(mt);

		/* check whether we have loaded the referenced file */
		File f = resourceFiles.get(rr.getHref());
		if (f == null) {
			/* fetch the referenced file */
			URL url = new URL(rr.getHref());
			f = mrs.createResourceFile(getUUID(), mt);
			Utils.saveToFile(f, url);
			resourceFiles.put(rr.getHref(), f);
		}

		r.setFile(f);
		r.setUUID(getUUID());
		r.setLastUsage(getLastUsage());

		resources.add(r);
	}

	private IObservationCollection parseCollection() throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(getFile());
			return new XBObservationParser().parse(IOUtils.toString(is));
		} catch (OMParsingException e) {
			throw VissError.internal(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public void suspend() {
		if (resources != null) {
			for (Resource r : resources) {
				r.suspend();
			}
		}
	}
}
