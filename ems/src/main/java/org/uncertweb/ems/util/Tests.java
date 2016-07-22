package org.uncertweb.ems.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.statistic.IStatistic;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.StandardDeviation;
import org.uncertml.statistic.StatisticCollection;
import org.uncertweb.api.om.GeneralTimeInstant;
import org.uncertweb.api.om.GeneralTimeInterval;
import org.uncertweb.api.om.IGeneralTime;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.CSVEncoder;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.ems.data.profiles.AbstractProfile;
import org.uncertweb.ems.data.profiles.Profile;
import org.uncertweb.ems.exposuremodel.OutdoorModel;
import org.uncertweb.ems.io.OMProfileGenerator;
import org.uncertweb.ems.io.OMProfileParser;
import org.uncertweb.netcdf.INcUwVariable;
import org.uncertweb.netcdf.NcUwConstants;
import org.uncertweb.netcdf.NcUwFile;
import org.uncertweb.om.io.v1.XBv1ObservationParser;

import ucar.nc2.Variable;
import ucar.nc2.units.DateUnit;

import com.vividsolutions.jts.geom.Geometry;

public class Tests {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		testNetCDFfile("D:/UncertWeb/WP3/sample data/WP6/oslo_conc_20110103_all_variables.nc");
//		testNetCDFfile("src/test/resources/nox_dummy_8days.nc ");
		//	testEMSalgorithm();

		// try {
		// XmlObject xml = XmlObject.Factory.parse(new
		// FileInputStream("D:/JavaProjects/aqMS-wps/src/main/resources/Austal/inputs/largeStreets.xml"));
		// IObservationCollection obs = (IObservationCollection) new
		// XBObservationParser().parse(xml.xmlText());
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (OMParsingException e) {
		// e.printStackTrace();
		// } catch (XmlException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		// try {
		// createDummyTrajectoryOMColl("D:/UncertWeb/WP8/D8.3/WP7_data/albatross_output_new2.xml",
		// "D:/UncertWeb/WP8/D8.3/dummy_WP8.3_output.xml");
		// } catch (OMEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		 try {
			 //D:\UncertWeb\WP8\D8.3\WP7_data
			 xml2json("D:/UncertWeb/WP8/D8.3/WP7_data/albatross_output_short_1.xml");
		 } catch (OMEncodingException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }

//		try {
//			 createDummyOMColl("D:/UncertWeb/meetings/12-09 Workshop Muenster/AQMS_2010-01.xml",
//					 "D:/UncertWeb/meetings/12-09 Workshop Muenster/MS_points_PM10.xml");
//		 } catch (OMEncodingException e) {
//			 // TODO Auto-generated catch block
//			 e.printStackTrace();
//		 }


	}

	private static void testNetCDFfile(String ncFilePath){
		NcUwFile ncFile = null;
		try {
			ncFile = new NcUwFile(ncFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error while reading NetCDF input: "
					+ e.getMessage(), e);
		}

		// check primary variables
		Set<INcUwVariable> primaryVariables = ncFile.getPrimaryVariables();
		for(INcUwVariable primVar : primaryVariables){
			primVar.getCRS();
		}
	}

	private static void xml2json(String xmlPath) throws OMEncodingException{
		IObservationCollection template = null;
		try {
			XmlObject xml = XmlObject.Factory.parse(new FileInputStream(
					xmlPath));
			template = (IObservationCollection) new XBObservationParser()
					.parse(xml.xmlText());
			new JSONObservationEncoder().encodeObservationCollection(template,
					new File(xmlPath.replace("xml", "json")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OMParsingException e) {
			e.printStackTrace();
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	// method to create a dummy uncertainty data with OM input
		private static void createDummyOMColl(String omTemplatePath,
				String omDummyPath) throws OMEncodingException {
			IObservationCollection template = null;
			try {
				XmlObject xml = XmlObject.Factory.parse(new FileInputStream(
						omTemplatePath));
				template = (IObservationCollection) new XBObservationParser()
						.parse(xml.xmlText());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (OMParsingException e) {
				e.printStackTrace();
			} catch (XmlException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			UncertaintyObservationCollection dummy = new UncertaintyObservationCollection();

			// go through observations
			for (AbstractObservation obs : template.getObservations()) {

				// get result
				NormalDistribution newDist = null;
				if(obs.getResult() instanceof UncertaintyResult){
					if(obs.getResult().getValue() instanceof StatisticCollection){
						double mean=0, var=0;
						StatisticCollection statColl = (StatisticCollection) obs.getResult().getValue();
						for(IStatistic stat: statColl.getMembers()){
							if (stat instanceof Mean){
								mean = ((Mean)stat).getValues().get(0);
							}else if(stat instanceof StandardDeviation){
								var = Math.pow(((StandardDeviation)stat).getValues().get(0),2);
							}
						}

						newDist = new NormalDistribution(mean,var);
					}
				}else{
					newDist = new NormalDistribution(
							Double.parseDouble(obs.getResult().getValue().toString()),
							Double.parseDouble(obs.getResult().getValue().toString()) * Math.random() + Math.random() * 10);
				}
					// create new observation
				UncertaintyObservation newObs = new UncertaintyObservation(
					obs.getPhenomenonTime(),
					obs.getResultTime(),
					obs.getProcedure(),
					obs.getObservedProperty(),
						obs.getFeatureOfInterest(), new UncertaintyResult(newDist));
				newObs.getResult().setUnitOfMeasurement("ug m-3");
				dummy.addObservation(newObs);
			}

			new StaxObservationEncoder().encodeObservationCollection(dummy,
					new File(omDummyPath));
			new JSONObservationEncoder().encodeObservationCollection(dummy,
					new File(omDummyPath.replace("xml", "json")));
//			new CSVEncoder().encodeObservationCollection(dummy, new File(
//					omDummyPath.replace("xml", "csv")));
		}


	// method to create a dummy output with WP6/7 data
	private static void createDummyTrajectoryOMColl(String omTemplatePath,
			String omDummyPath) throws OMEncodingException {
		IObservationCollection template = null;
		try {
			XmlObject xml = XmlObject.Factory.parse(new FileInputStream(
					omTemplatePath));
			template = (IObservationCollection) new XBObservationParser()
					.parse(xml.xmlText());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OMParsingException e) {
			e.printStackTrace();
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		UncertaintyObservationCollection dummy = new UncertaintyObservationCollection();

		// go through observations
		for (AbstractObservation obs : template.getObservations()) {
			// only copy one observation per location
			if (obs.getObservedProperty().getPath().contains("actionNumber")) {

				// pick a random date for the dummy data
				DateTime dt = ISODateTimeFormat.dateTime().parseDateTime(
						"2012-07-30T00:00:00.000+02");

				// adapt phenomenon time
				TimeObject phenTime = obs.getPhenomenonTime();
				TimeObject newPhenTime = null;
				if (phenTime.isGeneralTime()) {
					IGeneralTime time = phenTime.getGeneralTime();
					if (time instanceof GeneralTimeInstant) {
						GeneralTimeInstant timeInstant = (GeneralTimeInstant) time;

						// check which day the observation took place
						int day = timeInstant.getDay();
						String dtDay = dt.dayOfWeek().getAsText(
								new Locale("en"));
						dt = dt.plusDays(day
								- WeekdayMapping.DAY2INTEGER_EN.get(dtDay));

						dt = dt.plusHours(timeInstant.getHour());
						dt = dt.plusMinutes(timeInstant.getMinute());
						newPhenTime = new TimeObject(dt);
					} else if (time instanceof GeneralTimeInterval) {
						GeneralTimeInterval timeInterval = (GeneralTimeInterval) time;

						int day = timeInterval.getStart().getDay();
						String dtDay = dt.dayOfWeek().getAsText(
								new Locale("en"));
						dt = dt.plusDays(day
								- WeekdayMapping.DAY2INTEGER_EN.get(dtDay));
						dt = dt.plusHours(timeInterval.getStart().getHour());
						dt = dt.plusMinutes(timeInterval.getStart().getMinute());

						DateTime dt2 = ISODateTimeFormat.dateTime()
								.parseDateTime("2012-07-30T00:00:00.000+02");
						int day2 = timeInterval.getEnd().getDay();
						String dtDay2 = dt2.dayOfWeek().getAsText(
								new Locale("en"));
						// if start day was a sunday
						if (day > day2 && day2 == 1) {
							dt2 = dt2.plusDays(7);
						} else {
							dt2 = dt2
									.plusDays(day2
											- WeekdayMapping.DAY2INTEGER_EN
													.get(dtDay2));
						}

						dt2 = dt2.plusHours(timeInterval.getEnd().getHour());
						dt2 = dt2
								.plusMinutes(timeInterval.getEnd().getMinute());
						try {
							newPhenTime = new TimeObject(dt, dt2);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				// create new observation
				try {
					UncertaintyObservation newObs = new UncertaintyObservation(
							newPhenTime,
							obs.getResultTime(),
							obs.getProcedure(),
							new URI("http://www.uncertweb.org/phenomenon/pm10"),
							obs.getFeatureOfInterest(), new UncertaintyResult(
									new NormalDistribution(
											Math.random() * 10 + 20, Math
													.random() * 10 + 5)));
					Geometry tmp = newObs.getFeatureOfInterest().getShape();
					tmp.setSRID(4326);
					newObs.getFeatureOfInterest().setShape(tmp);
					dummy.addObservation(newObs);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}

		new StaxObservationEncoder().encodeObservationCollection(dummy,
				new File(omDummyPath));
		new JSONObservationEncoder().encodeObservationCollection(dummy,
				new File(omDummyPath.replace("xml", "json")));
		new CSVEncoder().encodeObservationCollection(dummy, new File(
				omDummyPath.replace("xml", "csv")));

	}

	private static void testEMSalgorithm() {
		/*
		 * 1) get inputs
		 */
		// define inputs
		List<IObservationCollection> omList = new ArrayList<IObservationCollection>();

		// WP8.3
// 		String ncFilePath = "D:/UncertWeb/WP8/D8.3/WP6_data/nox_dummy_8days.nc";
//		String omFilePath = "D:/UncertWeb/WP8/D8.3/WP7_data/albatross_output_new2.xml";
//		String resultFilePath = "D:/UncertWeb/WP8/D8.3/WP8.3_output.xml";
//		int minuteResolution = 60;

		// Muenster
		String ncFilePath = "C:/WebResources/AQMS/outputs/PM10/aqms_2010-04-01.nc";
		String omFilePath = "C:/WebResources/EMS/profiles/profile_p7.xml";
		String resultFilePath = "C:/WebResources/EMS/outputs/p7_PM10.xml";
		int minuteResolution = 10;

		// statistics
		List<String> statList = new ArrayList<String>();
		statList.add("mean");
		statList.add("standard-deviation");

		// read inputs
	//	IObservationCollection omFile = null;

		try {
			XmlObject xml = XmlObject.Factory.parse(new FileInputStream(
					omFilePath));
			omList.add((IObservationCollection) new XBObservationParser()
					.parse(xml.xmlText()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error while reading OM input: "
					+ e.getMessage(), e);
		}

		NcUwFile ncFile = null;
		try {
			ncFile = new NcUwFile(ncFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error while reading NetCDF input: "
					+ e.getMessage(), e);
		}







		/*
		 *  2) create internal data types
		 */
		// output collection
		UncertaintyObservationCollection exposureProfiles = new UncertaintyObservationCollection();

		// 2.1) get time from Netcdf file
		Variable timeVar = ncFile.getVariable(NcUwConstants.StandardNames.TIME);

		// if no time variable has been found
		if(timeVar==null){
			//TODO: throw exception
		}

		// create time list from Netcdf time array
		//TODO: ensure that this is done in UTC/GMT!!!
		ArrayList<DateTime> ncTimeList = new ArrayList<DateTime>();
		String timeUnit = timeVar.getUnitsString();
		try {
			DateUnit dateUnit = new DateUnit(timeUnit);
			for(int i=1; i<=timeVar.getDimension(0).getLength(); i++){
				ncTimeList.add(new DateTime(dateUnit.makeDate(i)));
			}

			// if minuteResolution has not been provided make this as default resolution
			if(minuteResolution==0){
				String unit = dateUnit.getTimeUnitString();
				if(unit.equals("days"))
					minuteResolution = 60*24;
				else if (unit.equals("hours"))
					minuteResolution = 60;
				else if (unit.equals("minutes"))
					minuteResolution = 1;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		for(IObservationCollection omFile : omList){
			// 2.2) create profile list from OM file
			List<AbstractProfile> profileList = new OMProfileParser().OM2Profiles(omFile, ncTimeList, minuteResolution);

			/*
			 *  3) check if modelling is possible
			 */



			/*
			 * 4) OUTDOOR MODEL
			 */
			// get outdoor concentration at profile locations
			// A) for the moment, do the overlay for GPS tracks in MS with the local version
			OutdoorModel outdoor = new OutdoorModel(resultFilePath);
			outdoor.run(profileList, ncFile);

			// perform averaging of profile observations
			// profile.aggregateProfile(minuteResolution);

			/*
			 * 5) INDOOR MODEL
			 */
			String parameter = ncFile.getStringAttribute(NcUwConstants.Attributes.PRIMARY_VARIABLES, true).split(" ")[0];
			String uom = ncFile.getVariable(parameter).findAttribute("units").getStringValue();
			// if activities are available, create indoor model with parameters
			// if(profile instanceof MEProfile || profile instanceof
			// ActivityProfile){
//			String parameter = ncFile.getPrimaryVariableNames().toArray(new String[1])[0];
			// IndoorModel indoor = new IndoorModel();
			// indoor.readParametersFile("DE",parameter,
			// "src/main/resources/indoorModel/parameters.csv");

			// indoor.runModel(profileList, indoorIterations, minuteResolution,
			// false);
			//
			// }

			/*
			 * RESULT COLLECTION
			 */
			// go through each individual profile
			for (AbstractProfile profile : profileList) {
				// get OM file and add to overall observation collection
				exposureProfiles.addObservationCollection(new OMProfileGenerator()
						.createExposureProfileObservationCollection(profile,
								statList));
			}
		}


		// write output locally
		try {
			new StaxObservationEncoder().encodeObservationCollection(exposureProfiles,
					new File(resultFilePath));
			new JSONObservationEncoder().encodeObservationCollection(exposureProfiles,
					new File(resultFilePath.replace("xml", "json")));
		} catch (OMEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
