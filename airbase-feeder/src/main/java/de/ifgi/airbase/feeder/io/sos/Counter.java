package de.ifgi.airbase.feeder.io.sos;

import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.io.filter.TimeRangeFilter;
import de.ifgi.airbase.feeder.util.Utils;

public class Counter extends SosClient {
	private static long count = 0;
	
	@Override
	public void registerStation(EEAStation station) {}

	@Override
	public void insertObservations(EEARawDataFile file) {
		TimeRangeFilter trf = Utils.getTimeRangeFilter();
		for (EEAMeasurement m : file.getMeasurements()) {
			if (m.isValid() && (trf == null || trf.accept(m))) {
                count++;
            }
		}
		log.info("Values: {}", count);
	}
}
