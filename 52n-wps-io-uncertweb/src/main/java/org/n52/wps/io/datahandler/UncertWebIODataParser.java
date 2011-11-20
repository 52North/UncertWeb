package org.n52.wps.io.datahandler;

import org.n52.wps.io.datahandler.netcdf.NetCDFParser;
import org.n52.wps.io.datahandler.om.OMParser;
import org.n52.wps.io.datahandler.uncertml.UncertMLParser;

public class UncertWebIODataParser extends DelegatingParser {
	public UncertWebIODataParser() {
		super(new OMParser(), new UncertMLParser(), new NetCDFParser());
	}
}
