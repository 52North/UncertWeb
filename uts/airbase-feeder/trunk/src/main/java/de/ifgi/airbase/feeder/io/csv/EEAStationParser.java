package de.ifgi.airbase.feeder.io.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.Utils;

class EEAStationParser {

	private static final Logger log = LoggerFactory
			.getLogger(EEAStationParser.class);

	static Map<String, EEAStation> parse(File file) throws IOException {
		CSVReader reader = null;
		Map<String, EEAStation> stations = new HashMap<String, EEAStation>();
		try {
			long start = System.currentTimeMillis();
			reader = new CSVReader(new FileReader(file), '\t', '"', 1);
			String[] line;
			while ((line = reader.readNext()) != null) {
				EEAStation s = parseStationLine(line);
				if (Utils.isAcceptableStation(s.getEuropeanCode())) {
					stations.put(s.getEuropeanCode(), s);
				}
			}
			log.info("Parsed {} stations in {}.", Integer.valueOf(stations.size()),
					Utils.timeElapsed(start));
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return stations;
	}

	private static EEAStation parseStationLine(String[] line) {
		EEAStation station = new EEAStation();
		station.setEuropeanCode(line[0]);
		station.setLocalCode(line[1]);
		station.setCountryIsoCode(line[2]);
		station.setCountryName(line[3]);
		station.setName(line[4]);
		station.setStartDate(line[5]);
		station.setEndDate(line[6]);
		station.setType(line[7]);
		station.setOzoneClassification(line[8]);
		station.setTypeOfArea(line[9]);
		station.setSubcatRuralBack(line[10]);
		station.setStreetType(line[11]);
		station.setLongitudeDeg(line[12]);
		station.setLatitudeDeg(line[13]);
		station.setAltitude(line[14]);
		station.setCity(line[15]);
		station.setLauLevel1Code(line[16]);
		station.setLauLevel2Code(line[17]);
		station.setLauLevel2Name(line[18]);
		station.setEMEPStation(line[19]);
		return station;
	}
}
