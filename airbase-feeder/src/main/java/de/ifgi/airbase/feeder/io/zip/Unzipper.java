package de.ifgi.airbase.feeder.io.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.util.Utils;

/**
 * Class to unzip the country specific data files.
 *
 * @author Christian Autermann
 *
 */
public class Unzipper {
	private static final String AIRBASE_FILE_PREFIX = "AirBase";

	//private static final String AIRBASE_FILE_VERSION = "v4";
	//TODO needs to be updated to newer version
	private static final String AIRBASE_FILE_VERSION = "v4";
	private static final String AIRBASE_FILE_SEPERATOR = "_";
	private static final String AIRBASE_STATISTICS_FILE_SUFFIX = "statistics.csv";
	private static final String AIRBASE_STATIONS_FILE_SUFFIX = "stations.csv";
	private static final String AIRBASE_RAWDATA_FILE_SUFFIX = "rawdata.zip";
	private static final String AIRBASE_RAW_DATA_DIRECTORY_NAME = "raw";
	private static final String AIRBASE_XML_FILE_SUFFIX = "xmldata.zip";
	private static final String AIRBASE_CONFIG_FILE_SUFFIX = "measurement_configurations.csv";

	private static final Logger log = LoggerFactory.getLogger(Unzipper.class);
	private static final int BUFFER_SIZE = 16384;
	private boolean unzippedMain = false;
	private File directory;
	private String country;
	private File zipFile;

	/**
	 * Creates a new {@code Unzipper}.
	 *
	 * @param zipFile
	 *            the {@code File} that contains the data.
	 * @throws ZipException
	 *             if a zip error occurs
	 * @throws IOException
	 *             if a IO error occurs
	 */
	public Unzipper(File zipFile) throws ZipException, IOException {
		this.zipFile = zipFile;
		parseCountry();
		unzip();
	}

	private void parseCountry() {
		int begin = AIRBASE_FILE_PREFIX.length()
				+ AIRBASE_FILE_SEPERATOR.length();
		int end = begin + 2;
		this.country = this.zipFile.getName().substring(begin, end).toLowerCase();
	}

	private void unzip() throws ZipException, IOException {
		if (this.unzippedMain)
			return;

		this.unzippedMain = true;

		long start = System.currentTimeMillis();
		this.directory = new File(this.zipFile.getParent() + File.separator + this.country);
		log.info("Unzipping from \"{}\" to \"{}\"", this.zipFile.getName(),
				this.directory);
		if (this.directory.exists()) {
			log.warn("Directory exits. Skipping");
			return;
		}
		// extract the complete dataset
		extractArchive(this.zipFile, this.directory);
		// extract the raw data files
		File rawZip = getRawDataFile();
		if (rawZip != null)
			extractArchive(rawZip, getRawDataDirectory());
		log.debug("Unzipped {}MB in {}. Files:\n\tConfig:\t\t{}\n\tStations:\t{}\n\tStatistics:\t{}\n\tXMLData:\t{}\n\tData Directory:\t{}",
				new Object[] { Long.valueOf((this.zipFile.length()) / (1024 * 1024)),
						Utils.timeElapsed(start), getConfigurationFile(),
						getStationsFile(), getStatisticsFile(),
						getXMLDataFile(), getRawDataDirectory() });
	}

	private File getRawDataFile() {
		return getFile(AIRBASE_RAWDATA_FILE_SUFFIX);
	}

	/**
	 * @return the directory containing the raw data files
	 */
	public File getRawDataDirectory() {
		return new File(getPath() + AIRBASE_RAW_DATA_DIRECTORY_NAME);
	}

	/**
	 * @return the statistic file
	 */
	public File getStatisticsFile() {
		return getFile(AIRBASE_STATISTICS_FILE_SUFFIX);
	}

	/**
	 * @return the stations file
	 */
	public File getStationsFile() {
		return getFile(AIRBASE_STATIONS_FILE_SUFFIX);
	}

	/**
	 * @return the measurement configuration file
	 */
	public File getConfigurationFile() {
		return getFile(AIRBASE_CONFIG_FILE_SUFFIX);
	}

	/**
	 * @return the xml meta data file
	 */
	public File getXMLDataFile() {
		return getFile(AIRBASE_XML_FILE_SUFFIX);
	}

	private File getFile(String ext) {
		File f = new File(getPath() + ext);
		if (f.exists())
			return f;
		return null;
	}

	private String getPath() {
		String absFilePath = new StringBuilder().append(this.directory.getAbsoluteFile())
		.append(File.separator).append(AIRBASE_FILE_PREFIX)
		.append(AIRBASE_FILE_SEPERATOR).append(this.country.toUpperCase())
		.append(AIRBASE_FILE_SEPERATOR).append(AIRBASE_FILE_VERSION)
		.append(AIRBASE_FILE_SEPERATOR).toString();
		log.info("File path created is: " + absFilePath);
		return absFilePath;
	}

	private void extractArchive(File archive, File destDir)
			throws ZipException, IOException {
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipFile zipF = new ZipFile(archive);
		Enumeration<?> entries = zipF.entries();
		byte[] buffer = new byte[BUFFER_SIZE];
		int len;
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String entryFileName = entry.getName();
			File dir = new File(destDir, entryFileName.substring(0,
					entryFileName.lastIndexOf(File.separator) + 1));
			if (!dir.exists()) {
				dir.mkdirs();
			}
			if (!entry.isDirectory()) {
				BufferedOutputStream bos = null;
				BufferedInputStream bis = null;
				try {
					bos = new BufferedOutputStream(new FileOutputStream(
							new File(destDir, entryFileName)));
					bis = new BufferedInputStream(zipF.getInputStream(entry));
					while ((len = bis.read(buffer)) > 0) {
						bos.write(buffer, 0, len);
					}
				} finally {
					if (bos != null) {
						bos.flush();
						bos.close();
					}
					if (bis != null) {
						bis.close();
					}
				}
			}
		}
	}
}
