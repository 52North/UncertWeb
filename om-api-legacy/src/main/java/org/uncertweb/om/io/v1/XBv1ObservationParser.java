package org.uncertweb.om.io.v1;

import java.io.InputStream;

import org.apache.xmlbeans.XmlObject;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.IObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

public class XBv1ObservationParser implements IObservationParser {

    @Override
    public IObservationCollection parse(String xmlString)
            throws OMParsingException {
        return new OMDecoder().parse(xmlString);
    }

    @Override
    public IObservationCollection parseObservationCollection(String xmlObsCol)
            throws OMParsingException {
        return new OMDecoder().parse(xmlObsCol);
    }

    @Override
    public AbstractObservation parseObservation(String xmlObs)
            throws OMParsingException {
        throw new UnsupportedOperationException("Not supported");
    }

    public IObservationCollection parse(XmlObject xb_object)
            throws OMParsingException {
        return new OMDecoder().parse(xb_object);
    }

    @Override
    public IObservationCollection parseObservationCollection(InputStream in)
            throws OMParsingException {
        return new OMDecoder().parse(in);
    }

}
