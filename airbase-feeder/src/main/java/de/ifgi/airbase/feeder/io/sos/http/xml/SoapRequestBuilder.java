package de.ifgi.airbase.feeder.io.sos.http.xml;

import java.net.URI;
import org.apache.xmlbeans.XmlObject;
import org.w3.x2003.x05.soapEnvelope.Envelope;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.Header;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SoapRequestBuilder extends AbstractXmlBuilder<EnvelopeDocument> {

    private XmlObject body;
    private URI action;

    public SoapRequestBuilder setBody(XmlObject body) {
        this.body = body;
        return this;
    }

    public SoapRequestBuilder setAction(URI action) {
        this.action = action;
        return this;
    }

    protected XmlObject getBody() {
        return body;
    }

    protected URI getAction() {
        return action;
    }

    @Override
    public EnvelopeDocument build() {
        EnvelopeDocument envelopeDocument = EnvelopeDocument.Factory.newInstance();
        Envelope envelope = envelopeDocument.addNewEnvelope();
        envelope.addNewBody().set(getBody());
        if (getAction() != null) {
            Header header = envelope.addNewHeader();
            //TODO action header
        }
        return envelopeDocument;
    }
}
