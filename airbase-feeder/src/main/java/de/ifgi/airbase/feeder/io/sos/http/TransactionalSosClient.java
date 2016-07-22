
package de.ifgi.airbase.feeder.io.sos.http;

import de.ifgi.airbase.feeder.io.sos.http.xml.InsertObservationRequestBuilder;
import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.io.sos.http.xml.EncodingException;
import de.ifgi.airbase.feeder.io.sos.http.xml.RegisterSensorRequestBuilder;
import de.ifgi.airbase.feeder.io.sos.http.xml.SensorDescriptionBuilder;
import de.ifgi.airbase.feeder.util.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A class to register {@link EEAStation}s and insert {@link EEARawDataFile} to a SOS.
 *
 * @author Christian Autermann
 *
 */
public class TransactionalSosClient extends AbstractTransactionalSosClient {

    private static final String REGISTER_SENSOR_REQUEST_FILE_PREFIX = "RegisterSensor-";
    private static final String INSERT_OBSERVATION_REQUEST_FILE_PREFIX = "InsertObservation-";


    /*
     * (non-Javadoc)
     *
     * @see de.ifgi.airbase.feeder.io.sos.http.SosClient#registerStation(de.ifgi.
     * airbase.feeder.data.EEAStation)
     */
    @Override
    public void registerStation(EEAStation station) throws IOException {
    	/*
		if (isAlreadyRegistered(station.getEuropeanCode())) {
    		log.info("Station is already registered: {}" , station.getEuropeanCode());
    		return;
    	}
		*/
        long start = System.currentTimeMillis();
        String req;
		try {
            req = new RegisterSensorRequestBuilder().setDescription(new SensorDescriptionBuilder().setStation(station).build()).asString();
		} catch (EncodingException e) {
			log.info("Station {} has no valid input/outputs.",station.getEuropeanCode());
			return;
		}
        printRequest(req, getPath(), REGISTER_SENSOR_REQUEST_FILE_PREFIX, station.getEuropeanCode(), ".xml");
        String buildTime = Utils.timeElapsed(start);
        InputStream in = request(req);
        log.info("Build RegisterSensor for {} in {}; SOS processed it in {}.",
                new Object[] {station.getEuropeanCode(), buildTime, Utils.timeElapsed(start)});

        if ( !processResponse(in)) {
            printRequest(req, getFailedPath(), REGISTER_SENSOR_REQUEST_FILE_PREFIX,
                    station.getEuropeanCode(), ".xml");
        }
    }

    @Override
    protected void insertObservations(int reqNo, EEARawDataFile file, List<EEAMeasurement> valuesToInsert) throws IOException, RequestFailedException {
        try {
            String req = new InsertObservationRequestBuilder()
                   .setValues(valuesToInsert).setFile(file).asString();
            printRequest(req, getPath(), INSERT_OBSERVATION_REQUEST_FILE_PREFIX,
                         file.getStation().getEuropeanCode(), "-",
                         String.valueOf(file.getConfiguration().getComponent().getCode()),
                         "-", String.valueOf(reqNo), ".xml");
            long requestStart = System.currentTimeMillis();
            InputStream in = request(req);
            log.debug("Build InsertObservation {} for Station {}; SOS processed it in {}.",
                      new Object[] {Integer.valueOf(reqNo),
                                    file.getStation().getEuropeanCode(),
                                    Utils.timeElapsed(requestStart)});

            if ( !processResponse(in)) {// && PRINT_FAILED_REQUESTS) {
                printRequest(req, getFailedPath(), INSERT_OBSERVATION_REQUEST_FILE_PREFIX,
                        file.getStation().getEuropeanCode(), "-",
                        String.valueOf(file.getConfiguration().getComponent().getCode()),
                        "-", String.valueOf(reqNo), ".xml");
            }
        } catch (EncodingException ex) {
            log.error("Could not encode InsertObservation request", ex);
        }
    }
}
