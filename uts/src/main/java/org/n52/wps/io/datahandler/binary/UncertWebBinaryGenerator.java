/***************************************************************
Copyright © 2009 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.io.datahandler.binary;

import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_NETCDF;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_NETCDFX;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_NETCDF_U;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.datahandler.AbstractUwGenerator;
import org.uncertweb.utils.UwCollectionUtils;

public class UncertWebBinaryGenerator extends AbstractUwGenerator{
	
	public UncertWebBinaryGenerator() {
		super(
			set(SCHEMA_NETCDF_U), 
			null,
			set(MIME_TYPE_NETCDFX, MIME_TYPE_NETCDF), 
			UwCollectionUtils.<Class<?>>set(UncertWebDataBinding.class)
		);
	}
	
	private static Logger LOGGER = Logger.getLogger(UncertWebBinaryGenerator.class);
	
	public boolean isSupportedSchema(String schema) {
		boolean ret = false;
		if(schema == null){
			ret = true;
		}
		else {
			if (schema.isEmpty()){
				ret = true;
			}
		}
		return ret;
	}

	

	public void writeToStream(IData outputData, OutputStream outputStream) {
		
		
		if(!(outputData instanceof UncertWebDataBinding)){
			throw new RuntimeException("UncertWebBinaryGenerator writer does not support incoming datatype");
		}
		LOGGER.info("Generating tempfile ...");
		InputStream theStream = ((UncertWebDataBinding)outputData).getPayload().dataStream;
		
		try {
			IOUtils.copy(theStream, outputStream);
			theStream.close();
			LOGGER.info("Tempfile generated!");
			System.gc();
		} catch (Exception e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
	}
	
	
}
