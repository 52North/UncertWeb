package org.uncertweb.viss.core;

import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.uncertweb.viss.core.resource.ResourceStore;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.wms.WMSAdapter;

public class VissConfig {
	private static VissConfig instance;

	public synchronized static VissConfig getInstance() {
		return (instance == null) ? instance = new VissConfig() : instance;
	}

	private final Timer timer = new Timer(true);
	private ResourceStore resourceStore;
	private WMSAdapter wmsAdapter;

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

	public WMSAdapter getWMSAdapter() {
		if (this.wmsAdapter == null) {
			try {
				this.wmsAdapter = (WMSAdapter) Class.forName(
						Constants.get(Constants.WMS_ADAPTER_KEY)).newInstance();
			} catch (Exception e) {
				throw VissError.internal(e);
			}
		}
		return this.wmsAdapter;
	}

	public void scheduleTask(TimerTask tt, Period p) {
		DateTime now = new DateTime();
		this.timer.schedule(tt, now.plusSeconds(30).toDate(),
				p.toDurationFrom(now).getMillis());
	}

}
