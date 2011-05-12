package org.n52.wps.io.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import junit.framework.TestCase;
import net.opengis.gml.x32.MultiLineStringType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.om.x20.OMUncertaintyObservationDocument;
import net.opengis.om.x20.UWUncertaintyObservationType;

import org.apache.xmlbeans.XmlObject;
import org.isotc211.x2005.gmd.DQQuantitativeAttributeAccuracyType;
import org.isotc211.x2005.gmd.DQUncertaintyResultType;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.OMData;
import org.n52.wps.io.data.binding.complex.OMDataBinding;
import org.n52.wps.io.datahandler.xml.OMGenerator;
import org.n52.wps.io.datahandler.xml.OMParser;
import org.uncertml.distribution.continuous.GaussianDistribution;
import org.uncertml.distribution.multivariate.MultivariateGaussianDistribution;
import org.uncertml.statistic.CovarianceMatrix;
import org.uncertml.x20.GaussianDistributionType;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.gml.geometry.GmlGeometryFactory;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class OMGeneratorTestCase extends TestCase {

	private String localPath = "D:/unc_Profiles/";
	private String pathToExamples = "uWPS/src/main/resources";

	public void testObservationGenerator() throws Exception {

//		obsColTest();
		obsTest();
	}

	private void obsTest() throws Exception {

		///////////////////////////////////////////////////
		// create an observation by using the om api classes
		// create an identifier
		Identifier identifier = null;

		// create bounds to the observation (optional)
		Envelope boundedBy = new Envelope(new Coordinate(3397499.22509252,
				5752217.51986212), new Coordinate(3398874.68920850,
				5754034.77200970));

		// create phenomenon result and valid (optional) time
		TimeObject phenomenonTime = new TimeObject(
				"2010-01-22T14:55:02.000+02:00");
		TimeObject resultTime = new TimeObject("2010-01-22T14:55:02.000+02:00");
		TimeObject validTime = new TimeObject("2005-01-13T16:22:25.000+01:00","2005-01-14T16:22:25.000+01:00");
		
		// create procedure and observed property
		URI procedure = new URI(
				"http://www.uncertweb.org/phenomenon/PM10_total");
		URI observedProperty = new URI(
				"http://www.uncertweb.org/phenomenon/PM10_total");

		// create spatial feature which carries the spatial support
		GmlGeometryFactory gfac = new GmlGeometryFactory();
		Coordinate[] coords = {
				new Coordinate(3397499.2250925214, 5752217.519862129),
				new Coordinate(3397533.721110907, 5752253.128932143),
				new Coordinate(3397574.2943997066, 5752290.48912186),
				new Coordinate(3397606.462574296, 5752318.187378165),
				new Coordinate(3397628.186147106, 5752335.3069503335),
				new Coordinate(3397648.8281844757, 5752350.469419348),
				new Coordinate(3397672.5088546355, 5752366.507449599),
				new Coordinate(3397702.349178348, 5752386.294896821),
				new Coordinate(3397774.143154312, 5752428.372879227),
				new Coordinate(3397789.4598990907, 5752435.748131653),
				new Coordinate(3397818.095094382, 5752450.581021927),
				new Coordinate(3397867.0839497675, 5752473.582327321),
				new Coordinate(3397928.186123235, 5752499.086720697),
				new Coordinate(3397999.485704568, 5752529.1748742275),
				new Coordinate(3398344.5395757197, 5752669.077886183),
				new Coordinate(3398391.324064551, 5752687.165750681),
				new Coordinate(3398413.7583583803, 5752697.250018042),
				new Coordinate(3398441.2295590867, 5752708.127447226),
				new Coordinate(3398468.7831481416, 5752721.003158277),
				new Coordinate(3398502.4963715286, 5752737.628259979),
				new Coordinate(3398536.291982529, 5752756.251640181),
				new Coordinate(3398569.0884476705, 5752774.916209944),
				new Coordinate(3398603.048835546, 5752797.536144485),
				new Coordinate(3398636.0512749613, 5752821.196409043),
				new Coordinate(3398657.774781111, 5752838.315835113),
				new Coordinate(3398678.5403414546, 5752856.475595217),
				new Coordinate(3398710.914403545, 5752889.169308437),
				new Coordinate(3398732.9674811726, 5752914.281849472),
				new Coordinate(3398756.349275503, 5752947.346316085),
				new Coordinate(3398756.3904723814, 5752948.345457085),
				new Coordinate(3398776.692453068, 5752979.535225157),
				new Coordinate(3398795.9952959395, 5753010.766184066),
				new Coordinate(3398824.48599256, 5753070.642636906),
				new Coordinate(3398847.352640919, 5753139.758524219),
				new Coordinate(3398856.993682523, 5753179.394560409),
				new Coordinate(3398864.6776491012, 5753220.112129793),
				new Coordinate(3398868.1178716524, 5753254.999647577),
				new Coordinate(3398870.8061516397, 5753295.923205191),
				new Coordinate(3398873.000054415, 5753324.85707744),
				new Coordinate(3398873.412042433, 5753334.848481132),
				new Coordinate(3398873.9064287217, 5753346.838165521),
				new Coordinate(3398874.689208502, 5753365.821832378),
				new Coordinate(3398871.9283975163, 5753493.042252343),
				new Coordinate(3398872.2579947053, 5753501.035375205),
				new Coordinate(3398871.6913629356, 5753560.108255594),
				new Coordinate(3398869.9091718947, 5753735.328620343),
				new Coordinate(3398870.2799798013, 5753744.320883431),
				new Coordinate(3398864.8001291705, 5753999.760895507),
				new Coordinate(3398865.047342435, 5754005.75573756),
				new Coordinate(3398864.8722476256, 5754025.779746653),
				new Coordinate(3398865.243068776, 5754034.7720097) };
		LineString[] lines = { gfac.createLineString(coords, 4326) };
		MultiLineString poly = gfac.createMultiLineString(lines, 4326);
		SpatialSamplingFeature featureOfInterest = new SpatialSamplingFeature(
				"FOINAME", poly);

		// create resultQuality (optional) and result
		GaussianDistribution[] gauss = { new GaussianDistribution(29.564, 7.45) };
		DQ_UncertaintyResult[] resultQuality = { new DQ_UncertaintyResult(
				gauss, "degC") };

		double[] mean = { 132.52, 384.23 };
		double[] values = { 66.26, 32.3, 232.2, 232.2 };
		CovarianceMatrix cov = new CovarianceMatrix(2, values);
		UncertaintyResult result = new UncertaintyResult(
				new MultivariateGaussianDistribution(mean, cov));

		UncertaintyObservation obs = new UncertaintyObservation(identifier,
				boundedBy, phenomenonTime, resultTime, validTime, procedure,
				observedProperty, featureOfInterest, resultQuality, result);

		///////////////////////////////////////////////////
		// create a data binding
		OMData data = new OMData(obs);
		OMDataBinding binding = new OMDataBinding(data);

		///////////////////////////////////////////////////
		// generate xml
		OMGenerator encoder = new OMGenerator();
		//Node encNode = encoder.generateXML(binding, null);
		XmlObject encDoc = encoder.generateXMLDocument(binding, null);
		
		///////////////////////////////////////////////////
		// test the encoded observation
		UWUncertaintyObservationType encObs = (UWUncertaintyObservationType) ((OMUncertaintyObservationDocument) encDoc).getOMObservation();
		
		// test bounds by comparing the upper and lower border
		assertEquals(encObs.getBoundedBy().getEnvelope().getLowerCorner().getListValue().get(1), obs.getBoundedBy().getMinY());
		assertEquals(encObs.getBoundedBy().getEnvelope().getUpperCorner().getListValue().get(1), obs.getBoundedBy().getMaxY());
		
		// test phenomenon, result and valid time, result time should be a reference to phenomenon time
		assertEquals(((TimeInstantType) encObs.getPhenomenonTime().getAbstractTimePrimitive()).getTimePosition().getStringValue(), obs.getPhenomenonTime().getDateTime().toString());
		assertEquals(encObs.getResultTime().getHref(), "#" + ((TimeInstantType) encObs.getPhenomenonTime().getAbstractTimePrimitive()).getId());
		
		assertEquals(encObs.getValidTime().getTimePeriod().getBegin().getTimeInstant().getTimePosition().getStringValue(), obs.getValidTime().getInterval().getStart().toString());
		assertEquals(encObs.getValidTime().getTimePeriod().getEnd().getTimeInstant().getTimePosition().getStringValue(), obs.getValidTime().getInterval().getEnd().toString());
		
		
		// test procedure and observed property
		assertEquals(encObs.getProcedure().getHref(), obs.getProcedure().toString());
		assertEquals(encObs.getObservedProperty().getHref(), obs.getObservedProperty().toString());
		
		// test feature of interest
		assertEquals(((MultiLineStringType) encObs.getFeatureOfInterest().getSFSpatialSamplingFeature().getShape().getAbstractGeometry()).getLineStringMemberArray(0).getLineString().getPosArray().length, ((MultiLineString) obs.getFeatureOfInterest().getShape()).getCoordinates().length);
		Coordinate c = ((MultiLineString) obs.getFeatureOfInterest().getShape()).getCoordinates()[0];
		assertEquals(((MultiLineStringType) encObs.getFeatureOfInterest().getSFSpatialSamplingFeature().getShape().getAbstractGeometry()).getLineStringMemberArray(0).getLineString().getPosArray(0).getStringValue(), c.x + " " + c.y);
		
		// test result quality and result
		DQUncertaintyResultType dqURT = (DQUncertaintyResultType) ((DQQuantitativeAttributeAccuracyType) encObs.getResultQualityArray(0).getAbstractDQElement()).getResultArray(0).getAbstractDQResult();
		DQ_UncertaintyResult dqUR = (DQ_UncertaintyResult) obs.getResultQuality()[0];
		assertEquals(((GaussianDistributionType) dqURT.getValueArray(0).getAbstractUncertainty()).getMean().getStringValue(), ((GaussianDistribution) dqUR.getValues()[0]).getMean().get(0).toString());
		assertEquals(((GaussianDistributionType) dqURT.getValueArray(0).getAbstractUncertainty()).getVariance().getStringValue(), ((GaussianDistribution) dqUR.getValues()[0]).getVariance().get(0).toString());
		assertEquals(dqURT.getValueUnit().getUnitDefinition().getIdentifier().getStringValue(), dqUR.getUom());
		
	}

	private void obsColTest() throws Exception {
		// read XML example file
		String xmlString;
		try {
			xmlString = readXmlFile(pathToExamples
					+ "/ObsCol_UncertaintyObs.xml");
		} catch (IOException ioe) {
			xmlString = readXmlFile(localPath + pathToExamples
					+ "/ObsCol_UncertaintyObs.xml");
		}
		OMParser parser = new OMParser();
		IData binding = parser.parseXML(xmlString);
		OMGenerator encoder = new OMGenerator();
		Node encDoc = encoder.generateXML(binding, null);
		String encString = encoder.generateXMLDocument(binding, null)
				.toString();

	}

	private String readXmlFile(String filePath) throws IOException {
		String result = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath)));
		try {
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} finally {
			in.close();
		}
		return result;
	}
}
