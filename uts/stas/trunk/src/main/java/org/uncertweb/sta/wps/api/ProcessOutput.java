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
package org.uncertweb.sta.wps.api;

import org.n52.wps.io.data.IData;
import org.uncertweb.sta.utils.Constants;

/**
 * Encapsulates a WPS process output.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ProcessOutput {

	/**
	 * The ID of this output.
	 */
	private String identifier;

	/**
	 * The description of this output.
	 */
	private String description;

	/**
	 * The title of this output.
	 */
	private String title;

	/**
	 * The {@link IData} class that encapsulates this output.
	 */
	private Class<? extends IData> bindingClass;

	/**
	 * Constructs a new process output.
	 * 
	 * @param identifier the ID of the output
	 * @param title the title of the output (if {@code title} is {@code null},
	 *            the {@code id} is used instead)
	 * @param description the description of the output
	 * @param bindingClass the {@link IData} class that encapsulates this output
	 */
	public ProcessOutput(String identifier, Class<? extends IData> bindingClass) {
		this.identifier = identifier;
		this.bindingClass = bindingClass;
		this.description = getOutputDescription(identifier);
		this.title = getOutputTitle(identifier);
	}

	
	private static String getOutputDescription(String id) {
		return Constants.get("process.output." + id + ".desc");
	}

	private static String getOutputTitle(String id) {
		return Constants.get("process.output." + id + ".title");
	}

	/**
	 * @return the ID of this output
	 */
	public String getId() {
		return this.identifier;
	}

	/**
	 * @return the {@link IData} class that encapsulates this output
	 */
	public Class<? extends IData> getBindingClass() {
		return this.bindingClass;
	}

	/**
	 * @return the title of this output
	 */
	public String getTitle() {
		return this.title == null || this.title.trim().isEmpty() ? getId()
				: this.title;
	}

	/**
	 * @return the description of this output
	 */
	public String getDescription() {
		return this.description == null || this.description.trim().isEmpty() ? null
				: this.description;
	}
}
