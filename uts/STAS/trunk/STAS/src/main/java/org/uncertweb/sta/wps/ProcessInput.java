package org.uncertweb.sta.wps;

import java.math.BigInteger;
import java.util.Set;

import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.wps.x100.LiteralInputType;

import org.n52.wps.io.data.IData;

/**
 * @author Christian Autermann
 */
public class ProcessInput {
	private String identifier;
	private String description;
	private String title;
	private Class<? extends IData> bindingClass;
	private BigInteger minOccurs, maxOccurs;
	private Set<String> allowedValues;
	private String defaultValue;

	public ProcessInput(String identifier, String title, String description,
			Class<? extends IData> bindingClass, int min, int max,
			Set<String> allowedValues, String defaultValue) {
		this.identifier = identifier;
		this.description = description;
		this.title = title;
		this.allowedValues = allowedValues;
		this.defaultValue = defaultValue;
		this.bindingClass = bindingClass;
		this.minOccurs = new BigInteger(String.valueOf(min));
		this.maxOccurs = new BigInteger(String.valueOf(max));
	}
	
	public ProcessInput(String identifier, String title, String description,
			Class<? extends IData> bindingClass, Set<String> allowedValues, String defaultValue) {
		this(identifier, title, description, bindingClass, 0, 1, allowedValues, defaultValue);
	}

	public ProcessInput(String identifier, String title, String description,
			Class<? extends IData> bindingClass, int min, int max) {
		this(identifier, title, description, bindingClass, min, max, null, null);
	}

	public ProcessInput(String identifier, String title, String description,
			Class<? extends IData> bindingClass) {
		this(identifier, title, description, bindingClass, 0, 1);
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public BigInteger getMinOccurs() {
		return this.minOccurs;
	}

	public BigInteger getMaxOccurs() {
		return this.maxOccurs;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getTitle() {
		return this.title == null ? this.getIdentifier() : this.title;
	}

	public String getDescription() {
		return this.description;
	}

	public Class<? extends IData> getBindingClass() {
		return this.bindingClass;
	}

	public AllowedValues getAllowedValues() {
		if (allowedValues == null || allowedValues.size() == 0) {
			return null;
		} else {
			LiteralInputType lit = LiteralInputType.Factory.newInstance();
			AllowedValues vals = lit.addNewAllowedValues();
			for (String val : allowedValues) {
				vals.addNewValue().setStringValue(val);
			}
			return vals;
		}
	}

}
