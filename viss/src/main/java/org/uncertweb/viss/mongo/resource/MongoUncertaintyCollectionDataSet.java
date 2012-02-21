package org.uncertweb.viss.mongo.resource;

import java.io.File;
import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.uncertweb.utils.UwIOUtils;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.UncertaintyReference;
import org.uncertweb.viss.core.resource.time.ITemporalExtent;
import org.uncertweb.viss.core.util.MediaTypes;

import com.google.code.morphia.annotations.Embedded;

public class MongoUncertaintyCollectionDataSet extends
		AbstractMongoDataSet<UncertaintyReference> {

	@Embedded
	private UncertaintyReference ref;

	public MongoUncertaintyCollectionDataSet() {
	}

	public MongoUncertaintyCollectionDataSet(
			MongoUncertaintyCollectionResource resource,
			UncertaintyReference ref) {
		super(resource);
		setContent(this.ref = ref);
	}

	@Override
	public String getPhenomenon() {
		return "UNKNOWN";
	}

	@Override
	public UncertaintyType getType() {
		return getContent().getType();
	}

	@Override
	protected UncertaintyReference loadContent() {
		if (ref.getContent() == null) {
			if (!ref.getMime().equals(MediaTypes.GEOTIFF_TYPE)) {
				throw VissError.internal("Currently only GeoTIFF is supported. (was "+ref.getMime()+")");
			}
			if (ref.getFile() == null) {
				try {
					ref.setFile(downloadReference());
				} catch (IOException e) {
					throw VissError.internal(e);
				}
			}

			try {
				GridCoverage2D coverage = new GeoTiffReader(ref.getFile())
						.read(null);
				ref.setContent(coverage);
			} catch (DataSourceException e) {
				throw VissError.internal(e);
			} catch (IOException e) {
				throw VissError.internal(e);
			}
		}
		return ref;
	}

	private File downloadReference() throws IOException {
		MongoResourceStore store = (MongoResourceStore) 
				VissConfig.getInstance().getResourceStore();
		File f = store.createResourceFile(getResource().getId(), ref.getMime());
		UwIOUtils.saveToFile(f, ref.getRef().toURL());
		return f;
	}
	
	@Override
	protected ITemporalExtent loadTemporalExtent() {
		return ITemporalExtent.NO_TEMPORAL_EXTENT;
	}

}
