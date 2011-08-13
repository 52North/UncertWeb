package org.uncertweb.viss.mongo;

import org.uncertweb.viss.core.visualizer.Visualizer;
import org.uncertweb.viss.core.visualizer.VisualizerFactory;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;

@SuppressWarnings("rawtypes")
public class VisualizerConverter extends TypeConverter implements
		SimpleValueConverter {

	public VisualizerConverter() {
		super(Visualizer.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		Visualizer v = (Visualizer) value;
		return v.getShortName();
	}

	@Override
	public Object decode(Class c, Object o, MappedField i)
			throws MappingException {
		if (o == null)
			return null;
		return VisualizerFactory.getVisualizer((String) o);
	}
}