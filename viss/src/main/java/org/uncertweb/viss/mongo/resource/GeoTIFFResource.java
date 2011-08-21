package org.uncertweb.viss.mongo.resource;

import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Constants;

import com.google.code.morphia.annotations.Polymorphic;

@Polymorphic
public class GeoTIFFResource extends AbstractMongoResource<GridCoverage2D> {

	public GeoTIFFResource() {
		super(Constants.GEOTIFF_TYPE);
	}

	@Override
	public void load() throws VissError, IOException {
		setContent(new GeoTiffReader(getFile()).read(null));
	}

	@Override
	protected String getPhenomenonForResource() {
		// TODO test this...
		return getContent().getName().toString();
	}

}
