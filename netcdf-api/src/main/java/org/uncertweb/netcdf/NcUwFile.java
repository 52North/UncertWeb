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
package org.uncertweb.netcdf;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.utils.UwCollectionUtils;

import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NcUwFile implements Closeable, Iterable<INcUwVariable> {
	protected static final Logger log = LoggerFactory.getLogger(NcUwFile.class);
	private Map<String, INcUwVariable> primaryVariables;
	private final File f;
	private final NetcdfFile file;
	private final NcUwArrayCache cache = new NcUwArrayCache();

	protected NcUwFile(final NetcdfFile file, final String path) {
		if (file == null) {
			throw new NullPointerException();
		}
		this.file = file;
		if (path != null) {
			this.f = new File(path);
		} else {
			this.f = null;
		}
		checkForConventions();
	}

	public NcUwFile(final String path) throws IOException {
		this(NetcdfFile.open(path), path);
	}

	public NcUwFile(final File file) throws IOException {
		this(file.getAbsolutePath());
	}

	protected boolean checkForConventions() {
		final String attr = getStringAttribute(	NcUwConstants.Attributes.CONVENTIONS, false);
		if (attr == null) { return false; }
		for (final String s : attr.split(" ")) {
			if (s.equalsIgnoreCase(NcUwConstants.UW_CONVENTION)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getPrimaryVariableNames() {
		return _getPrimaryVariables().keySet();
	}

	public Set<INcUwVariable> getPrimaryVariables() {
		return UwCollectionUtils.asSet(_getPrimaryVariables().values());
	}

	public INcUwVariable getPrimaryVariable(String name) {
		final INcUwVariable v = _getPrimaryVariables().get(name);
		if (v == null) {
			throw new NcUwException("There is no variable called %s", name);
		} else {
			return v;
		}
	}
	
	private Map<String, INcUwVariable> _getPrimaryVariables() {
		if (this.primaryVariables == null) {
			this.primaryVariables = UwCollectionUtils.map();
			for (final String name : getStringAttribute(NcUwConstants.Attributes.PRIMARY_VARIABLES, true).split(" ")) {
				try {
					this.primaryVariables.put(name, AbstractNcUwVariable.create(this, getVariable(name, true), getCache(), null));
				} catch (NcUwException e) {
					log.error("Can not process primary variable \"" + name + "\": {}", e.getMessage());
				}
			}
		}
		return this.primaryVariables;
	}
	
	public Variable getVariable(final String name,
			final boolean failIfNotExisting) {
		return NcUwHelper.findVariable(getFile(), name, failIfNotExisting);
	}

	public Variable getVariable(final String name) {
		return getVariable(name, false);
	}

	public String getStringAttribute(final String name,
			final boolean failIfNotExisting) {
		return NcUwHelper.getStringAttribute(getFile(), name, failIfNotExisting);
	}

	public Number getNumberAttribute(final String name,
			final boolean failIfNotExisting) {
		return NcUwHelper.getNumberAttribute(getFile(), name, failIfNotExisting);
	}

	public String getStringAttribute(final String name) {
		return getStringAttribute(name, false);
	}

	public Number getNumberAttribute(final String name) {
		return getNumberAttribute(name, false);
	}

	public NcUwArrayCache getCache() {
		return this.cache;
	}

	public NetcdfFile getFile() {
		return this.file;
	}

	
	
	@Override
	public void finalize() {
		try {
			close();
		} catch (final IOException e) {
		}
	}

	@Override
	public void close() throws IOException {
		getFile().close();
	}

	public File getUnderlyingFile() {
		return f;
	}

	public boolean delete() throws IOException {
		close();
		if (getUnderlyingFile() != null) {
			return getUnderlyingFile().delete();
		} else {
			return false;
		}
	}

	@Override
	public Iterator<INcUwVariable> iterator() {
		return getPrimaryVariables().iterator();
	}
}
