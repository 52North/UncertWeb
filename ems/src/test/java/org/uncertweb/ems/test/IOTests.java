package org.uncertweb.ems.test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;

import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.ems.EMSalgorithm;
import org.uncertweb.ems.data.profiles.AbstractProfile;
import org.uncertweb.ems.exceptions.EMSInputException;
import org.uncertweb.ems.io.OMProfileGenerator;
import org.uncertweb.ems.io.OMProfileParser;
import org.uncertweb.netcdf.NcUwConstants;
import org.uncertweb.netcdf.NcUwFile;

import ucar.nc2.units.DateUnit;

public class IOTests {//extends TestCase{

//	private String testDataPath = "src/test/resources";
//	private String intervalTimeOMFile = testDataPath + "/activityProfile.xml";
//	private String genericTimeOMFile = testDataPath + "/albatross_response4_schedules2.xml";
//	private String ncRotterdam8days = testDataPath + "/rotterdam_conc_20110402.nc";
//	private String ncRotterdam3days = testDataPath + "/nox_dummy_8days.nc";
//	private String ncMuenster3days = testDataPath + "/airQualityData.nc";
//	private String startDate = "hours since 2010-04-01 01:00:00 00:00";
//
//
//		// variables for profile parsing
//		private int minuteResolution = 1;
//		private ArrayList<DateTime> netcdfList = new ArrayList<DateTime>();
//
//		/*
//		 * Test cases for OMProfileParser
//		 * - Interval OM
//		 * -- minuteResolution (0,...,60*24)
//		 *
//		 * - GenericTime OM
//		 * -- minuteResolution (0,...,60*24)
//		 * -- netcdfList (empty, 1,...,10 values)
//		 *
//		 */
//
//		private void testOMProfileParserIntervalOM(){
//			IObservationCollection obsColl = Utils.getOMfile(intervalTimeOMFile);
//
//			// the netcdfList is not important for this test
//			netcdfList = Utils.createTimeList(startDate, 1);
//
//			// minuteResolution = 0
//			List<AbstractProfile> profileList = new OMProfileParser().OM2Profiles(obsColl, netcdfList, minuteResolution);
//			assertNotNull(profileList);
//			assertEquals(1, profileList.size());
//
//			// minuteResolution = 1
//			minuteResolution = 5;
//			profileList = new OMProfileParser().OM2Profiles(obsColl, netcdfList, minuteResolution);
//			assertNotNull(profileList);
//			assertEquals(1, profileList.size());
////			assertEquals((24*60)/minuteResolution, profileList.get(0).getSize());
//
//			// minuteResolution = 5
//			minuteResolution = 10;
//			profileList = new OMProfileParser().OM2Profiles(obsColl, netcdfList, minuteResolution);
//			assertNotNull(profileList);
//			assertEquals(1, profileList.size());
////			assertEquals((24*60)/minuteResolution, profileList.get(0).getSize());
//		}
//
//		private void testOMProfileParserGenericTimeOM(){
//			IObservationCollection obsColl = Utils.getOMfile(genericTimeOMFile);
//
//			// netcdfList = empty
//			minuteResolution = 5;
////			List<AbstractProfile> profileList = new OMProfileParser().OM2Profiles(obsColl, netcdfList, minuteResolution);
////			assertNotNull(profileList);
////			assertEquals(522, profileList.size());
//
//			// netcdfList = 1
//			netcdfList = Utils.createTimeList(startDate, 1);
//			List<AbstractProfile> profileList = new OMProfileParser().OM2Profiles(obsColl, netcdfList, minuteResolution);
//			assertNotNull(profileList);
////			assertEquals(522, profileList.size());
//
//			// netcdfList = 10
//			netcdfList = Utils.createTimeList(startDate, 10);
//			profileList = new OMProfileParser().OM2Profiles(obsColl, netcdfList, minuteResolution);
//			assertNotNull(profileList);
////			assertEquals(522, profileList.size());
//
//			// minuteResolution = 0
//			profileList = new OMProfileParser().OM2Profiles(obsColl, netcdfList, minuteResolution);
//			assertNotNull(profileList);
//			assertEquals(522, profileList.size());
//
//			// minuteResolution = 1
//			minuteResolution = 1;
//			profileList = new OMProfileParser().OM2Profiles(obsColl, netcdfList, minuteResolution);
//			assertNotNull(profileList);
//			assertEquals(522, profileList.size());
//
//			// minuteResolution = 5
//			minuteResolution = 5;
//			profileList = new OMProfileParser().OM2Profiles(obsColl, netcdfList, minuteResolution);
//			assertNotNull(profileList);
//			assertEquals(522, profileList.size());
//		}
//
//
//
//	private void testGetTimeArrayFromNcUwFile(){
//		// dummy Rotterdam data
//		NcUwFile ncFile = Utils.getNetCDFUfile(ncRotterdam8days);
//		ArrayList<DateTime> netcdfList = Utils.createTimeList(ncFile.getVariable(NcUwConstants.StandardNames.TIME).getUnitsString(), ncFile.getVariable(NcUwConstants.StandardNames.TIME).getDimension(0).getLength());
//		assertNotNull(netcdfList);
//
//		// new Rotterdam data
//		ncFile = Utils.getNetCDFUfile(ncRotterdam3days);
//		netcdfList = Utils.createTimeList(ncFile.getVariable(NcUwConstants.StandardNames.TIME).getUnitsString(), ncFile.getVariable(NcUwConstants.StandardNames.TIME).getDimension(0).getLength());
//		assertNotNull(netcdfList);
//
//		// Muenster data
//		ncFile = Utils.getNetCDFUfile(ncMuenster3days);
//		netcdfList = Utils.createTimeList(ncFile.getVariable(NcUwConstants.StandardNames.TIME).getUnitsString(), ncFile.getVariable(NcUwConstants.StandardNames.TIME).getDimension(0).getLength());
//		assertNotNull(netcdfList);
//	}
//
//



}
