package de.ifgi.airbase.feeder.io.sos.http.xml;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import net.opengis.gml.AbstractFeatureType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.FeatureCollectionType;
import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.TimePeriodType;
import net.opengis.om.x10.ObservationType;
import net.opengis.sampling.x10.SamplingPointType;
import net.opengis.sos.x10.InsertObservationDocument;
import net.opengis.sos.x10.InsertObservationDocument.InsertObservation;
import net.opengis.swe.x101.DataArrayDocument;
import net.opengis.swe.x101.DataArrayType;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.DataValuePropertyType;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.TextBlockDocument.TextBlock;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlString;

import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;
import de.ifgi.airbase.feeder.util.Tuple;
import de.ifgi.airbase.feeder.util.Utils;
import java.util.Collections;

/**
 * Class to build a {@link InsertObservationDocument} for an
 * {@link EEARawDataFile}.
 * 
 * @author Christian Autermann
 * 
 */
public class InsertObservationRequestBuilder extends AbstractXmlBuilder<InsertObservationDocument> {

    private EEARawDataFile file;
    private Collection<EEAMeasurement> values;

    public InsertObservationRequestBuilder setFile(EEARawDataFile file) {
        this.file = file;
        return this;
    }

    protected EEARawDataFile getFile() {
        return file;
    }
    
    public InsertObservationRequestBuilder setValues(Collection<EEAMeasurement> values) {
        this.values = values;
        return this;
    }
    
    protected Collection<EEAMeasurement> getValues() {
        return Collections.unmodifiableCollection(values);
    }
    
	/**
	 * Creates a {@code InsertObservationDocument} that inserts a
	 * {@code EEARawDataFile} into a SOS.
	 * 
	 * @return the InsertObservation request
	 */
    @Override
	public InsertObservationDocument build() {
		InsertObservationDocument insObDoc = InsertObservationDocument.Factory.newInstance();
		List<EEAMeasurement> sortedMeasurements = sortMeasurements(getValues());
		InsertObservation insert = insObDoc.addNewInsertObservation();
		insert.setVersion(SOS_V1_SERVICE_VERSION);
		insert.setService(SOS_SERVICE_NAME);
		insert.setAssignedSensorId(getStationId(getFile().getStation()));
		ObservationType observation = insert.addNewObservation();
		buildSamplingTime(observation, sortedMeasurements);
		buildObservedProperty(observation);
		buildProcedure(observation);
		buildFeatureOfInterest(observation);
		buildResult(observation, sortedMeasurements);
		SOSNamespaceUtils.insertSchemaLocations(insObDoc);
		return insObDoc;
	}
    
	protected void buildObservedProperty(ObservationType observation) {
		observation.addNewObservedProperty().setHref(getPhenomenonId(getFile().getConfiguration().getComponentCode()));
	}
    
	protected void buildProcedure(ObservationType observation) {
		observation.addNewProcedure().setHref(getStationId(getFile().getStation()));
	}

	protected void buildSamplingTime(ObservationType observationType, List<EEAMeasurement> eeams) {
		String startString = Utils.ISO8601_DATETIME_FORMAT.print(eeams.get(0).getTime());
		String endString = Utils.ISO8601_DATETIME_FORMAT.print(eeams.get(eeams.size() - 1).getTime());
		TimePeriodType tpt = (TimePeriodType) observationType
				.addNewSamplingTime()
				.addNewTimeObject()
				.substitute(SOSNamespaceUtils.QN_GML_TIME_PERIOD,
						TimePeriodType.type);
		tpt.addNewBeginPosition().setStringValue(startString);
		tpt.addNewEndPosition().setStringValue(endString);
	}

	protected void buildFeatureOfInterest(ObservationType observation) {
		FeaturePropertyType fpt = observation.addNewFeatureOfInterest();
		AbstractFeatureType aft = fpt.addNewFeature();
		FeatureCollectionType fct = (FeatureCollectionType) aft.substitute(
				SOSNamespaceUtils.QN_GML_FEATURE_COLLECTION,
				FeatureCollectionType.type);
		FeaturePropertyType member = fct.addNewFeatureMember();
		AbstractFeatureType feature = member.addNewFeature();
		SamplingPointType spt = SamplingPointType.Factory.newInstance();
		spt.addNewName().setStringValue(
				getFeatureOfInterestId(getFile().getStation()));
		spt.setId(getFeatureOfInterestId(getFile().getStation()));
		spt.addNewSampledFeature().setHref("");
		DirectPositionType pos = spt.addNewPosition().addNewPoint().addNewPos();
		pos.setSrsName(EPSG_4326_REFERENCE_SYSTEM_DEFINITION);
		pos.setStringValue(buildPosString(getFile().getStation()));
		feature.set(spt);
		XmlCursor c = member.newCursor();
		c.toChild(SOSNamespaceUtils.QN_GML_ABSTRACT_FEATURE);
		c.setName(SOSNamespaceUtils.QN_SA_SAMPLING_POINT);
		c.dispose();
	}

	protected void buildResult(ObservationType observation, List<EEAMeasurement> eeams) {
		Tuple<Integer, String> dataArrayValues = buildSweDataArrayContent(eeams);
		DataArrayDocument dataArrayDoc = DataArrayDocument.Factory
				.newInstance();
		DataArrayType dataArray = dataArrayDoc.addNewDataArray1();
		dataArray.addNewElementCount().addNewCount()
				.setValue(BigInteger.valueOf(dataArrayValues.getElem1()));
		DataComponentPropertyType dcpt = dataArray.addNewElementType();
		dcpt.setName("Components");
		DataRecordType sdrt = (DataRecordType) dcpt.addNewAbstractDataRecord()
				.substitute(SOSNamespaceUtils.QN_SWE_1_0_1_DATA_RECORD,
						DataRecordType.type);
		DataComponentPropertyType timeField = sdrt.addNewField();
		timeField.setName("time");
		timeField.addNewTime().setDefinition(ISO8601_TIME_FORMAT_DEFINITION);
		DataComponentPropertyType componentField = sdrt.addNewField();
		Quantity q = componentField.addNewQuantity();
		q.setDefinition(getPhenomenonId(getFile().getConfiguration()
				.getComponentCode()));
		q.addNewUom().setCode(
				getFile().getConfiguration().getMeasurementUnit().replace(' ', '_'));
		DataComponentPropertyType featureField = sdrt.addNewField();
		featureField.setName("feature");
		featureField.addNewText().setDefinition(FOI_DEFINITION);
		TextBlock textBlock = dataArray.addNewEncoding().addNewTextBlock();
		textBlock.setDecimalSeparator(SWE_DATA_ARRAY_DECIMAL_SEPERATOR);
		textBlock.setTokenSeparator(SWE_DATA_ARRAY_TOKEN_SEPERATOR);
		textBlock.setBlockSeparator(SWE_DATA_ARRAY_BLOCK_SEPERATOR);
		DataValuePropertyType dvpt = dataArray.addNewValues();
		dvpt.setRecordCount(new BigInteger(String.valueOf(dataArrayValues
				.getElem1())));
		XmlString xs = XmlString.Factory.newInstance();
		xs.setStringValue(dataArrayValues.getElem2());
		dvpt.set(xs);
		observation.addNewResult().set(dataArrayDoc);
	}

	protected Tuple<Integer, String> buildSweDataArrayContent(List<EEAMeasurement> eeams) {
		String foi = getFeatureOfInterestId(getFile().getStation());
		switch (getFile().getType()) {
		case VAR:
			throw new Error("Unknown Format: " + getFile().getType());
		default:
			return buildSweDataArrayContent(eeams, foi);
		}
	}

	protected Tuple<Integer, String> buildSweDataArrayContent(List<EEAMeasurement> eeams, String foi) {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (EEAMeasurement eeam : eeams) {
			if (eeam.isValid()) {
				count++;
				sb.append(Utils.ISO8601_DATETIME_FORMAT.print(eeam.getTime()))
                  .append(SWE_DATA_ARRAY_TOKEN_SEPERATOR)
                  .append(MEASUREMENT_VALUE_FORMAT.format(eeam.getValue()))
                  .append(SWE_DATA_ARRAY_TOKEN_SEPERATOR)
                  .append(foi)
                  .append(SWE_DATA_ARRAY_BLOCK_SEPERATOR);
			}
		}
		return new Tuple<Integer, String>(count, sb.toString());
	}
}