package org.uncertweb.viss.core.resource.mongo;

import java.io.IOException;

import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Constants;

import ucar.nc2.NetcdfFile;

import com.google.code.morphia.annotations.Polymorphic;

@Polymorphic
public class NetCDFResource extends AbstractMongoResource {

	public NetCDFResource() {
		super(Constants.NETCDF_TYPE);
	}

	private static final boolean LOAD_TO_MEMORY = false;

	@Override
	public void load() throws IOException, VissError {
		try {
			String path = getFile().getAbsolutePath();
			NetcdfFile f = (LOAD_TO_MEMORY) ? NetcdfFile.openInMemory(path)
					: NetcdfFile.open(path);
			setContent(new NetcdfUWFile(f));
		} catch (NetcdfUWException e) {
			throw VissError.internal(e);
		}
	}

}