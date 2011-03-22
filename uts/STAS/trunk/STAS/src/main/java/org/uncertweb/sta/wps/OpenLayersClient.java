package org.uncertweb.sta.wps;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.opengis.sos.x10.GetObservationDocument;

import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;

public class OpenLayersClient {

	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory
			.getLogger(OpenLayersClient.class);

	/**
	 * The directory that contains the OpenLayers client.
	 */
	private static final String OLC_PATH = Constants.get("stas.olc.path");

	/**
	 * The directory name in which GetObservation requests are saved.
	 */
	private static final String OLC_REQUEST_SAVE_DIRECTORY = Constants
			.get("stas.olc.requestPath");

	/**
	 * Parameter for the SOS URL.
	 */
	private static final String OLC_URL_PARAMETER = Constants
			.get("stas.olc.param.url");

	/**
	 * Parameter for the SOS request.
	 */
	private static final String OLC_REQUEST_PARAMETER = Constants
			.get("stas.olc.param.request");

	/**
	 * The singleton of this class.
	 */
	private static OpenLayersClient singleton;

	/**
	 * @return the singleton of this class.
	 */
	public static OpenLayersClient getInstance() {
		if (singleton == null) {
			singleton = new OpenLayersClient();
		}
		return singleton;
	}

	/**
	 * Private due singleton pattern.
	 */
	private OpenLayersClient() {}

	/**
	 * The directory in which {@link GetObservationDocument} request are saved.
	 */
	private File requestDirectory = null;

	/**
	 * The URL of the integrated OpenLayers Client.
	 */
	private String baseUrl = null;

	/**
	 * Saves a {@link GetObservationDocument} request and returns a link to the
	 * integrated OpenLayers client visualizing the aggregated observations.
	 * 
	 * @param url the URL of the source SOS
	 * @param getObsDoc the request
	 * @return the URL of the OpenLayers client
	 * @throws IOException if an IO error occurs
	 */
	public String getUrlForRequest(String process, String url,
			GetObservationDocument getObsDoc) throws IOException {
		if (baseUrl == null) {
			Server server = WPSConfig.getInstance().getWPSConfig().getServer();
			baseUrl = "http://" + server.getHostname() + ":"
					+ server.getHostport() + "/" + server.getWebappPath() + "/";
		}
		String requestPath = saveGetObservationRequest(process, getObsDoc);
		Map<String, String> params = new HashMap<String, String>();
		params.put(OLC_URL_PARAMETER, url);
		params.put(OLC_REQUEST_PARAMETER, baseUrl + requestPath);
		return Utils.buildGetRequest(baseUrl + OLC_PATH, params);
	}

	/**
	 * Saves a {@link GetObservationDocument} request. The process id is used as
	 * the filename.
	 * 
	 * @param getObsDoc the request
	 * @return the path relative to the STAS' root
	 * @throws IOException if an IO error occurs
	 */
	protected String saveGetObservationRequest(String process,
			GetObservationDocument getObsDoc) throws IOException {
		if (requestDirectory == null) {
			String domain = Constants.class.getProtectionDomain()
					.getCodeSource().getLocation().getFile();
			int index = domain.indexOf("WEB-INF");
			String parentDir = null;
			if (index < 0) {
				log.warn("Could not find web app directory. Using java.io.tmpdir.");
				parentDir = System.getProperty("java.io.tmpdir");
			} else {
				parentDir = domain.substring(0, index);
			}

			if (!parentDir.endsWith(File.separator)) {
				parentDir = parentDir + File.separator;
			}

			String dirName = parentDir + OLC_REQUEST_SAVE_DIRECTORY;
			File reqDir = new File(dirName);
			if (!reqDir.exists() && !reqDir.mkdir()) {
				throw new RuntimeException(
						"Could not create request directory: " + dirName);
			} else if (!reqDir.isDirectory()) {
				throw new RuntimeException(
						"Request directory is not a directory: " + dirName);
			} else if (!reqDir.canWrite()) {
				throw new RuntimeException(
						"Request directory is not writeable: " + dirName);
			} else if (!reqDir.canRead()) {
				throw new RuntimeException(
						"Request directory is not readable: " + dirName);
			} else {
				requestDirectory = reqDir;
			}
		}

		File f = new File(requestDirectory.getPath() + File.separator + process
				+ ".xml");
		if (!f.createNewFile()) {
			throw new RuntimeException("Could not create new file: "
					+ f.getAbsolutePath());
		}
		getObsDoc.save(f, Namespace.defaultOptions());
		f.deleteOnExit();
		return OLC_REQUEST_SAVE_DIRECTORY + "/" + f.getName();
	}

}
