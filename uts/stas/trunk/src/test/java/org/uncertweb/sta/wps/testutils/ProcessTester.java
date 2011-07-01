/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.sta.wps.testutils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javax.xml.namespace.QName;

import net.opengis.gml.TimePeriodType;
import net.opengis.ogc.BinaryTemporalOpType;
import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.GetObservationDocument.GetObservation;
import net.opengis.sos.x10.GetObservationDocument.GetObservation.EventTime;
import net.opengis.sos.x10.ResponseModeType;
import net.opengis.wfs.GetFeatureDocument;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.geronimo.mail.util.StringBufferOutputStream;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.feature.FeatureCollection;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IOHandler;
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
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.intamap.utils.TimeUtils;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.STARepository;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;
import org.uncertweb.sta.wps.xml.binding.GetFeatureRequestBinding;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;
import org.uncertweb.sta.wps.xml.io.dec.GetObservationRequestParser;

/**
 * TODO JavaDoc
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ProcessTester {

	private static final String CONFIG_PATH = ProcessTester.class
			.getResource("/wps_config/wps_config.xml").getFile();
	protected static final Logger log = LoggerFactory
			.getLogger(ProcessTester.class);

	static {
		// Logging.GEOTOOLS.forceMonolineConsoleOutput();
		try {
			WPSConfig.forceInitialization(CONFIG_PATH);
		} catch (XmlException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ParserFactory.initialize(WPSConfig.getInstance().getRegisteredParser());
		GeneratorFactory.initialize(WPSConfig.getInstance()
				.getRegisteredGenerator());
	}

	public ObservationCollection getObservationCollection() {
		return oc;
	}

	private static STARepository sta;
	private static AbstractXMLParser gmlParser;
	private static AbstractXMLParser omParser;
	private static AbstractXMLParser getObsParser = null;
	private static IStreamableGenerator omGenerator;

	public static IAlgorithmRepository getRepository() {
		if (sta == null) {
			sta = new STARepository();
		}
		return sta;
	}

	public static Collection<IAlgorithm> getAlgorithms() {
		return getRepository().getAlgorithms();
	}

	public static IStreamableGenerator getOMGenerator() {
		if (omGenerator == null) {
			omGenerator = (IStreamableGenerator) GeneratorFactory
					.getInstance()
					.getGenerator(Namespace.OM.SCHEMA, IOHandler.DEFAULT_MIMETYPE, IOHandler.DEFAULT_ENCODING, ObservationCollectionBinding.class);
		}
		return omGenerator;
	}

	public static AbstractXMLParser getGMLParser() {
		if (gmlParser == null) {
			gmlParser = (AbstractXMLParser) ParserFactory
					.getInstance()
					.getParser(Namespace.WFS.SCHEMA, IOHandler.DEFAULT_MIMETYPE, IOHandler.DEFAULT_ENCODING, GTVectorDataBinding.class);
		}
		return gmlParser;
	}

	public static AbstractXMLParser getGetObsParser() {
		if (getObsParser == null) {
			getObsParser = (AbstractXMLParser) ParserFactory
					.getInstance()
					.getParser(Namespace.SOS.SCHEMA, IOHandler.DEFAULT_MIMETYPE, IOHandler.DEFAULT_ENCODING, GetObservationRequestBinding.class);
		}
		return getObsParser;
	}

	public static AbstractXMLParser getOMParser() {
		if (omParser == null) {
			omParser = (AbstractXMLParser) ParserFactory
					.getInstance()
					.getParser(Namespace.OM.SCHEMA, IOHandler.DEFAULT_MIMETYPE, IOHandler.DEFAULT_ENCODING, ObservationCollectionBinding.class);
		}
		return omParser;
	}

	public static void print(ObservationCollection oc) throws XmlException {
		StringBuffer sb = new StringBuffer();
		StringBufferOutputStream out = new StringBufferOutputStream(sb);
		getOMGenerator()
				.writeToStream(new ObservationCollectionBinding(oc), out);
		System.out.println(XmlObject.Factory.parse(sb.toString())
				.xmlText(Namespace.defaultOptions()));
	}

	// public static void print(ObservationCollection oc, String filename)
	// throws FileNotFoundException {
	// getOMGenerator().writeToStream(new ObservationCollectionBinding(oc), new
	// FileOutputStream(filename));
	// }

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

	private XmlObject ocOutput = null;
	private XmlObject refOutput = null;

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
		String sgN = sg.getSimpleName(), tgN = tg.getSimpleName();
		selectAlgorithm(sgN + ":" + tgN);
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
			setSosRequest(GetObservationDocument.Factory
					.parse(testNull(request)));
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}

	public void setSosRequest(GetObservationDocument request) {
		this.sosRequest = testNull(request);
	}

	public void setSosRequest(String offering, String obsProp, DateTime begin,
			DateTime end) {
		GetObservationDocument request = GetObservationDocument.Factory
				.newInstance();
		GetObservation getObs = request.addNewGetObservation();
		getObs.setOffering(offering);
		getObs.setService(Constants.Sos.SERVICE_NAME);
		getObs.setVersion(Constants.Sos.SERVICE_VERSION);
		getObs.setResultModel(Constants.Sos.MEASUREMENT_RESULT_MODEL);
		getObs.setResponseFormat(Constants.Sos.OBSERVATION_OUTPUT_FORMAT);
		getObs.setResponseMode(ResponseModeType.INLINE);
		getObs.addNewObservedProperty().setStringValue(obsProp);
		BinaryTemporalOpType btot = BinaryTemporalOpType.Factory.newInstance();
		btot.addNewPropertyName();
		XmlCursor cursor = btot.newCursor();
		cursor.toChild(new QName("http://www.opengis.net/ogc", "PropertyName"));
		cursor.setTextValue("om:SamplingTime");
		cursor.dispose();
		TimePeriodType xb_timePeriod = TimePeriodType.Factory.newInstance();
		xb_timePeriod.addNewBeginPosition()
				.setStringValue(TimeUtils.format(begin));
		xb_timePeriod.addNewEndPosition().setStringValue(TimeUtils.format(end));
		btot.setTimeObject(xb_timePeriod);
		EventTime eventTime = getObs.addNewEventTime();
		eventTime.setTemporalOps(btot);
		cursor = eventTime.newCursor();
		cursor.toChild(new QName("http://www.opengis.net/ogc", "temporalOps"));
		cursor.setName(new QName("http://www.opengis.net/ogc", "TM_Equals"));
		cursor.toChild(new QName("http://www.opengis.net/gml", "_TimeObject"));
		cursor.setName(new QName("http://www.opengis.net/gml", "TimePeriod"));
		cursor.dispose();
		this.setSosRequest(request);
	}

	public void setSosDestinationUrl(String url) {
		try {
			this.sosDestUrl = new URL(testNull(url));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setSpatialAggregationMethod(
			Class<? extends AggregationMethod> sam) {
		this.spatialAM = testNull(sam);
	}

	@SuppressWarnings("unchecked")
	public void setSpatialAggregationMethod(String sam) {
		try {
			setSpatialAggregationMethod((Class<? extends AggregationMethod>) Class
					.forName(testNull(sam)));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void setTemporalAggregationMethod(
			Class<? extends AggregationMethod> tam) {
		this.temporalAM = testNull(tam);
	}

	public void setObservationCollection(ObservationCollection oc) {
		this.oc = oc;
	}

	@SuppressWarnings("unchecked")
	public void setTemporalAggregationMethod(String tam) {
		try {
			setTemporalAggregationMethod((Class<? extends AggregationMethod>) Class
					.forName(testNull(tam)));
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

	public ProcessTester execute() {
		return execute(null);
	}

	public ProcessTester execute(String wpsUrl) {
		ProcessDescriptionType desc = getSelectedAlgorithm().getDescription();
		ExecuteRequestBuilder eb = new ExecuteRequestBuilder(desc);
		if (p != null) {
			eb.addLiteralData(Constants.Process.Inputs.TIME_RANGE_ID, TimeUtils
					.format(p));
		}
		if (wfsUrl != null) {
			eb.addLiteralData(Constants.Process.Inputs.WFS_URL_ID, wfsUrl
					.toExternalForm());
		}
		if (sosDestUrl != null) {
			eb.addLiteralData(Constants.Process.Inputs.SOS_DESTINATION_URL_ID, sosDestUrl
					.toExternalForm());
		}
		if (groupByObservedProperty != null) {
			eb.addLiteralData(Constants.Process.Inputs.GROUP_BY_OBSERVED_PROPERTY_ID, groupByObservedProperty
					.toString());
		}
		if (temporalBeforeSpatial != null) {
			eb.addLiteralData(Constants.Process.Inputs.SPATIAL_BEFORE_TEMPORAL, temporalBeforeSpatial
					.toString());
		}
		if (wfsRequest != null) {
			eb.addComplexData(Constants.Process.Inputs.WFS_REQUEST_ID, new GetFeatureRequestBinding(
					wfsRequest), Namespace.WFS.SCHEMA, IOHandler.DEFAULT_ENCODING, IOHandler.DEFAULT_MIMETYPE);
		}
		if (sosSrcUrl != null) {
			eb.addLiteralData(Constants.Process.Inputs.SOS_SOURCE_URL_ID, sosSrcUrl
					.toExternalForm());
		}
		if (sosRequest != null) {
			eb.addComplexData(Constants.Process.Inputs.SOS_REQUEST_ID, new GetObservationRequestBinding(
					sosRequest), Namespace.SOS.SCHEMA, IOHandler.DEFAULT_ENCODING, IOHandler.DEFAULT_MIMETYPE);
		}
		if (fc != null) {
			eb.addComplexData(Constants.Process.Inputs.FEATURE_COLLECTION_ID, new GTVectorDataBinding(
					fc), Namespace.GML.SCHEMA, IOHandler.DEFAULT_ENCODING, IOHandler.DEFAULT_MIMETYPE);
		}

		ExecuteDocument exec = eb.getExecute();

		DocumentOutputDefinitionType dodt = exec.getExecute()
				.addNewResponseForm().addNewResponseDocument().addNewOutput();
		dodt.addNewIdentifier()
				.setStringValue(Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_ID);

		if (sosDestUrl != null) {
			dodt = exec.getExecute().getResponseForm().getResponseDocument()
					.addNewOutput();
			dodt.addNewIdentifier()
					.setStringValue(Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_REFERENCE_ID);

			dodt = exec.getExecute().getResponseForm().getResponseDocument()
					.addNewOutput();
			dodt.addNewIdentifier()
					.setStringValue(Constants.Process.Outputs.VISUALIZATION_LINK_ID);
		}

		// log.info("Sending Execute request:\n{}",exec.xmlText(Namespace.defaultOptions()));

		try {
			XmlObject res = null;
			if (wpsUrl == null) {
				// execute locally
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				new RequestHandler(exec.newInputStream(), os).handle();
				res = XmlObject.Factory.parse(os.toString());
			} else {
				// execute remote
				res = XmlObject.Factory.parse(Utils
						.sendPostRequest(wpsUrl, exec.xmlText()));
			}

			exec.save(new File("src/test/request.xml"), Namespace.defaultOptions());
			res.save(new File("src/test/response.xml"), Namespace.defaultOptions());

			if (res instanceof ExecuteResponseDocument) {
				// log.info("Got response.\n{}",res.xmlText(Namespace.defaultOptions()));
				ExecuteResponseDocument resp = (ExecuteResponseDocument) res;

				for (OutputDataType odt : resp.getExecuteResponse()
						.getProcessOutputs().getOutputArray()) {
					log.info("Got '{}'-Output.", odt.getIdentifier()
							.getStringValue());
					if (odt.getIdentifier()
							.getStringValue()
							.equals(Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_ID)) {
						ocOutput = odt.getData().getComplexData();
					} else if (odt
							.getIdentifier()
							.getStringValue()
							.equals(Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_REFERENCE_ID)) {
						refOutput = odt.getData().getComplexData();
					} else if (odt
							.getIdentifier()
							.getStringValue()
							.equals(Constants.Process.Outputs.VISUALIZATION_LINK_ID)) {
						log.info("VisualizationLink: {}", odt.getData()
								.getLiteralData().getStringValue());
					}

				}
			} else if (res instanceof ExceptionReport) {
				throw new RuntimeException(res.xmlText(Namespace
						.defaultOptions()));
			}
		} catch (ExceptionReport e) {
			throw new RuntimeException(e);
		} catch (XmlException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public GetObservationDocument getReferenceOutput() {
		if (refOutput == null)
			throw new RuntimeException("Not yet executed.");
		return (GetObservationDocument) new GetObservationRequestParser()
				.parseXML(refOutput.newInputStream()).getPayload();
	}

	public ObservationCollection getOutput() {
		if (ocOutput == null)
			throw new RuntimeException("Not yet executed.");
		return (ObservationCollection) getOMParser()
				.parseXML(ocOutput.newInputStream()).getPayload();
	}

	private <T> T testNull(T t) {
		if (t == null)
			throw new NullPointerException();
		ocOutput = null;
		refOutput = null;
		return t;
	}

}
