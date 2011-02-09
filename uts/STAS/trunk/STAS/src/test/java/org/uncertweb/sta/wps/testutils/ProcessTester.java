package org.uncertweb.sta.wps.testutils;
import static org.uncertweb.intamap.utils.Namespace.GML;
import static org.uncertweb.intamap.utils.Namespace.OM;
import static org.uncertweb.intamap.utils.Namespace.SOS;
import static org.uncertweb.intamap.utils.Namespace.WFS;
import static org.uncertweb.intamap.utils.Namespace.defaultOptions;
import static org.uncertweb.sta.utils.Constants.DESTINATION_SOS_URL_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.FEATURE_COLLECTION_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.GROUP_BY_OBSERVED_PROPERTY_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.SOURCE_SOS_REQUEST_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.SOURCE_SOS_URL_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.SPATIAL_AGGREGATION_METHOD_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.TEMPORAL_AGGREGATION_METHOD_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.TIME_RANGE_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.UTF8_ENCODING;
import static org.uncertweb.sta.utils.Constants.WFS_REQUEST_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.WFS_URL_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.XML_MIME_TYPE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.wfs.GetFeatureDocument;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.geronimo.mail.util.StringBufferOutputStream;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.feature.FeatureCollection;
import org.geotools.util.logging.Logging;
import org.joda.time.Period;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.ExecuteResponseAnalyser;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.utils.TimeUtils;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.STARepository;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.grouping.spatial.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.TemporalGrouping;
import org.uncertweb.sta.wps.xml.binding.GetFeatureRequestBinding;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;

public class ProcessTester {

	private static final String CONFIG_PATH = ProcessTester.class.getResource("/wps_config/wps_config.xml").getFile();
	private static final String OFFLINE_CONFIG_PATH = ProcessTester.class.getResource("/test_wps_config.xml").getFile();
	private static final String WFS_REQ_SCHEMA = WFS.SCHEMA;
	private static final String FC_SCHEMA = GML.SCHEMA;
	private static final String SOS_REQ_SCHEMA = SOS.SCHEMA;
	private static final Logger log = LoggerFactory.getLogger(ProcessTester.class);
	
	static {
		Logging.GEOTOOLS.forceMonolineConsoleOutput();
		try {
			WPSConfig.forceInitialization(CONFIG_PATH);
		} catch (XmlException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ParserFactory.initialize(WPSConfig.getInstance().getRegisteredParser());
		GeneratorFactory.initialize(WPSConfig.getInstance().getRegisteredGenerator());
	}

	private ProcessTester() {}
	private static ProcessTester pt = null;
	public static ProcessTester getInstance() {
		if (pt == null) {
			pt = new ProcessTester();
		}
		return pt;
	}
	
	public ObservationCollection getObservationCollection() {
		return oc;
	}
	
	private boolean offline = false;
	public void setOffline(boolean offline) {
		if (this.offline != offline) {
			this.offline = offline;
			try {
				WPSConfig.forceInitialization((offline) ? OFFLINE_CONFIG_PATH : CONFIG_PATH);
			} catch (XmlException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static STARepository sta;
	private static AbstractXMLParser gmlParser;
	private static AbstractXMLParser omParser;
	private static IStreamableGenerator omGenerator;

	private IAlgorithm process;
	
	/* common inputs */
	private URL sosSrcUrl;
	private URL sosDestUrl;
	private GetObservationDocument sosRequest;
	private ObservationCollection oc;
	private Class<? extends AggregationMethod> temporalAM;
	private Class<? extends AggregationMethod> spatialAM;
	private Boolean groupByObservedProperty;
	private Boolean temporalBeforeSpatial;
	
	/* time range inputs */
	private Period p;
	
	/* coverage grouping inputs */
	private URL wfsUrl;
	private GetFeatureDocument wfsRequest;
	private FeatureCollection<?, ?> fc;

	/* outputs */
	private ObservationCollection ocOutput = null;
	private GetObservationDocument refOutput = null;
	
	public void reset() {
		p = null;
		wfsUrl = null;
		sosDestUrl = null;
		sosSrcUrl = null;
		wfsRequest = null;
		sosRequest = null;
		temporalAM = null;
		spatialAM = null;
		groupByObservedProperty = null;
		temporalBeforeSpatial = null;
		ocOutput = null;
		refOutput = null;
		process = null;
	}

	public void selectAlgorithm(String id) {
		process = getRepository().getAlgorithm(id, null);
	}

	public void selectAlgorithm(Class<? extends SpatialGrouping> sg,
			Class<? extends TemporalGrouping> tg) {
		String sgN = sg.getName(), tgN = tg.getName();
		selectAlgorithm(sgN.substring(sgN.lastIndexOf('.') + 1, sgN.length())
				+ "." + tgN.substring(tgN.lastIndexOf('.') + 1, tgN.length()));
	}

	public IAlgorithm getSelectedAlgorithm() {
		if (process == null)
			throw new NullPointerException();
		return process;
	}

	public void setSosSourceUrl(String url) {
		try {
			this.sosSrcUrl = new URL(testNull(url));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setSosRequest(String request) {
		try {
			setSosRequest(GetObservationDocument.Factory.parse(testNull(request)));
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}

	public void setSosRequest(GetObservationDocument request) {
		this.sosRequest = testNull(request);
	}

	public void setSosDestinationUrl(String url) {
		try {
			this.sosDestUrl = new URL(testNull(url));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setSpatialAggregationMethod(Class<? extends AggregationMethod> sam) {
		this.spatialAM = testNull(sam);
	}

	@SuppressWarnings("unchecked")
	public void setSpatialAggregationMethod(String sam) {
		try {
			setSpatialAggregationMethod((Class<? extends AggregationMethod>) 
					Class.forName(testNull(sam)));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void setTemporalAggregationMethod(Class<? extends AggregationMethod> tam) {
		this.temporalAM = testNull(tam);
	}
	
	public void setObservationCollection(ObservationCollection oc) {
		this.oc = oc;
	}

	@SuppressWarnings("unchecked")
	public void setTemporalAggregationMethod(String tam) {
		try {
			setTemporalAggregationMethod((Class<? extends AggregationMethod>) 
					Class.forName(testNull(tam)));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void setTemporalBeforeSpatialAggregation(Boolean flag) {
		this.temporalBeforeSpatial = testNull(flag);
	}

	public void setGroupByObservedProperty(Boolean flag) {
		this.groupByObservedProperty = testNull(flag);
	}

	public void setTimeRange(String p) {
		setTimeRange(TimeUtils.parsePeriod(testNull(p)));
	}

	public void setTimeRange(Period p) {
		this.p = testNull(p);
	}

	public void setWfsUrl(String url) {
		try {
			this.wfsUrl = new URL(testNull(url));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setWfsRequest(String request) {
		try {
			setWfsRequest(GetFeatureDocument.Factory.parse(testNull(request)));
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}

	public void setWfsRequest(GetFeatureDocument request) {
		this.wfsRequest = testNull(request);
	}
	
	public void setFeatureCollection(FeatureCollection<?, ?> fc) {
		fc = testNull(fc);
	}
	
	public void execute() {
		execute(null);
	}
	
	public void execute(String wpsUrl) {
		ProcessDescriptionType desc = getSelectedAlgorithm().getDescription();
		ExecuteRequestBuilder eb = new ExecuteRequestBuilder(desc);
		if (p != null) {
			eb.addLiteralData(TIME_RANGE_INPUT_ID, TimeUtils.format(p));
		}
		if (wfsUrl != null) {
			log.info("Adding '{}' Parameter", WFS_URL_INPUT_ID);
			eb.addLiteralData(WFS_URL_INPUT_ID, wfsUrl.toExternalForm());
		}
		if (sosDestUrl != null) {
			log.info("Adding '{}' Parameter", DESTINATION_SOS_URL_INPUT_ID);
			eb.addLiteralData(DESTINATION_SOS_URL_INPUT_ID, sosDestUrl.toExternalForm());
		}
		if (temporalAM != null) {
			log.info("Adding '{}' Parameter", TEMPORAL_AGGREGATION_METHOD_INPUT_ID);
			eb.addLiteralData(TEMPORAL_AGGREGATION_METHOD_INPUT_ID, temporalAM.getName());
		}
		if (spatialAM != null) {
			log.info("Adding '{}' Parameter", SPATIAL_AGGREGATION_METHOD_INPUT_ID);
			eb.addLiteralData(SPATIAL_AGGREGATION_METHOD_INPUT_ID, spatialAM.getName());
		}
		if (groupByObservedProperty != null) {
			log.info("Adding '{}' Parameter", GROUP_BY_OBSERVED_PROPERTY_INPUT_ID);
			eb.addLiteralData(GROUP_BY_OBSERVED_PROPERTY_INPUT_ID, groupByObservedProperty.toString());
		}
		if (temporalBeforeSpatial != null) {
			log.info("Adding '{}' Parameter", TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_ID);
			eb.addLiteralData(TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_ID, temporalBeforeSpatial.toString());
		}
		if (wfsRequest != null) {
			log.info("Adding '{}' Parameter", WFS_REQUEST_INPUT_ID);
			eb.addComplexData(WFS_REQUEST_INPUT_ID, new GetFeatureRequestBinding(wfsRequest), WFS_REQ_SCHEMA, UTF8_ENCODING, XML_MIME_TYPE);
		}
		if (sosSrcUrl != null) {	
			log.info("Adding '{}' Parameter", SOURCE_SOS_URL_INPUT_ID);
			eb.addLiteralData(SOURCE_SOS_URL_INPUT_ID, sosSrcUrl.toExternalForm());
		}
		if (sosRequest != null) {
			log.info("Adding '{}' Parameter", SOURCE_SOS_REQUEST_INPUT_ID);
			eb.addComplexData(SOURCE_SOS_REQUEST_INPUT_ID, new GetObservationRequestBinding(sosRequest), SOS_REQ_SCHEMA, UTF8_ENCODING, XML_MIME_TYPE);
		}
		if (fc != null) {
			log.info("Adding '{}' Parameter", FEATURE_COLLECTION_INPUT_ID);
			eb.addComplexData(FEATURE_COLLECTION_INPUT_ID, new GTVectorDataBinding(fc), FC_SCHEMA, UTF8_ENCODING, XML_MIME_TYPE);
		}
		
		ExecuteDocument exec = eb.getExecute();
	
		//TODO outputs...
		
		DocumentOutputDefinitionType dodt = exec.getExecute().addNewResponseForm().addNewResponseDocument().addNewOutput();
		dodt.addNewIdentifier().setStringValue(Constants.OBSERVATION_COLLECTION_OUTPUT_ID);
		
		if (sosDestUrl != null) {
			dodt = exec.getExecute().getResponseForm().getResponseDocument().addNewOutput();
			dodt.addNewIdentifier().setStringValue(Constants.OBSERVATION_COLLECTION_OUTPUT_ID);	
		}
		
		if (!exec.validate()) {
			log.error("Invalid request:\n{}", exec.xmlText(defaultOptions()));
			throw new RuntimeException("Invalid Request");
			
		}
		
		try {
			XmlObject res = null;
			if (wpsUrl == null) {
				// execute locally
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				new RequestHandler(exec.newInputStream(), os).handle();
				res = XmlObject.Factory.parse(os.toString());
			} else {
				// execute remote
				res = XmlObject.Factory.parse(Utils.sendPostRequest(wpsUrl, exec.xmlText()));
			}
			if (res instanceof ExecuteResponseDocument) {
				ExecuteResponseAnalyser ers = new ExecuteResponseAnalyser((ExecuteResponseDocument) res, desc);
				try {
					ocOutput = ((ObservationCollectionBinding) ers.getComplexData(Constants.OBSERVATION_COLLECTION_OUTPUT_ID)).getPayload();
				} catch (IllegalArgumentException e) {
					/* ignore if it was not requested */
				}
				try {
					refOutput = ((GetObservationRequestBinding) ers.getComplexData(Constants.OBSERVATION_COLLECTION_REFERENCE_OUTPUT_ID)).getPayload();
				} catch (IllegalArgumentException e) {
					/* ignore if it was not requested */
				}
			} else if (res instanceof ExceptionReport) {
				throw new RuntimeException(res.xmlText(defaultOptions()));
			}
		} catch (ExceptionReport e) {
			throw new RuntimeException(e);
		} catch (XmlException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public GetObservationDocument getReferenceOutput() {
		if (refOutput == null) throw new RuntimeException("Not yet executed.");
		return refOutput;
	}

	public ObservationCollection getOutput() {
		if (ocOutput == null) throw new RuntimeException("Not yet executed.");
		return ocOutput;
	}
	
	private <T> T testNull(T t) {
		if (t == null) 
			throw new NullPointerException();
		ocOutput = null;
		refOutput = null;
		return t;
	}

	public static IAlgorithmRepository getRepository() {
		if (sta == null) {
			sta = new OfflineRepository();
		}
		return sta;
	}

	public static Collection<IAlgorithm> getAlgorithms() {
		return getRepository().getAlgorithms();
	}
	
	public static IStreamableGenerator getOMGenerator() {
		if (omGenerator == null) {
			omGenerator = (IStreamableGenerator) GeneratorFactory.getInstance()
					.getGenerator(OM.SCHEMA,
							Constants.XML_MIME_TYPE, Constants.UTF8_ENCODING,
							ObservationCollectionBinding.class);
		}
		return omGenerator;
	}

	public static AbstractXMLParser getGMLParser() {
		if (gmlParser == null) {
			gmlParser = (AbstractXMLParser) ParserFactory
					.getInstance()
					.getParser(
							"http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd",
							Constants.XML_MIME_TYPE, Constants.UTF8_ENCODING,
							GTVectorDataBinding.class);
		}
		return gmlParser;
	}

	public static AbstractXMLParser getOMParser() {
		if (omParser == null) {
			omParser = (AbstractXMLParser) ParserFactory.getInstance()
					.getParser(OM.SCHEMA, Constants.XML_MIME_TYPE,
							Constants.UTF8_ENCODING,
							ObservationCollectionBinding.class);
		}
		return omParser;
	}
	
	public static void print(ObservationCollection oc) throws XmlException {
		StringBuffer sb = new StringBuffer();
		StringBufferOutputStream out = new StringBufferOutputStream(sb);
		getOMGenerator().writeToStream(new ObservationCollectionBinding(oc), out);
		System.out.println(XmlObject.Factory.parse(sb.toString()).xmlText(
				defaultOptions()));
	}
	public static void main(String[] args) {
		ProcessTester pt = new ProcessTester();
		pt.setOffline(true);
	}
}