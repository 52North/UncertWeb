package org.uncertweb.sta.wps;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.wps.x100.LiteralInputType;

import org.n52.wps.io.data.IData;

/**
 * @author Christian Autermann
 */
public class SingleProcessInput<T> extends IProcessInput<T> {
	
	private String identifier;
	private String description;
	private String title;
	private Class<? extends IData> bindingClass;
	private BigInteger minOccurs, maxOccurs;
	private Set<String> allowedValues;
	private T defaultValue;
	private ProcessInputHandler<T> handler;

	public SingleProcessInput(String identifier, String title, String description,
			Class<? extends IData> bindingClass, int min, int max,
			Set<String> allowedValues, T defaultValue, ProcessInputHandler<T> handler) {
		this.identifier = identifier;
		this.description = description;
		this.title = title;
		this.allowedValues = allowedValues;
		this.defaultValue = defaultValue;
		this.bindingClass = bindingClass;
		this.minOccurs = new BigInteger(String.valueOf(min));
		this.maxOccurs = new BigInteger(String.valueOf(max));
		if (handler != null) {
			this.handler = handler;
		} else {
			this.handler = new SingleProcessInputHandler<T>();
		}
		this.handler.setNeededInputs(this.getProcessInputs());
	}
	
	public SingleProcessInput(String identifier, String title, String description,
			Class<? extends IData> bindingClass, int min, int max,
			Set<String> allowedValues, T defaultValue) {
		this(identifier, title, description, bindingClass, min, max, allowedValues, defaultValue, null);
	}
	
	public SingleProcessInput(String identifier, String title, String description,
			Class<? extends IData> bindingClass, Set<String> allowedValues, T defaultValue) {
		this(identifier, title, description, bindingClass, 0, 1, allowedValues, defaultValue);
	}

	public SingleProcessInput(String identifier, String title, String description,
			Class<? extends IData> bindingClass, int min, int max) {
		this(identifier, title, description, bindingClass, min, max, null, null);
	}

	public SingleProcessInput(String identifier, String title, String description,
			Class<? extends IData> bindingClass) {
		this(identifier, title, description, bindingClass, 0, 1);
	}

	public T getDefaultValue() {
		return this.defaultValue;
	}

	public BigInteger getMinOccurs() {
		return this.minOccurs;
	}

	public BigInteger getMaxOccurs() {
		return this.maxOccurs;
	}

	public String getId() {
		return this.identifier;
	}

	public String getTitle() {
		return this.title == null ? this.getId() : this.title;
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

	@Override
	public Set<SingleProcessInput<?>> getProcessInputs() {
		Set<SingleProcessInput<?>> set = new HashSet<SingleProcessInput<?>>();
		set.add(this);
		return set;
	}

	@Override
	public T handle(Map<String, List<IData>> inputs) {
		T t = handler.process(inputs);
		if (t == null && this.defaultValue != null)
			return this.defaultValue;
		else return t;
	}

}