package org.uncertweb.viss.core;

import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.uncertweb.viss.core.resource.ResourceStore;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.wcs.WCSAdapter;

public class VissConfig {
	private static VissConfig instance;

	public synchronized static VissConfig getInstance() {
		return (instance == null) ? instance = new VissConfig() : instance;
	}

	private final Timer timer = new Timer(true);
	private ResourceStore resourceStore;
	private WCSAdapter wcsAdapter;

	private VissConfig() {}

	public ResourceStore getResourceStore() {
		if (this.resourceStore == null) {
			try {
				this.resourceStore = (ResourceStore) Class.forName(
						Constants.get(Constants.RESOURCE_STORE_KEY)).newInstance();
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
						Constants.get(Constants.WCS_ADAPTER_KEY)).newInstance();
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
