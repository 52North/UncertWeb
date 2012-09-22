package org.uncertweb.ems.test;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.uncertweb.ems.data.profiles.AbstractProfile;
import org.uncertweb.ems.exposuremodel.OutdoorModel;
import org.uncertweb.ems.io.OMProfileParser;
import org.uncertweb.netcdf.NcUwConstants;
import org.uncertweb.netcdf.NcUwFile;

public class ProcessTests {
//
//	private String testDataPath = "src/test/resources";
//	private String intervalTimeOMFile = testDataPath + "/activityProfile.xml";
//	private String genericTimeOMFile = testDataPath + "/albatross_response4_schedules2.xml";
//	private String ncRotterdam8days = testDataPath + "/rotterdam_conc_20110402";
//	private String ncRotterdam3days = testDataPath + "/nox_dummy_8days.nc";
//	private String ncMuenster3days = testDataPath + "/airQualityData.nc";
//	
//	/*
//	 * Outdoor Model tests:
//	 * - no temporal overlay
//	 * - no spatial overlay
//	 * 
//	 */
//	
//	public void testOutdoorModel(){
//		NcUwFile ncFile = Utils.getNetCDFUfile(ncRotterdam3days);
//		List<AbstractProfile> profileList = new OMProfileParser().OM2Profiles(Utils.getOMfile(genericTimeOMFile),
//				Utils.createTimeList(ncFile.getVariable(NcUwConstants.StandardNames.TIME).getUnitsString(), 
//						ncFile.getVariable(NcUwConstants.StandardNames.TIME).getDimension(0).getLength()),60);
//		new OutdoorModel().run(profileList, ncFile);
//		
//		// check if exposure values are in the profiles
//		
//	}
	
	
}
