package org.uncertweb.api.om.io;

import org.apache.xmlbeans.XmlException;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

public class JSONEncoder implements IObservationEncoder{

	@Override
	public String encodeObservation(AbstractObservation obs)
			throws IllegalArgumentException, XmlException,
			UnsupportedUncertaintyTypeException, UncertaintyEncoderException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeObservationCollection(IObservationCollection obsCol)
			throws IllegalArgumentException, XmlException,
			UnsupportedUncertaintyTypeException, UncertaintyEncoderException {
		// TODO Auto-generated method stub
		return null;
	}

}
