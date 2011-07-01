package de.ifgi.airbase.feeder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.zip.ZipException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.io.csv.EEAParser;
import de.ifgi.airbase.feeder.io.filter.FileExtensionFilter;
import de.ifgi.airbase.feeder.io.sos.SosClient;
import de.ifgi.airbase.feeder.io.zip.Unzipper;
import de.ifgi.airbase.feeder.util.Utils;

/**
 * 
 * @author Christian Autermann
 * 
 */
public class Feeder {
	private static final Logger log = LoggerFactory.getLogger(Feeder.class);
	private static final String REGISTER_STATIONS_PROPERTY = "eea.registerStations";
	private static final boolean REGISTER_STATIONS = Boolean.parseBoolean(Utils.get(REGISTER_STATIONS_PROPERTY));
	private static final String REGISTER_OBSERVATIONS_PROPERTY = "eea.registerObservations";
	private static final boolean REGISTER_OBSERVATIONS = Boolean.parseBoolean(Utils.get(REGISTER_OBSERVATIONS_PROPERTY));
	
	protected void process(String fileName) {
		long start = System.currentTimeMillis();
		try {
			File f = new File(fileName);
			if (f.isDirectory()) {
				for (File f2 : f.listFiles(new FileExtensionFilter("zip"))) {
					processFile(f2);
				}
			} else if (f.isFile()) {
				processFile(f);
			} else {
				throw new Error(fileName + " is neither a directory nor a normal file... what is it?");
			}
			File failed = new File(Utils.getFailedRequestPrintPath());
	        if (failed.exists() && failed.isDirectory()) {
	        	File[] files = failed.listFiles(new FileExtensionFilter(".xml"));
	        	if (files.length != 0) {
	        		log.info("Processing {} failed Requests.", files.length);
	        		StaticFileFeeder.processDirectory(failed);
	        		int size = failed.listFiles(new FileExtensionFilter(".xml")).length;
	        		if (size != 0) {
	        			log.info("Still {} failed requests in '{}'.",size,failed.getAbsolutePath());
	        		}
	        	}
	        }
		} catch (Exception e) {
			log.warn("Unexpected Exception.", e);
		} finally {
			log.info("Processed everything in {}.", Utils.timeElapsed(start));
		}
	}

	protected void processFile(File f) {
		long fileStartTime = System.currentTimeMillis();
		try {
			SosClient sos = SosClient.newInstance();
			Unzipper uz = new Unzipper(f);
			EEAParser p = new EEAParser(uz.getStationsFile(),
										uz.getRawDataDirectory(), 
										uz.getStatisticsFile(),
										uz.getConfigurationFile());
			for (EEAStation station : p.getStations()) {
				if (REGISTER_STATIONS) {
					sos.registerStation(station);
				}
				if (REGISTER_OBSERVATIONS) {
					for (EEARawDataFile file : p.getDataByStation(station)) {
						sos.insertObservations(file);
					}
				}
			}
			sos = null;
			uz = null;
			p = null;
		} catch (ZipException e) {
			log.warn("Unable to unpack zip file.", e);
		} catch (IOException e) {
			log.warn("Unexpected IO error.", e);
		} finally {
			log.info("Processed {} in {}.", f.getName(),
					Utils.timeElapsed(fileStartTime));
		}
	}

	/**
	 * @param args
	 *            arguments for {@code Eea2Sos}
	 * @throws MalformedURLException
	 *             if given URL is not valid.
	 */
	public static void main(String[] args) throws MalformedURLException {
		Feeder eea2sos = new Feeder();
		if (args.length == 1) {
			eea2sos.process(args[0]);
		} else {
			System.exit(1);
		}
	}
}
