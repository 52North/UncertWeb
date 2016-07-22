/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ifgi.airbase.feeder.io.sos.http.xml;

import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;
import net.opengis.om.x10.MeasurementType;
import net.opengis.sos.x10.ObservationTemplateDocument;
import net.opengis.sos.x10.RegisterSensorDocument;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class RegisterSensorRequestBuilder extends AbstractXmlBuilder<RegisterSensorDocument> {

    private static final String AN_UOM = "uom";
    private XmlObject description;

    public RegisterSensorRequestBuilder setDescription(XmlObject description) {
        this.description = description;
        return this;
    }

    protected XmlObject getDescription() {
        return this.description;
    }

    /**
     * Creates a {@code RegisterSensorDocument} that registers a {@code EEAStation} at a SOS.
     *
     * @param station
     *        the {@code EEASTation} that should be registered.
     * @return the RegisterSensor request
     * @throws NoValidInputsOrOutputsException
     */
    @Override
    public RegisterSensorDocument build() throws NoValidInputsOrOutputsException {
        RegisterSensorDocument regSensorDoc = RegisterSensorDocument.Factory.newInstance();
        RegisterSensorDocument.RegisterSensor regSensor = regSensorDoc.addNewRegisterSensor();
        regSensor.setService(SOS_SERVICE_NAME);
        regSensor.setVersion(SOS_V1_SERVICE_VERSION);
        regSensor.addNewSensorDescription().set(getDescription());
        // buildDomainFeature(station, regSensor.addNewDomainFeature());
        buildObservationTemplate(regSensor.addNewObservationTemplate());
        SOSNamespaceUtils.insertSchemaLocations(regSensor);
        return regSensorDoc;
    }

    private void buildObservationTemplate(ObservationTemplateDocument.ObservationTemplate template) {
        MeasurementType measurementType = (MeasurementType) template.addNewObservation()
                .substitute(SOSNamespaceUtils.QN_OM_1_0_MEASUREMENT, MeasurementType.type);
        measurementType.addNewSamplingTime();
        measurementType.addNewProcedure();
        measurementType.addNewObservedProperty();
        measurementType.addNewFeatureOfInterest();
        XmlObject result = measurementType.addNewResult();
        XmlCursor resultCursor = result.newCursor();
        resultCursor.toNextToken();
        resultCursor.insertAttributeWithValue(AN_UOM, "");
        resultCursor.insertChars("0.0");
        resultCursor.dispose();
    }

}
