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

import static org.uncertweb.viss.core.util.VissConstants.CLEAN_UP_INTERVAL_DEFAULT;
import static org.uncertweb.viss.core.util.VissConstants.CLEAN_UP_INTERVAL_KEY;
import static org.uncertweb.viss.core.util.VissConstants.CONFIG_FILE;
import static org.uncertweb.viss.core.util.VissConstants.DELETE_OLDER_THAN_PERIOD_DEFAULT;
import static org.uncertweb.viss.core.util.VissConstants.DELETE_OLDER_THAN_PERIOD_KEY;
import static org.uncertweb.viss.core.util.VissConstants.RESOURCE_STORE_KEY;
import static org.uncertweb.viss.core.util.VissConstants.WMS_ADAPTER_KEY;
import static org.uncertweb.viss.core.util.VissConstants.WORKING_DIR_KEY;

import java.io.File;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.xmlbeans.XmlOptions;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.utils.UwStringUtils;
import org.uncertweb.viss.core.resource.IResourceStore;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.wms.WMSAdapter;

public class VissConfig {

	public static class ContextListener implements ServletContextListener {
		@Override
		public void contextDestroyed(ServletContextEvent arg0) {}

		@Override
		public void contextInitialized(ServletContextEvent e) {
			VissConfig.getInstance().setWebAppPath(
			    new File(e.getServletContext().getRealPath("/")));
		}
	}

	private static final Logger log = LoggerFactory.getLogger(VissConfig.class);
	private static VissConfig instance;
	
	public synchronized static VissConfig getInstance() {
		return (instance == null) ? instance = new VissConfig() : instance;
	}

	private final Timer timer = new Timer(true);
	private IResourceStore resourceStore;
	private WMSAdapter wmsAdapter;
	private Period cleanUpInterval;
	private Period periodToDeleteAfterLastUse;
	private Properties p;
	private Boolean doPrettyPrint;
	private XmlOptions defaultXmlOptions;
	private File webAppPath;
	private File workingDirectory;
	private File resourcePath;

	private VissConfig() {
		log.info("Instantiating VissConfig");
	}

	public IResourceStore getResourceStore() {
		if (this.resourceStore == null) {
			try {
				this.resourceStore = (IResourceStore) Class.forName(
				    get(RESOURCE_STORE_KEY)).newInstance();
			} catch (Exception e) {
				throw VissError.internal(e);
			}
		}
		return this.resourceStore;
	}

	public WMSAdapter getWMSAdapter() {
		if (this.wmsAdapter == null) {
			try {
				this.wmsAdapter = (WMSAdapter) Class.forName(get(WMS_ADAPTER_KEY))
				    .newInstance();
			} catch (Exception e) {
				throw VissError.internal(e);
			}
		}
		return this.wmsAdapter;
	}

	public void scheduleTask(TimerTask tt, Period p) {
		DateTime now = new DateTime();
		this.timer.schedule(tt, now.plusSeconds(30).toDate(), p.toDurationFrom(now)
		    .getMillis());
	}

	public synchronized String get(String key) {
		if (p == null) {
			try {
				p = new Properties();
				p.load(getClass().getResourceAsStream(CONFIG_FILE));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		String s = p.getProperty(key);

		return (s == null || s.trim().isEmpty()) ? null : s;
	}

	private String get(String key, String defauld) {
		String s = get(key);
		return (s == null || s.trim().isEmpty()) ? defauld : s;
	}

	public Period getCleanUpInterval() {
		if (cleanUpInterval == null) {
			cleanUpInterval = new Period(get(CLEAN_UP_INTERVAL_KEY,
			    CLEAN_UP_INTERVAL_DEFAULT));
		}
		return cleanUpInterval;
	}

	public Period getPeriodToDeleteAfterLastUse() {
		if (periodToDeleteAfterLastUse == null) {
			periodToDeleteAfterLastUse = new Period(get(DELETE_OLDER_THAN_PERIOD_KEY,
			    DELETE_OLDER_THAN_PERIOD_DEFAULT));
		}
		return periodToDeleteAfterLastUse;
	}

	public boolean doPrettyPrint() {
		if (doPrettyPrint == null) {
			doPrettyPrint = Boolean.valueOf(get("prettyPrintIO", "false"));
		}
		return doPrettyPrint;
	}

	public XmlOptions getDefaultXmlOptions() {
		if (defaultXmlOptions == null) {
			defaultXmlOptions = new XmlOptions()
				.setLoadStripWhitespace()
			    .setLoadStripProcinsts()
			    .setLoadStripComments()
			    .setLoadTrimTextBuffer()
			    .setSaveAggressiveNamespaces();
			if (doPrettyPrint()) {
				defaultXmlOptions.setSavePrettyPrint();
			}
		}
		return defaultXmlOptions;
	}

	public File getWorkingDirectory() {
		if (workingDirectory == null) {
			String path = get(WORKING_DIR_KEY);
			if (path == null || path.trim().isEmpty()) {
				if (this.webAppPath != null) {
					path = this.webAppPath.getAbsolutePath();
				} else {
					path = UwStringUtils.join(File.separator,
					    System.getProperty("java.io.tmpdir"), "VISS");
				}
			}
			workingDirectory = new File(path);
			if (!workingDirectory.exists()) {
				workingDirectory.mkdirs();
			}
		}
		return workingDirectory;
	}

	private void setWebAppPath(File webAppPath) {
		this.webAppPath = webAppPath;
	}

	public File getResourcePath() {
		if (resourcePath == null) {
			resourcePath = Utils.to(getWorkingDirectory(), "resources");
		}
		return resourcePath;
	}
}
