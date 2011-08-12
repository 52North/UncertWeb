package org.uncertweb.viss.core.util;

import java.io.File;
import java.net.URI;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.joda.time.Period;
import org.uncertml.UncertML;

public class Constants {

	public static final URI NORMAL_DISTRIBUTION = URI
			.create(UncertML
					.getURI(org.uncertml.distribution.continuous.NormalDistribution.class));
	public static final URI NORMAL_DISTRIBUTION_MEAN = URI.create(Utils.join(
			"#", NORMAL_DISTRIBUTION.toString(), "mean"));
	public static final URI NORMAL_DISTRIBUTION_VARIANCE = URI.create(Utils
			.join("#", NORMAL_DISTRIBUTION.toString(), "variance"));

	public static final String NETCDF = "application/netcdf";
	public static final MediaType NETCDF_TYPE = MediaType.valueOf(NETCDF);

	public static final String X_NETCDF = "application/x-netcdf";
	public static final MediaType X_NETCDF_TYPE = MediaType.valueOf(X_NETCDF);

	public static final String GEOTIFF = "image/geotiff";
	public static final MediaType GEOTIFF_TYPE = MediaType.valueOf(GEOTIFF);

	public static final String OM_2 = "application/xml;subtype=\"om/2.0.0\"";
	public static final MediaType OM_2_TYPE = MediaType.valueOf(OM_2);

	public static final String STYLED_LAYER_DESCRIPTOR = "application/xml;subtype=\"sld/1.0.0\"";
	public static final MediaType STYLED_LAYER_DESCRIPTOR_TYPE = MediaType
			.valueOf(STYLED_LAYER_DESCRIPTOR);

	public static final String WORKING_DIR = get("workingDir");

	public static final String RESOURCE_PATH = Utils.join(File.separator,
			WORKING_DIR, "resources");
	public static final String HSQLDB_PATH = Utils.join(File.separator,
			WORKING_DIR, "database");

	public static final Period CLEAN_UP_INTERVAL = new Period(get(
			"cleanup.interval", "PT2H"));
	public static final Period DELETE_OLDER_THAN_PERIOD = new Period(get(
			"cleanup.deleteBefore", "P1D"));

	public static final String CONFIG_FILE = "/viss.properties";

	private static Properties p;

	private static String get(String key) {
		if (p == null) {
			try {
				p = new Properties();
				p.load(Constants.class.getResourceAsStream(CONFIG_FILE));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		String s = p.getProperty(key);

		return (s == null || s.trim().isEmpty()) ? null : s;
	}

	private static String get(String key, String defauld) {
		String s = get(key);
		return (s == null || s.trim().isEmpty()) ? defauld : s;
	}

	public static final boolean INTEND_JSON = true;

}
