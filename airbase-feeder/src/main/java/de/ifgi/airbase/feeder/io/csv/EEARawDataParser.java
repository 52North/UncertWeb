package de.ifgi.airbase.feeder.io.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.joda.time.MutableDateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.data.EEAConfiguration;
import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEAMeasurementType;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.Utils;

import au.com.bytecode.opencsv.CSVReader;


class EEARawDataParser {

	private static final Logger log = LoggerFactory.getLogger(EEARawDataParser.class);
	private File[] files;


	static EEAMeasurementType getTypeByFileName(String name) {
		return EEAMeasurementType.getValue(name.split("\\.")[0]
				.substring(17));
	}

	EEARawDataParser(File directory) {
		this.files = directory.listFiles(Utils.getRawDataFileFilter());
	}

	LinkedList<File> getAllFilesByStation(EEAStation station) {
		LinkedList<File> stationFiles = new LinkedList<File>();
		/* faster then performing a directory listing for every station */
		for (File file : this.files) {
			if (file.getName().startsWith(station.getEuropeanCode())) {
				stationFiles.add(file);
			}
		}
		return stationFiles;
	}

	Collection<EEARawDataFile> parse(EEAStation station) throws IOException {
		ArrayList<EEARawDataFile> fileList = new ArrayList<EEARawDataFile>();
		final long start = System.currentTimeMillis();
		Collection<File> datasets = getAllFilesByStation(station);
		for (File f : getAllFilesByStation(station)) {
			EEARawDataFile data = parseFile(f, station);
			if (data.getConfiguration() == null) {
				log.warn("No Configuration for \"{}\" found. Skipping.",
						f.getName());
			} else {
				fileList.add(data);
			}
		}
		log.info("Parsed {} data files for station {} in {}.",
				new Object[] { Integer.valueOf(datasets.size()), station.getEuropeanCode(),
						Utils.timeElapsed(start) });
		return fileList;
	}

	static EEARawDataFile parseFile(File f, EEAStation station) throws IOException {
		log.debug("Parsing {}", f.getName());
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(f), '\t');
			String[] line = null;
			EEARawDataFile file = parseFileName(f, station);
			while ((line = reader.readNext()) != null) {
				Period period = file.getType().getPeriod();
				MutableDateTime lineStart = Utils.parseDateReverse(line[0]).toMutableDateTime();
				for (int i = 1; i < line.length; i += 2) {
					EEAMeasurement m = new EEAMeasurement(
							Double.parseDouble(line[i]),
							Integer.parseInt(line[i + 1]),
							lineStart.toDateTime());
					file.addMeasurement(m);
					lineStart.add(period);
				}
			}
			reader.close();
			return file;
		} finally {
			if (reader != null) {
				reader.close();
			}
			reader = null;
		}
	}

	static EEARawDataFile parseFileName(File f, EEAStation station) {
		EEARawDataFile file = new EEARawDataFile();
		file.setFileName(f.getName());
		String fileName = f.getName();
		String split[] = fileName.split("\\.");
		file.setStartDate(Utils.parseDate(split[1]));
		file.setEndDate(Utils.parseDate(split[2]));
		int componentId = Integer.parseInt(split[0].substring(7, 12));
		String stationId = split[0].substring(0, 6);
		if (stationId.equals(station.getEuropeanCode())) {
			throw new Error();
		}
		int groupCode = Integer.parseInt(split[0].substring(12, 17));
		EEAConfiguration config = station.getConfigurationsByComponentAndMeasurment(componentId, groupCode);
		file.setStation(station);
		file.setConfiguration(config);
		file.setMeasurementEuropeanGroupCode(groupCode);
		file.setType(getTypeByFileName(fileName));
		return file;
	}

}