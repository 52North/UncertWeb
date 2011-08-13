package org.uncertweb.viss.mongo;

import net.opengis.sld.StyledLayerDescriptorDocument;
import net.opengis.sld.impl.StyledLayerDescriptorDocumentImpl;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.uncertweb.viss.core.VissError;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;

@SuppressWarnings("rawtypes")
public class XmlObjectConverter extends TypeConverter implements
		SimpleValueConverter {

	public XmlObjectConverter() {
		super(XmlObject.class, StyledLayerDescriptorDocument.class,
				StyledLayerDescriptorDocumentImpl.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null)
			return null;
		return ((XmlObject) value).xmlText();
	}

	@Override
	public Object decode(Class c, Object o, MappedField i)
			throws MappingException {
		if (o == null)
			return null;
		try {
			return XmlObject.Factory.parse(o.toString());
		} catch (XmlException e) {
			throw VissError.internal(e);
		}

	}
}
