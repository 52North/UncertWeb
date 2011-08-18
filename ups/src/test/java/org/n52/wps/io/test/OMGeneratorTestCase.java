package org.n52.wps.io.test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.opengis.gml.x32.MultiLineStringType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.om.x20.OMAbstractObservationType;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument.OMUncertaintyObservationCollection;
import net.opengis.om.x20.OMUncertaintyObservationDocument;
import net.opengis.om.x20.UWUncertaintyObservationType;

import org.apache.xmlbeans.XmlObject;
import org.isotc211.x2005.gmd.DQQuantitativeAttributeAccuracyType;
import org.isotc211.x2005.gmd.DQUncertaintyResultType;
import org.n52.wps.io.data.OMData;
import org.n52.wps.io.data.binding.complex.OMDataBinding;
import org.n52.wps.io.datahandler.xml.OMGenerator;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertml.statistic.CovarianceMatrix;
import org.uncertml.x20.NormalDistributionType;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.gml.geometry.GmlGeometryFactory;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * JUnit tests for O&M encoding
 * 
 * @author Kiesow
 * 
 */
public class OMGeneratorTestCase extends TestCase {

	public void testObservationGenerator() throws Exception {

		obsTest();
		obsColTest();
	}

	/*
	 * This test rebuilds the first observation from
	 * src/main/resources/ObsCol_UncertaintyObs.xml but adds validTime (to test
	 * time periods) and resultQuality
	 */
	private void obsTest() throws Exception {

		// /////////////////////////////////////////////////
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
		TimeObject validTime = new TimeObject("2005-01-13T16:22:25.000+01:00",
				"2005-01-14T16:22:25.000+01:00");

		// create procedure and observed property
		URI procedure = new URI(
				"http://www.uncertweb.org/phenomenon/PM10_total");
		URI observedProperty = new URI(
				"http://www.uncertweb.org/phenomenon/PM10_total");

		// create spatial feature which manages the spatial support
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
		NormalDistribution[] gauss = { new NormalDistribution(29.564, 7.45) };
		DQ_UncertaintyResult[] resultQuality = { new DQ_UncertaintyResult(
				gauss, "degC") };

		double[] mean = { 132.52, 384.23 };
		double[] values = { 66.26, 32.3, 232.2, 232.2 };
		CovarianceMatrix cov = new CovarianceMatrix(2, values);
		UncertaintyResult result = new UncertaintyResult(
				new MultivariateNormalDistribution(mean, cov));

		UncertaintyObservation obs = new UncertaintyObservation(identifier,
				boundedBy, phenomenonTime, resultTime, validTime, procedure,
				observedProperty, featureOfInterest, resultQuality, result);

		// /////////////////////////////////////////////////
		// create a data binding
		OMData data = new OMData(obs);
		OMDataBinding binding = new OMDataBinding(data);

		// /////////////////////////////////////////////////
		// generate xml
		OMGenerator encoder = new OMGenerator();
		XmlObject encDoc = encoder.generateXMLDocument(binding, null);

		// /////////////////////////////////////////////////
		// test the encoded observation
		UWUncertaintyObservationType encObs = (UWUncertaintyObservationType) ((OMUncertaintyObservationDocument) encDoc)
				.getOMObservation();

		singleObsTest(obs, encObs);
	}

	/*
	 * This test rebuilds the first three observations from
	 * src/main/resources/ObsCol_UncertaintyObs.xml
	 */
	private void obsColTest() throws Exception {

		// /////////////////////////////////////////////////
		// create an observation collection by using the om api classes
		// create a list for all observations
		List<UncertaintyObservation> obss = new ArrayList<UncertaintyObservation>();

		// create 1st observation
		// create phenomenon result and valid (optional) time
		TimeObject phenomenonTime = new TimeObject(
				"2010-01-22T14:55:02.000+02:00");
		TimeObject resultTime = new TimeObject("2010-01-22T14:55:02.000+02:00");

		// create procedure and observed property
		URI procedure = new URI(
				"http://www.uncertweb.org/phenomenon/PM10_total");
		URI observedProperty = new URI(
				"http://www.uncertweb.org/phenomenon/PM10_total");

		// create spatial feature which manages the spatial support
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
				"FOINAME1", poly);

		// create result
		double[] mean = { 132.52, 384.23 };
		double[] values = { 66.26, 32.3, 232.2, 232.2 };
		CovarianceMatrix cov = new CovarianceMatrix(2, values);
		UncertaintyResult result = new UncertaintyResult(
				new MultivariateNormalDistribution(mean, cov));

		// create and add an observation from the parameters above
		UncertaintyObservation obs1 = new UncertaintyObservation(
				phenomenonTime, resultTime, procedure, observedProperty,
				featureOfInterest, result);
		obss.add(obs1);

		// create 2nd observation
		// create phenomenon result and valid (optional) time
		phenomenonTime = new TimeObject("2010-01-22T15:55:02.000+02:00");
		resultTime = new TimeObject("2010-01-22T15:55:02.000+02:00");

		// create procedure and observed property
		procedure = new URI("http://www.uncertweb.org/phenomenon/PM10_total");
		observedProperty = new URI(
				"http://www.uncertweb.org/phenomenon/PM10_total");

		// create spatial feature which manages the spatial support
		gfac = new GmlGeometryFactory();
		Coordinate[] coords2 = {
				new Coordinate(3404375.988318778, 5746051.243281115),
				new Coordinate(3404376.860169614, 5746145.283406132),
				new Coordinate(3404377.9303791304, 5746171.260348892),
				new Coordinate(3404378.9594299705, 5746196.238178274),
				new Coordinate(3404384.643532795, 5746237.037168832),
				new Coordinate(3404391.955407775, 5746268.761814327),
				new Coordinate(3404396.5692976327, 5746283.583860615),
				new Coordinate(3404399.8547747163, 5746290.454164404),
				new Coordinate(3404405.4677790734, 5746305.235046974),
				new Coordinate(3404412.079897391, 5746319.974766006),
				new Coordinate(3404428.754267337, 5746360.320954502),
				new Coordinate(3404442.3489795662, 5746398.792400049),
				new Coordinate(3404450.495338489, 5746426.4794163685),
				new Coordinate(3404465.912445074, 5746484.891944026),
				new Coordinate(3404488.353341303, 5746568.0352944415),
				new Coordinate(3404526.046780976, 5746705.594670743),
				new Coordinate(3404563.657979867, 5746841.155772077),
				new Coordinate(3404583.730301173, 5746915.389366795),
				new Coordinate(3404593.164000896, 5746950.028961626),
				new Coordinate(3404614.2766388543, 5747025.22048062),
				new Coordinate(3404623.545690872, 5747055.863617837),
				new Coordinate(3404641.1258630725, 5747118.190162415),
				new Coordinate(3404643.5357542904, 5747128.098946333),
				new Coordinate(3404645.945645983, 5747138.007730019),
				new Coordinate(3404647.315259537, 5747146.958568653),
				new Coordinate(3404649.6839852, 5747155.8682402335),
				new Coordinate(3404652.340880245, 5747171.771693452),
				new Coordinate(3404655.203612102, 5747192.670704645),
				new Coordinate(3404658.6015205593, 5747226.558166807),
				new Coordinate(3404677.918718179, 5747403.906018028),
				new Coordinate(3404682.9744687015, 5747453.738089254),
				new Coordinate(3404691.0988425147, 5747529.464718868),
				new Coordinate(3404694.373301517, 5747560.3548360085),
				new Coordinate(3404701.128080492, 5747627.130624535),
				new Coordinate(3404702.580054741, 5747638.079680534),
				new Coordinate(3404704.9377583507, 5747671.009186734),
				new Coordinate(3404720.5360828303, 5747879.535263477),
				new Coordinate(3404726.2396798767, 5747969.372935059),
				new Coordinate(3404727.515985032, 5748000.345383333),
				new Coordinate(3404734.6414565886, 5748076.113149544),
				new Coordinate(3404740.7788310666, 5748127.902243397),
				new Coordinate(3404750.7369957375, 5748199.550039891),
				new Coordinate(3404751.8486475307, 5748226.526039292),
				new Coordinate(3404752.960303014, 5748253.502038471),
				new Coordinate(3404750.734280626, 5748296.628505331),
				new Coordinate(3404749.1889585983, 5748307.70107277),
				new Coordinate(3404747.849501366, 5748323.76919588) };
		LineString[] lines2 = { gfac.createLineString(coords2, 4326) };
		poly = gfac.createMultiLineString(lines2, 4326);
		featureOfInterest = new SpatialSamplingFeature("FOINAME2", poly);

		// create result
		double[] mean2 = { 133.52, 384.23 };
		double[] values2 = { 67.26, 32.3, 232.2, 232.2 };
		cov = new CovarianceMatrix(2, values2);
		result = new UncertaintyResult(new MultivariateNormalDistribution(
				mean2, cov));

		// create and add an observation from the parameters above
		UncertaintyObservation obs2 = new UncertaintyObservation(
				phenomenonTime, resultTime, procedure, observedProperty,
				featureOfInterest, result);
		obss.add(obs2);

		// create 3rd observation
		// create phenomenon result and valid (optional) time
		phenomenonTime = new TimeObject("2010-01-22T16:55:02.000+02:00");
		resultTime = new TimeObject("2010-01-22T16:55:02.000+02:00");

		// create procedure and observed property
		procedure = new URI("http://www.uncertweb.org/phenomenon/PM10_total");
		observedProperty = new URI(
				"http://www.uncertweb.org/phenomenon/PM10_total");

		// create spatial feature which manages the spatial support
		gfac = new GmlGeometryFactory();
		Coordinate[] coords3 = {
				new Coordinate(3400175.7513762508, 5749310.847289848),
				new Coordinate(3400185.2789019956, 5749323.465428799),
				new Coordinate(3400196.763517832, 5749335.002075613),
				new Coordinate(3400212.3270266703, 5749348.372275131),
				new Coordinate(3400228.807312033, 5749359.703026167),
				new Coordinate(3400243.1246146886, 5749367.119597187),
				new Coordinate(3400257.3595593777, 5749372.5378995985),
				new Coordinate(3400272.634815525, 5749378.914156253),
				new Coordinate(3400297.942585145, 5749385.877758757),
				new Coordinate(3400338.484424849, 5749398.2184789255),
				new Coordinate(3400350.6799183083, 5749402.72000058),
				new Coordinate(3400369.0761042773, 5749411.970114962),
				new Coordinate(3400387.4722890016, 5749421.220227591),
				new Coordinate(3400400.749270778, 5749427.678833838),
				new Coordinate(3400420.1857651984, 5749437.886897477),
				new Coordinate(3400579.797738646, 5749522.384012138),
				new Coordinate(3400741.4490643954, 5749607.797759871),
				new Coordinate(3400758.804913358, 5749616.089884841),
				new Coordinate(3400793.516607842, 5749632.67413033),
				new Coordinate(3400838.5078656278, 5749655.840485838),
				new Coordinate(3400846.7068117163, 5749660.506699661),
				new Coordinate(3401033.7480341564, 5749754.882002544),
				new Coordinate(3401039.866354192, 5749757.632308991),
				new Coordinate(3401043.986414628, 5749760.464976257),
				new Coordinate(3401065.462304112, 5749771.589741367),
				new Coordinate(3401086.938191938, 5749782.7145040175),
				new Coordinate(3401106.4569999557, 5749794.920754941),
				new Coordinate(3401165.0957789654, 5749833.537751291),
				new Coordinate(3401183.656634513, 5749846.7843021825),
				new Coordinate(3401201.259541324, 5749861.071160473),
				new Coordinate(3401217.863318818, 5749875.3991972655),
				new Coordinate(3401241.872808713, 5749899.430250871),
				new Coordinate(3401245.0761007983, 5749904.302350818),
				new Coordinate(3401256.5606909567, 5749915.8389012525),
				new Coordinate(3401267.0873338925, 5749928.415760249),
				new Coordinate(3401276.6148485653, 5749941.033799295),
				new Coordinate(3401285.184416579, 5749954.692147075),
				new Coordinate(3401293.7951667197, 5749969.349622122),
				new Coordinate(3401302.405917601, 5749984.007095905),
				new Coordinate(3401310.0587225473, 5749999.704878461),
				new Coordinate(3401313.385561217, 5750007.574360151),
				new Coordinate(3401325.487880918, 5750034.097824814),
				new Coordinate(3401338.537390594, 5750083.601235098),
				new Coordinate(3401341.1121969554, 5750097.506665598),
				new Coordinate(3401345.1391374506, 5750122.361323383),
				new Coordinate(3401348.166952868, 5750147.2571626445),
				new Coordinate(3401352.0184234637, 5750192.135563807),
				new Coordinate(3401359.4026663834, 5750249.879079904),
				new Coordinate(3401363.1825274783, 5750268.738964313),
				new Coordinate(3401367.0035734577, 5750288.597976074),
				new Coordinate(3401371.7001996944, 5750305.418419822),
				new Coordinate(3401376.438010584, 5750323.237990788),
				new Coordinate(3401382.1337675466, 5750340.017249566),
				new Coordinate(3401390.2396167354, 5750366.705419603),
				new Coordinate(3401409.1192382853, 5750411.965179392),
				new Coordinate(3401411.2822289937, 5750415.879323085),
				new Coordinate(3401485.6369395126, 5750592.9629491065),
				new Coordinate(3401565.6875740793, 5750786.825675473),
				new Coordinate(3401613.5923421565, 5750904.949902793),
				new Coordinate(3401639.1258117957, 5750965.94846881),
				new Coordinate(3401649.0652427957, 5750988.557700449),
				new Coordinate(3401659.9626156506, 5751010.126616598),
				new Coordinate(3401678.2245572577, 5751040.399359787),
				new Coordinate(3401688.792438233, 5751053.97525931),
				new Coordinate(3401699.3191325543, 5751066.552031106),
				new Coordinate(3401708.8055138057, 5751078.170862449),
				new Coordinate(3401718.2507081456, 5751088.790566501),
				new Coordinate(3401728.7362159737, 5751100.36820845),
				new Coordinate(3401915.652493026, 5751288.824238702),
				new Coordinate(3401934.4193208753, 5751307.066228234),
				new Coordinate(3401940.661200728, 5751312.813848964) };
		LineString[] lines3 = { gfac.createLineString(coords3, 4326) };
		poly = gfac.createMultiLineString(lines3, 4326);
		featureOfInterest = new SpatialSamplingFeature("FOINAME3", poly);

		// create result
		double[] mean3 = { 134.52, 384.23 };
		double[] values3 = { 68.26, 32.3, 232.2, 232.2 };
		cov = new CovarianceMatrix(2, values3);
		result = new UncertaintyResult(new MultivariateNormalDistribution(
				mean3, cov));

		// create and add an observation from the parameters above
		UncertaintyObservation obs3 = new UncertaintyObservation(
				phenomenonTime, resultTime, procedure, observedProperty,
				featureOfInterest, result);
		obss.add(obs3);

		// create the actual collection
		UncertaintyObservationCollection obsCol = new UncertaintyObservationCollection(
				obss);

		// /////////////////////////////////////////////////
		// create a data binding
		OMData data = new OMData(obsCol);
		OMDataBinding binding = new OMDataBinding(data);

		// /////////////////////////////////////////////////
		// generate xml
		OMGenerator encoder = new OMGenerator();
		XmlObject encDoc = encoder.generateXMLDocument(binding, null);

		// /////////////////////////////////////////////////
		// test the encoded observation
		OMUncertaintyObservationCollection encObsCol = (OMUncertaintyObservationCollection) ((OMUncertaintyObservationCollectionDocument) encDoc)
				.getOMUncertaintyObservationCollection();

		singleObsTest(obs1, encObsCol.getOMUncertaintyObservationArray(0));
		singleObsTest(obs2, encObsCol.getOMUncertaintyObservationArray(1));
		singleObsTest(obs3, encObsCol.getOMUncertaintyObservationArray(2));

	}

	/*
	 * method, capable of testing OMUncertaintyObservations (for now)
	 */
	private void singleObsTest(AbstractObservation obs,
			OMAbstractObservationType encObs) {

		// test bounds by comparing the upper and lower border
		if (encObs.getBoundedBy() != null) {
			assertEquals(encObs.getBoundedBy().getEnvelope().getLowerCorner()
					.getListValue().get(1), obs.getBoundedBy().getMinY());
			assertEquals(encObs.getBoundedBy().getEnvelope().getUpperCorner()
					.getListValue().get(1), obs.getBoundedBy().getMaxY());
		}

		// test phenomenon, result and valid time (optional), result time should
		// be a reference to phenomenon time
		assertEquals(
				((TimeInstantType) encObs.getPhenomenonTime()
						.getAbstractTimePrimitive()).getTimePosition()
						.getStringValue(), obs.getPhenomenonTime()
						.getDateTime().toString());
		assertEquals(encObs.getResultTime().getHref(), "#"
				+ ((TimeInstantType) encObs.getPhenomenonTime()
						.getAbstractTimePrimitive()).getId());

		if (obs.getValidTime() != null) {
			assertEquals(encObs.getValidTime().getTimePeriod().getBegin()
					.getTimeInstant().getTimePosition().getStringValue(), obs
					.getValidTime().getInterval().getStart().toString());
			assertEquals(encObs.getValidTime().getTimePeriod().getEnd()
					.getTimeInstant().getTimePosition().getStringValue(), obs
					.getValidTime().getInterval().getEnd().toString());
		}

		// test procedure and observed property
		assertEquals(encObs.getProcedure().getHref(), obs.getProcedure()
				.toString());
		assertEquals(encObs.getObservedProperty().getHref(), obs
				.getObservedProperty().toString());

		// test feature of interest
		assertEquals(
				((MultiLineStringType) encObs.getFeatureOfInterest()
						.getSFSpatialSamplingFeature().getShape()
						.getAbstractGeometry()).getLineStringMemberArray(0)
						.getLineString().getPosArray().length,
				((MultiLineString) obs.getFeatureOfInterest().getShape())
						.getCoordinates().length);
		Coordinate c = ((MultiLineString) obs.getFeatureOfInterest().getShape())
				.getCoordinates()[0];
		assertEquals(
				((MultiLineStringType) encObs.getFeatureOfInterest()
						.getSFSpatialSamplingFeature().getShape()
						.getAbstractGeometry()).getLineStringMemberArray(0)
						.getLineString().getPosArray(0).getStringValue(), c.x
						+ " " + c.y);

		// test result quality (optional)
		if (encObs.getResultQualityArray() != null
				&& encObs.getResultQualityArray().length > 0) {
			DQUncertaintyResultType dqURT = (DQUncertaintyResultType) ((DQQuantitativeAttributeAccuracyType) encObs
					.getResultQualityArray(0).getAbstractDQElement())
					.getResultArray(0).getAbstractDQResult();
			DQ_UncertaintyResult dqUR = (DQ_UncertaintyResult) obs
					.getResultQuality()[0];

			assertEquals(
					((NormalDistributionType) dqURT.getValueArray(0)
							.getAbstractUncertainty()).getMean()
							.getStringValue(),
					((NormalDistribution) dqUR.getValues()[0]).getMean()
							.get(0).toString());

			assertEquals(((NormalDistributionType) dqURT.getValueArray(0)
					.getAbstractUncertainty()).getVariance().getStringValue(),
					((NormalDistribution) dqUR.getValues()[0]).getVariance()
							.get(0).toString());

			assertEquals(dqURT.getValueUnit().getUnitDefinition()
					.getIdentifier().getStringValue(), dqUR.getUom());
		}

		// test result
		org.uncertml.x20.MultivariateNormalDistributionDocument.MultivariateNormalDistribution encMGD = (org.uncertml.x20.MultivariateNormalDistributionDocument.MultivariateNormalDistribution) ((UWUncertaintyObservationType) encObs)
				.getResult().getAbstractUncertainty();
		MultivariateNormalDistribution mGD = (MultivariateNormalDistribution) obs
				.getResult().getValue();
		assertEquals((Double) encMGD.getMean().getListValue().get(0), mGD
				.getMean().get(0));
		assertEquals(encMGD.getCovarianceMatrix().getValues().getListValue()
				.get(0), mGD.getCovarianceMatrix().getValues().get(0));

	}
}
