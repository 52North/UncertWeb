
package de.ifgi.airbase.feeder.io.sos.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;
import net.opengis.sos.x10.InsertObservationDocument;
import net.opengis.sos.x10.InsertObservationResponseDocument;
import net.opengis.sos.x10.RegisterSensorDocument;
import net.opengis.sos.x10.RegisterSensorResponseDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.io.filter.TimeRangeFilter;
import de.ifgi.airbase.feeder.io.sos.SosClient;
import de.ifgi.airbase.feeder.util.Utils;

/**
 * A class to register {@link EEAStation}s and insert {@link EEARawDataFile} to a SOS.
 * 
 * @author Christian Autermann
 * 
 */
public abstract class TransactionalSosClient extends SosClient {
    private static final String SOS_PRINT_PATH_PROPERTY = "eea.sosRequestPrintPath";
    private static final String VALUES_PER_REQUEST_PROPERTY = "eea.valuesProRequest";
    private static final String SOS_URL_PROPERTY = "eea.sosTransactionalUrl";

    private static final String REGISTER_SENSOR_REQUEST_FILE_PREFIX = "RegisterSensor-";
    private static final String INSERT_OBSERVATION_REQUEST_FILE_PREFIX = "InsertObservation-";

    private static final int VALUES_PER_REQUEST = Integer.parseInt(Utils.get(VALUES_PER_REQUEST_PROPERTY));
    private URL url = null;
    private String path = null;
    private String failedPath = Utils.getFailedRequestPrintPath();
    
    public URL getUrl() {
        if (this.url == null) {
            String urlProp = Utils.get(SOS_URL_PROPERTY);
            if (urlProp == null) {
                throw new NullPointerException();
            }
            try {
                this.url = new URL(urlProp);
            }
            catch (MalformedURLException e) {
                throw new Error(e);
            }
        }
        return this.url;
    }

    private String getPath() {
        if (this.path != null) {
            String p = Utils.get(SOS_PRINT_PATH_PROPERTY);
            if (p != null) {
                File f = new File(p);
                if ( !f.exists()) {
                    f.mkdirs();
                    this.path = p;
                }
            }
        }
        return this.path;
    }

    protected abstract InputStream post(String xml) throws IOException;

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
        RegisterSensorDocument regSenDoc = null;
		try {
			regSenDoc = RegisterSensorRequestBuilder.buildRegisterSensor(station);
		} catch (NoValidInputsOrOutputsException e) {
			log.info("Station {} has no valid input/outputs.",station.getEuropeanCode());
			return;
		}
        String req = regSenDoc.toString();
        printRequest(req, getPath(), REGISTER_SENSOR_REQUEST_FILE_PREFIX, station.getEuropeanCode(), ".xml");
        String buildTime = Utils.timeElapsed(start);
        InputStream in;

        try {
            while ((in = post(req)) == null)
                log.warn("Connection failed. Trying again.");

        }
        catch (RuntimeException e) {
            log.error("Could not send station: ", e.getMessage());
            return;
        }

        log.info("Build RegisterSensor for {} in {}; SOS processed it in {}.", new Object[] {station.getEuropeanCode(),
                                                                                             buildTime,
                                                                                             Utils.timeElapsed(start)});

        if ( !processResponse(in)) {
            printRequest(req,
                         this.failedPath,
                         REGISTER_SENSOR_REQUEST_FILE_PREFIX,
                         station.getEuropeanCode(),
                         ".xml");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ifgi.airbase.feeder.io.sos.http.SosClient#insertObservations(de.ifgi
     * .airbase.feeder.data.EEARawDataFile)
     */
    @Override
    public void insertObservations(EEARawDataFile file) throws IOException {
        long start = System.currentTimeMillis();
        int index = 0, requestCount = 0;
        TimeRangeFilter trf = Utils.getTimeRangeFilter();
        List<EEAMeasurement> allMeasurements = file.getMeasurements();
        while (index < allMeasurements.size()) {
            long buildingStart = System.currentTimeMillis();
            List<EEAMeasurement> valuesToInsert = new LinkedList<EEAMeasurement>();
            while (valuesToInsert.size() < VALUES_PER_REQUEST && index < allMeasurements.size()) {
                EEAMeasurement e = allMeasurements.get(index);
                if (e.isValid() && (trf == null || trf.accept(e))) {
                    valuesToInsert.add(e);
                }
                index++;
            }

            if (!valuesToInsert.isEmpty()) {
                InsertObservationDocument iod = InsertObservationRequestBuilder.buildInsertObservationRequest(file,
                                                                                                              valuesToInsert);
                String req = iod.toString();
                printRequest(req,
                             getPath(),
                             INSERT_OBSERVATION_REQUEST_FILE_PREFIX,
                             file.getStation().getEuropeanCode(),
                             "-",
                             String.valueOf(file.getConfiguration().getComponent().getCode()),
                             "-",
                             String.valueOf(requestCount),
                             ".xml");
                String buildTime = Utils.timeElapsed(buildingStart);
                long requestStart = System.currentTimeMillis();
                InputStream in;
                while ( (in = post(req)) == null) {
                    log.warn("Connection failed. Trying again.");
                }
                log.debug("Build InsertObservation {} for Station {} in {}; SOS processed it in {}.",
                          new Object[] {Integer.valueOf(requestCount),
                                        file.getStation().getEuropeanCode(),
                                        buildTime,
                                        Utils.timeElapsed(requestStart)});

                if ( !processResponse(in)) {// && PRINT_FAILED_REQUESTS) {
                    printRequest(req,
                                 this.failedPath,
                                 INSERT_OBSERVATION_REQUEST_FILE_PREFIX,
                                 file.getStation().getEuropeanCode(),
                                 "-",
                                 String.valueOf(file.getConfiguration().getComponent().getCode()),
                                 "-",
                                 String.valueOf(requestCount),
                                 ".xml");
                }
            }
            requestCount++;
        }
        log.info("Processed {} in {}", file.getFileName(), Utils.timeElapsed(start));

    }

    private void printRequest(String string, String p, String... filenameComponents) {
        if (p != null) {
            BufferedWriter bw = null;
            try {
                StringBuilder sb = new StringBuilder().append(p).append(File.separator);
                for (String s : filenameComponents)
                    sb.append(s);
                String fileName = sb.toString();
                log.info("Writing request to file {}", fileName);
                bw = new BufferedWriter(new FileWriter(new File(fileName)));
                bw.append(string);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (bw != null) {
                    try {
                        bw.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected static boolean processResponse(InputStream response) throws IOException {
        try {
            XmlObject xml = XmlObject.Factory.parse(response);

            if (xml instanceof RegisterSensorResponseDocument) {
                log.info("Registered station; assigned Id: {}",
                         ((RegisterSensorResponseDocument) xml).getRegisterSensorResponse().getAssignedSensorId());
                return true;
            }

            else if (xml instanceof InsertObservationResponseDocument) {
                log.info("Inserted Observation; assigned Id: {}",
                         ((InsertObservationResponseDocument) xml).getInsertObservationResponse().getAssignedObservationId());
                return true;
            }

            else if (xml instanceof ExceptionReportDocument) {
                StringBuilder sb = new StringBuilder();
                for (ExceptionType et : ((ExceptionReportDocument) xml).getExceptionReport().getExceptionArray()) {
                    for (String s : et.getExceptionTextArray()) {
                    	if (s.matches("Sensor with ID: .* is already registered at this SOS!")) {
                    		log.info("Station already registered.");
                    		return true;
                    	}
                    	if (s.startsWith("FEHLER: doppelter Schlüsselwert verletzt Unique-Constraint")) {
                    		log.info("Observation already registered.");
                    		return true;
                    	}
                        sb.append("\t").append(s);
                    }
                }
                log.warn("Request failed: {}", sb.toString());
                return false;
            }

            else {
                log.warn("Unable to read Response:\n{}", xml);
                return false;
            }

        }
        catch (XmlException ex) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(response));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ( (line = br.readLine()) != null) {
                    sb.append("\n").append(line);
                }
                log.warn("Unable to read Response: {}", sb.toString());
            }
            finally {
                if (br != null)
                    br.close();
            }
            return false;
        }
        finally {
            if (response != null) {
                response.close();
            }
        }
    }
    /*
    private static HashSet<String> registeredProcedures;
    
    private static boolean isAlreadyRegistered(String procedure) {
		if (registeredProcedures == null) {
			registeredProcedures = new HashSet<String>();
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(TransactionalSosClient.class
						.getResourceAsStream("/procedures.txt")));
				String line = null;
				while ((line = in.readLine()) != null) {
					registeredProcedures.add(line.trim());
				}
			} catch (IOException e) {
			} finally {
				try {
					if (in != null) in.close();
				} catch (IOException e) {}
			}
		}
		return registeredProcedures.contains(procedure);
	}
	*/
}
