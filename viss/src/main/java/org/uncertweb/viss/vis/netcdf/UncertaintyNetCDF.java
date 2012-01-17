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
package org.uncertweb.viss.vis.netcdf;

import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.ATTRIBUTE_CONVENTIONS;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.ATTRIBUTE_PRIMARY_VARIABLES;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.UW_CONVENTION;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.UncertaintyType;

import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;


public class UncertaintyNetCDF {
	private List<UncertaintyVariable> variables;
	private NetcdfFile f;

	public UncertaintyNetCDF(NetcdfFile f) {
//TODO	if (!checkForUWConvention(f)) throw new IllegalArgumentException("File is not compliant with UW-1.0 convention.");
		this.f = f;
	}

	protected boolean checkForUWConvention(NetcdfFile f) {
		for (String s : getNotNullGlobalAttribute(ATTRIBUTE_CONVENTIONS)
				.getStringValue().split(" "))
			if (s.equals(UW_CONVENTION))
				return true;
		return false;
	}

	public List<UncertaintyVariable> getVariables() {
		if (this.variables == null) {
			this.variables = UwCollectionUtils.list();
			for (String pv : getNotNullGlobalAttribute(ATTRIBUTE_PRIMARY_VARIABLES).getStringValue().split(" ")) {
				Variable v = getNotNullVariable(pv);
				Attribute a = v.findAttribute("ref");
				if (a != null && UncertaintyType.fromURI(URI.create(a.getStringValue())) != null) {
					this.variables.add(new UncertaintyVariable(this, getNotNullVariable(pv)));
				}
			}
		}
		return this.variables;
	}
	
	public UncertaintyVariable getVariable(String name) {
		for (UncertaintyVariable v : getVariables()) {
			if (v.getName().equals(name)) {
				return v;
			}
		}
		return null;
	}

	public Set<UncertaintyType> getTypes() {
		Set<UncertaintyType> set = UwCollectionUtils.set();
		for (UncertaintyVariable uv : getVariables()) {
			set.add(uv.getType());
		}
		return set;
	}

	Variable getNotNullVariable(String name) {
		Variable v = f.findVariable(name);
		if (v == null) {
			throw new NullPointerException("No such variable: " + name);
		} else {
			return v;
		}
	}

	Attribute getNotNullGlobalAttribute(String attribute) {
		Attribute a = f.findGlobalAttribute(attribute);
		if (a == null) {
			throw new NullPointerException("No such attribute: " + attribute);
		} else {
			return a;
		}
	}
	
}
