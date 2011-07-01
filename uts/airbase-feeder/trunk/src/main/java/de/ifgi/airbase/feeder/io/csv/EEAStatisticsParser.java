package de.ifgi.airbase.feeder.io.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.data.EEAStatistic;
import de.ifgi.airbase.feeder.util.Utils;

import au.com.bytecode.opencsv.CSVReader;


class EEAStatisticsParser {

	private static final Logger log = LoggerFactory.getLogger(EEAStationParser.class);
	
	static Collection<EEAStatistic> parse(File file) throws IOException {
		CSVReader reader = null;
		try {
			long start = System.currentTimeMillis();
			LinkedList<EEAStatistic> stats = new LinkedList<EEAStatistic>();
			reader = new CSVReader(new FileReader(file), '\t',
					'"', 1);
			String[] line = null;
			while ((line = reader.readNext()) != null) {
				stats.add(parseStatisticLine(line));
			}
			log.info("Parsed {} statistics in {}.", Integer.valueOf(stats.size()),
					Utils.timeElapsed(start));
			return stats;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private static EEAStatistic parseStatisticLine(String[] line) {
		EEAStatistic stat = new EEAStatistic();
		stat.setStationEuropeanCode(line[0]);
		stat.setComponentCode(Integer.parseInt(line[1]));
		stat.setComponentName(line[2]);
		stat.setComponentCaption(line[3]);
		stat.setMeasurementUnit(line[4]);
		stat.setMeasurementEuropeanGroupCode(line[5]);
		stat.setStatisticsPeriod(line[6]);
		stat.setStatisticsYear(Integer.parseInt(line[7]));
		stat.setStatisticsAverageGroup(line[8]);
		stat.setStatisticShortname(line[9]);
		stat.setStatisticName(line[10]);
		stat.setStatisticValue(Float.parseFloat(line[11]));
		stat.setStatisticsPercentageValid(Float.parseFloat(line[12]));
		stat.setStatisticsNumberValid(Integer.parseInt(line[13]));
		stat.setStatisticsCalculated(line[14].equalsIgnoreCase("Y"));
		return stat;
	}

}
