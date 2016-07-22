package org.uncertweb.ems.test;

import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.netcdf.NcUwFile;

import ucar.nc2.units.DateUnit;

public class Utils {

//	public static ArrayList<DateTime> createTimeList(String start, int length){
//		ArrayList<DateTime> ncTimeList = new ArrayList<DateTime>();
//		DateUnit dateUnit;
//		try {
//			dateUnit = new DateUnit(start);
//			for(int i=1; i<=length; i++){
//				ncTimeList.add(new DateTime(dateUnit.makeDate(i)));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return ncTimeList;
//	}
//	
//	public static IObservationCollection getOMfile(String omFilePath){
//		IObservationCollection obsColl = null;
//		try {
//			XmlObject xml = XmlObject.Factory.parse(new FileInputStream(
//					omFilePath));
//			obsColl = (IObservationCollection) new XBObservationParser()
//					.parse(xml.xmlText());
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new RuntimeException("Error while reading OM input: "
//					+ e.getMessage(), e);
//		}
//		
//		return obsColl;		
//	}
//	
//	public static NcUwFile getNetCDFUfile(String ncFilePath){	
//		NcUwFile ncFile = null;
//
//		try {
//			ncFile = new NcUwFile(ncFilePath);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new RuntimeException("Error while reading NetCDF input: "
//					+ e.getMessage(), e);
//		}
//		
//		return ncFile;		
//	}
}
