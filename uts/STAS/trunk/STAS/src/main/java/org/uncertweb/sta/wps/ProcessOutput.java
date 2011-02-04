package org.uncertweb.sta.wps;

import org.n52.wps.io.data.IData;

/**
 * @author Christian Autermann
 */
public class ProcessOutput {
	private String identifier;
	private String description;
	private String title;
	private Class<? extends IData> bindingClass;

	public ProcessOutput(String identifier, String title, String description,
			Class<? extends IData> bindingClass) {
		this.identifier = identifier;
		this.description = description;
		this.title = title;
		this.bindingClass = bindingClass;
		this.title = title;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getTitle() {
		return title == null ? getIdentifier() : title;
	}

	public Class<? extends IData> getBindingClass() {
		return this.bindingClass;
	}

	public String getDescription() {
		return this.description;
	}
}
