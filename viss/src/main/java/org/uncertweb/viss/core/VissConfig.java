/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24,
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
