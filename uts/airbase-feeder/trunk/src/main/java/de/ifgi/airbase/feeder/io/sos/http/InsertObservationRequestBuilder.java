package de.ifgi.airbase.feeder.io.sos.http;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
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

/**
 * Class to build a {@link InsertObservationDocument} for an
 * {@link EEARawDataFile}.
 * 
 * @author Christian Autermann
 * 
 */
public class InsertObservationRequestBuilder extends SOSRequestBuilder {

	/**
	 * Creates a {@code InsertObservationDocument} that inserts a
	 * {@code EEARawDataFile} into a SOS.
	 * 
	 * @param file
	 *            the {@code EEARawDataFile} that should be inserted.
	 * @param line
	 *            the line
	 * @return the InsertObservation request
	 */
	public static InsertObservationDocument buildInsertObservationRequest(
			EEARawDataFile file, Collection<EEAMeasurement> eeams) {
		InsertObservationDocument insObDoc = InsertObservationDocument.Factory
				.newInstance();
		List<EEAMeasurement> sortedMeasurements = new LinkedList<EEAMeasurement>(
				eeams);
		Collections.sort(sortedMeasurements, new Comparator<EEAMeasurement>() {
			@Override
            public int compare(EEAMeasurement o1, EEAMeasurement o2) {
				return o1.getTime().compareTo(o2.getTime());
			}
		});

		InsertObservation insert = insObDoc.addNewInsertObservation();
		SOSNamespaceUtils.nameSpaceOptions(insert);
		insert.setVersion(SOS_SERVICE_VERSION);
		insert.setService(SOS_SERVICE_NAME);
		insert.setAssignedSensorId(getStationId(file.getStation()));
		ObservationType observation = insert.addNewObservation();
		buildSamplingTime(file, observation, sortedMeasurements);
		buildObservedProperty(file, observation);
		buildProcedure(file, observation);
		buildFeatureOfInterest(file, observation);
		buildResult(file, observation, sortedMeasurements);
		SOSNamespaceUtils.schemaLocations(insObDoc);
		return insObDoc;
	}

	private static void buildObservedProperty(EEARawDataFile file,
			ObservationType observation) {
		observation.addNewObservedProperty().setHref(
				getPhenomenonId(file.getConfiguration().getComponentCode()));
	}

	private static void buildProcedure(EEARawDataFile file,
			ObservationType observation) {
		observation.addNewProcedure().setHref(getStationId(file.getStation()));
	}

	private static void buildSamplingTime(EEARawDataFile file,
			ObservationType observationType, List<EEAMeasurement> eeams) {
		String startString = Utils.ISO8601_DATETIME_FORMAT.print(eeams
				.get(0).getTime());
		String endString = Utils.ISO8601_DATETIME_FORMAT.print(eeams
				.get(eeams.size() - 1).getTime());
		TimePeriodType tpt = (TimePeriodType) observationType
				.addNewSamplingTime()
				.addNewTimeObject()
				.substitute(SOSNamespaceUtils.gml("TimePeriod"),
						TimePeriodType.type);
		tpt.addNewBeginPosition().setStringValue(startString);
		tpt.addNewEndPosition().setStringValue(endString);
	}

	private static void buildFeatureOfInterest(EEARawDataFile file,
			ObservationType observation) {
		FeaturePropertyType fpt = observation.addNewFeatureOfInterest();
		AbstractFeatureType aft = fpt.addNewFeature();
		FeatureCollectionType fct = (FeatureCollectionType) aft.substitute(
				SOSNamespaceUtils.gml("FeatureCollection"),
				FeatureCollectionType.type);
		FeaturePropertyType member = fct.addNewFeatureMember();
		AbstractFeatureType feature = member.addNewFeature();
		SamplingPointType spt = SamplingPointType.Factory.newInstance();
		spt.addNewName().setStringValue(
				getFeatureOfInterestId(file.getStation()));
		spt.setId(getFeatureOfInterestId(file.getStation()));
		spt.addNewSampledFeature().setHref("");
		DirectPositionType pos = spt.addNewPosition().addNewPoint().addNewPos();
		pos.setSrsName(EPSG_4326_REFERENCE_SYSTEM_DEFINITION);
		pos.setStringValue(buildPosString(file.getStation()));
		feature.set(spt);
		XmlCursor c = member.newCursor();
		c.toChild(SOSNamespaceUtils.gml("_Feature"));
		c.setName(SOSNamespaceUtils.sa("SamplingPoint"));
		c.dispose();
	}

	private static void buildResult(EEARawDataFile file,
			ObservationType observation, List<EEAMeasurement> eeams) {
		Tuple<Integer, String> dataArrayValues = buildSweDataArrayContent(file,
				eeams);
		DataArrayDocument dataArrayDoc = DataArrayDocument.Factory
				.newInstance();
		DataArrayType dataArray = dataArrayDoc.addNewDataArray1();
		dataArray.addNewElementCount().addNewCount()
				.setValue(BigInteger.valueOf(dataArrayValues.getElem1()));
		DataComponentPropertyType dcpt = dataArray.addNewElementType();
		dcpt.setName("Components");
		DataRecordType sdrt = (DataRecordType) dcpt.addNewAbstractDataRecord()
				.substitute(SOSNamespaceUtils.swe("DataRecord"),
						DataRecordType.type);
		DataComponentPropertyType timeField = sdrt.addNewField();
		timeField.setName("time");
		timeField.addNewTime().setDefinition(ISO8601_TIME_FORMAT_DEFINITION);
		DataComponentPropertyType componentField = sdrt.addNewField();
		Quantity q = componentField.addNewQuantity();
		q.setDefinition(getPhenomenonId(file.getConfiguration()
				.getComponentCode()));
		q.addNewUom().setCode(
				file.getConfiguration().getMeasurementUnit().replace(' ', '_'));
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

	private static Tuple<Integer, String> buildSweDataArrayContent(
			EEARawDataFile file, List<EEAMeasurement> eeams) {
		String foi = getFeatureOfInterestId(file.getStation());
		switch (file.getType()) {
		case VAR:
			throw new Error("Unknown Format: " + file.getType());
		default:
			return buildSweDataArrayContent(eeams, foi);
		}
	}

	private static Tuple<Integer, String> buildSweDataArrayContent(
			List<EEAMeasurement> eeams, String foi) {
		StringBuffer sb = new StringBuffer();
		int count = 0;
		for (EEAMeasurement eeam : eeams) {
			if (eeam.isValid()) {
				count++;
				sb.append(
						Utils.ISO8601_DATETIME_FORMAT.print(eeam
								.getTime()))
						.append(",")
						.append(MEASUREMENT_VALUE_FORMAT.format(eeam.getValue()))
						.append(",").append(foi).append(";");
			}
		}
		return new Tuple<Integer, String>(count, sb.toString());
	}
}
