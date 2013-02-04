/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ifgi.airbase.feeder.io.sos.http.xml;

import de.ifgi.airbase.feeder.data.EEAConfiguration;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;
import java.util.Collection;
import net.opengis.sos.x20.SosInsertionMetadataDocument;
import net.opengis.sos.x20.SosInsertionMetadataType;
import net.opengis.swes.x20.InsertSensorDocument;
import net.opengis.swes.x20.InsertSensorType;
import net.opengis.swes.x20.InsertSensorType.ProcedureDescription;
import org.apache.xmlbeans.XmlObject;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class InsertSensorRequestBuilder extends AbstractXmlBuilder<InsertSensorDocument> {
    private EEAStation station;
    private XmlObject description;
    
    public InsertSensorRequestBuilder setStation(EEAStation station) {
        this.station = station;
        return this;
    }
    
    protected EEAStation getStation() {
        return station;
    }
    
    public InsertSensorRequestBuilder setDescription(XmlObject description) {
        this.description = description;
        return this;
    }
    
    protected XmlObject getDescription() {
        return this.description;
    }

    @Override
    public InsertSensorDocument build() throws NoValidInputsOrOutputsException {
        InsertSensorDocument insertSensorDocument = InsertSensorDocument.Factory.newInstance();
        InsertSensorType insertSensorType = insertSensorDocument.addNewInsertSensor();
        insertSensorType.setService(AbstractXmlBuilder.SOS_SERVICE_NAME);
        insertSensorType.setVersion(AbstractXmlBuilder.SOS_V2_SERVICE_VERSION);
        insertSensorType.setProcedureDescriptionFormat(SOSNamespaceUtils.SML_1_0_1_PROCEDURE_DESCRIPTION_FORMAT);
        ProcedureDescription procedureDescription = insertSensorType.addNewProcedureDescription();
        procedureDescription.set(getDescription());
        setObservableProperties(insertSensorType);
        setInsertionMetadata(insertSensorType);
        SOSNamespaceUtils.insertSchemaLocations(insertSensorType);
        return insertSensorDocument;
    }

    private void setObservableProperties(InsertSensorType insertSensorType) throws NoValidInputsOrOutputsException {
        Collection<EEAConfiguration> configurations = getUniqueConfigurations(getStation());
        for (EEAConfiguration configuration : configurations) {
            insertSensorType.addObservableProperty(getPhenomenonId(configuration.getComponentCode()));
        }
    }
    
    private void setInsertionMetadata(InsertSensorType insertSensorType) {
        SosInsertionMetadataDocument sosInsertionMetadataDocument = SosInsertionMetadataDocument.Factory.newInstance();
        SosInsertionMetadataType sosInsertionMetadataType = sosInsertionMetadataDocument.addNewSosInsertionMetadata();
        sosInsertionMetadataType.addFeatureOfInterestType(SF_SAMPLING_POINT);
        sosInsertionMetadataType.addObservationType(OM_2_MEASUREMENT_OBSERVATION);
        insertSensorType.addNewMetadata().set(sosInsertionMetadataDocument);
    }

    
}
