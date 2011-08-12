package org.uncertweb.viss.core.resource.mongo;

import java.io.IOException;

import org.geotools.gce.geotiff.GeoTiffReader;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Constants;

import com.google.code.morphia.annotations.Polymorphic;

@Polymorphic
public class GeoTIFFResource extends AbstractMongoResource {

	public GeoTIFFResource() {
		super(Constants.GEOTIFF_TYPE);
	}

	@Override
	public void load() throws VissError, IOException {
		setContent(new GeoTiffReader(getFile()).read(null));
	}

}
