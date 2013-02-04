/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ifgi.airbase.feeder.io.sos.http.xml;

import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;
import de.ifgi.airbase.feeder.util.Utils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.opengis.sos.x20.InsertResultDocument;
import net.opengis.sos.x20.InsertResultType;
import org.apache.xmlbeans.XmlString;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class InsertResultRequestBuilder extends AbstractXmlBuilder<InsertResultDocument> {
    private EEARawDataFile file;
    private Collection<EEAMeasurement> values;
    
    public InsertResultRequestBuilder setFile(EEARawDataFile file) {
        this.file = file;
        return this;
    }

    public InsertResultRequestBuilder setValues(Collection<EEAMeasurement> values) {
        this.values = values;
        return this;
    }
    
    protected EEARawDataFile getFile() {
        return file;
    }

    protected Collection<EEAMeasurement> getValues() {
        return Collections.unmodifiableCollection(values);
    }
    
    @Override
    public InsertResultDocument build() {
        InsertResultDocument insertResultDocument = InsertResultDocument.Factory.newInstance();
        InsertResultType insertResultType = insertResultDocument.addNewInsertResult();
        insertResultType.setVersion(SOS_V2_SERVICE_VERSION);
        insertResultType.setService(SOS_SERVICE_NAME);
        insertResultType.setTemplate(getResultTemplateIdentifier(getFile().getStation(), getFile().getConfiguration()));
        String resultString = buildResultString(sortMeasurements(getValues()));
        XmlString valuesXmlString = XmlString.Factory.newInstance();
        valuesXmlString.setStringValue(resultString);
        insertResultType.addNewResultValues().set(valuesXmlString);
        SOSNamespaceUtils.insertSchemaLocations(insertResultDocument);
        return insertResultDocument;
    }

    protected String buildResultString(List<EEAMeasurement> eeams) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (EEAMeasurement eeam : eeams) {
            if (!eeam.isValid()) {
                continue;
            }
            count++;
            sb.append(Utils.ISO8601_DATETIME_FORMAT.print(eeam.getTime()))
              .append(AbstractXmlBuilder.SWE_DATA_ARRAY_TOKEN_SEPERATOR)
              .append(AbstractXmlBuilder.MEASUREMENT_VALUE_FORMAT.format(eeam.getValue()))
              .append(AbstractXmlBuilder.SWE_DATA_ARRAY_BLOCK_SEPERATOR);
        }
        return count + AbstractXmlBuilder.SWE_DATA_ARRAY_BLOCK_SEPERATOR + sb.toString();
    }
}