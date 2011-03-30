package org.uncertweb.api.om.io;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.xmlbeans.XmlException;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

public class JSONParser implements IObservationParser{

	@Override
	public AbstractObservation parseObservation(String xmlObs)
			throws IllegalArgumentException, MalformedURLException,
			URISyntaxException, XmlException, UncertaintyParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IObservationCollection parseObservationCollection(String xmlObsCol)
			throws XmlException, URISyntaxException, IllegalArgumentException,
			MalformedURLException, UncertaintyParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IObservationCollection parse(String xmlString) throws XmlException,
			URISyntaxException, IllegalArgumentException,
			MalformedURLException, UncertaintyParserException {
		// TODO Auto-generated method stub
		return null;
	}

}
