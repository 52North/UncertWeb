package de.ifgi.airbase.feeder.io.sos.http;

import de.ifgi.airbase.feeder.io.sos.http.xml.SoapRequestBuilder;
import de.ifgi.airbase.feeder.io.sos.http.xml.InsertResultTemplateRequestBuilder;
import de.ifgi.airbase.feeder.data.EEAConfiguration;
import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.io.sos.http.xml.EncodingException;
import de.ifgi.airbase.feeder.io.sos.http.xml.InsertResultRequestBuilder;
import de.ifgi.airbase.feeder.io.sos.http.xml.InsertSensorRequestBuilder;
import de.ifgi.airbase.feeder.io.sos.http.xml.SensorDescriptionBuilder;
import de.ifgi.airbase.feeder.util.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.opengis.sos.x20.InsertResultTemplateDocument;
import net.opengis.swes.x20.InsertSensorDocument;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class TransactionalSosV2Client extends AbstractTransactionalSosClient {

    private static final String INSERT_SENSOR_REQUEST_FILE_PREFIX = "InsertSensor-";
    private static final String INSERT_RESULT_REQUEST_FILE_PREFIX = "InsertResult-";
    private static final String INSERT_RESULT_TEMPLATE_REQUEST_FILE_PREFIX = "InsertResultTemplate-";

    @Override
    public void registerStation(EEAStation station) throws IOException, RequestFailedException {
        insertSensor(station);
        insertResultTemplates(station);
    }

    private void insertResultTemplates(EEAStation station) throws IOException, RequestFailedException {
        for (EEAConfiguration configuration : station.getConfigurations()) {
            if (!Utils.shouldBeIgnored(configuration.getComponentCode())) {
                insertResultTemplate(station, configuration);
            }
        }
    }

    private void insertResultTemplate(EEAStation station, EEAConfiguration configuration) throws IOException, RequestFailedException {
        try {
            long start = System.currentTimeMillis();

            InsertResultTemplateDocument doc = new InsertResultTemplateRequestBuilder()
                    .setConfiguration(configuration).setStation(station).build();
            String req = new SoapRequestBuilder().setBody(doc).asString();
            String buildTime = Utils.timeElapsed(start);
            InputStream in = request(req);
            log.info("Build InsertResulteTemplateRequest for {} in {}; SOS processed it in {}.", new Object[]{
                        station.getEuropeanCode(), buildTime, Utils.timeElapsed(start)});

            if (!processResponse(in)) {
                String file = printRequest(req,
                        getFailedPath(),
                        INSERT_RESULT_TEMPLATE_REQUEST_FILE_PREFIX,
                        station.getEuropeanCode(),"-",
                        String.valueOf(configuration.getComponentCode()),
                        ".xml");
                throw new RequestFailedException("Request '" + file + "'  failed.");
            }
        } catch (EncodingException ex) {
            log.error("Could not encoder InsertResultTemplate request", ex);
        }
    }

    private void insertSensor(EEAStation station) throws IOException, RequestFailedException {
        try {
            /*
             if (isAlreadyRegistered(station.getEuropeanCode())) {
             log.info("Station is already registered: {}" , station.getEuropeanCode());
             return;
             }
             */
            long start = System.currentTimeMillis();
            InsertSensorDocument insertSensorDocument;
            try {
                insertSensorDocument = new InsertSensorRequestBuilder().setStation(station)
                        .setDescription(new SensorDescriptionBuilder().setStation(station).build()).build();
            } catch (EncodingException e) {
                log.info("Station {} has no valid input/outputs.", station.getEuropeanCode());
                return;
            }
            String req = new SoapRequestBuilder().setBody(insertSensorDocument).asString();
            printRequest(req, getPath(), INSERT_SENSOR_REQUEST_FILE_PREFIX, station.getEuropeanCode(), ".xml");
            String buildTime = Utils.timeElapsed(start);
            InputStream in = request(req);
            log.info("Build RegisterSensor for {} in {}; SOS processed it in {}.", new Object[]{
                station.getEuropeanCode(), buildTime, Utils.timeElapsed(start)});
            if (!processResponse(in)) {
                String file = printRequest(req, getFailedPath(), INSERT_SENSOR_REQUEST_FILE_PREFIX,
                        station.getEuropeanCode(), ".xml");
                throw new RequestFailedException("Request '" + file + "'  failed.");
            }
        } catch (EncodingException ex) {
            log.error("Could not encode InsertSensor request", ex);
        }
    }

    @Override
    protected void insertObservations(int reqNo, EEARawDataFile file, List<EEAMeasurement> valuesToInsert) throws IOException, RequestFailedException {
        try {
            String req = new SoapRequestBuilder().setBody(
                    new InsertResultRequestBuilder().setValues(valuesToInsert)
                            .setFile(file).build()).asString();
            printRequest(req, getPath(), INSERT_RESULT_REQUEST_FILE_PREFIX, file.getStation()
                    .getEuropeanCode(), "-", String.valueOf(file.getConfiguration().getComponent()
                    .getCode()), "-", String.valueOf(reqNo), ".xml");
            long requestStart = System.currentTimeMillis();
            InputStream in = request(req);

            log.debug("Build InsertResult {} for Station {}; SOS processed it in {}.",
                      new Object[] {Integer.valueOf(reqNo),
                                    file.getStation().getEuropeanCode(),
                                    Utils.timeElapsed(requestStart)});

            if ( !processResponse(in)) {// && PRINT_FAILED_REQUESTS) {
                String f = printRequest(req, getFailedPath(), INSERT_RESULT_REQUEST_FILE_PREFIX,
                             file.getStation().getEuropeanCode(), "-",
                             String.valueOf(file.getConfiguration().getComponent().getCode()),
                             "-", String.valueOf(reqNo), ".xml");
                throw new RequestFailedException("Request '" + f + "'  failed.");
            }
        } catch (EncodingException ex) {
            log.error("Could not encode InsertResult request", ex);
        }
    }
}