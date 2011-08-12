package org.uncertweb.viss.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.uncertweb.viss.core.resource.ResourceStore;
import org.uncertweb.viss.core.wcs.WCSAdapter;

public class VissConfig {
	private static final String CONFIG_FILE = "/viss.properties";
	private static final String RESOURCE_STORE_KEY = "implementation.resourceStore";
	private static final String WCS_ADAPTER_KEY = "implementation.wcsAdapter";

	private static VissConfig instance;

	public synchronized static VissConfig getInstance() {
		return (instance == null) ? instance = new VissConfig() : instance;
	}

	private final Timer timer = new Timer(true);
	private ResourceStore resourceStore;
	private WCSAdapter wcsAdapter;
	private Properties p = null;

	private VissConfig() {
		InputStream is = null;
		try {
			is = VissConfig.class.getResourceAsStream(CONFIG_FILE);
			if (is == null) {
				throw new RuntimeException("Can not load properties.");
			}
			p = new Properties();
			p.load(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public ResourceStore getResourceStore() {
		if (this.resourceStore == null) {
			try {
				this.resourceStore = (ResourceStore) Class.forName(
						p.getProperty(RESOURCE_STORE_KEY)).newInstance();
			} catch (Exception e) {
				throw VissError.internal(e);
			}
		}
		return this.resourceStore;
	}

	public WCSAdapter getWCSAdapter() {
		if (this.wcsAdapter == null) {
			try {
				this.wcsAdapter = (WCSAdapter) Class.forName(
						p.getProperty(WCS_ADAPTER_KEY)).newInstance();
			} catch (Exception e) {
				throw VissError.internal(e);
			}
		}
		return this.wcsAdapter;
	}

	public void scheduleTask(TimerTask tt, Period p) {
		DateTime now = new DateTime();
		this.timer.schedule(tt, now.plusSeconds(30).toDate(),
				p.toDurationFrom(now).getMillis());
	}

}
