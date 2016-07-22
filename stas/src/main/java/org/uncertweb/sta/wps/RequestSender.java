package org.uncertweb.sta.wps;

import static org.uncertweb.utils.UwCollectionUtils.list;
import static org.uncertweb.utils.UwCollectionUtils.map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.RequestHandler;
import org.uncertweb.utils.UwXmlUtils;

public class RequestSender {


	static {
		try {
			WPSConfig.forceInitialization(RequestSender.class.getResourceAsStream("/wps_config/wps_config.xml"));
			ParserFactory.initialize(WPSConfig.getInstance().getActiveRegisteredParser());
			GeneratorFactory.initialize(WPSConfig.getInstance().getActiveRegisteredGenerator());
		} catch (XmlException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String sendPostRequest(String request) {
		if (request == null) {
			throw new NullPointerException();
		}
		return sendPostRequest(IOUtils.toInputStream(request));
	}

	public static String sendPostRequest(InputStream request) {
		if (request == null) {
			throw new NullPointerException();
		}
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			RequestHandler handler = new RequestHandler(request, os);
			handler.handle();
			return XmlObject.Factory.parse(os.toString()).xmlText(UwXmlUtils.defaultOptions());
		} catch (ExceptionReport e) {
			return e.getExceptionDocument().xmlText(UwXmlUtils.defaultOptions());
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}

	public static String sendGetRequest(Map<String, List<String>> parameters) {
		if (parameters == null) {
			parameters = map();
		}
		try {
			Map<String, String[]> params = map();
			for (Entry<String, List<String>> e : parameters.entrySet()) {
				params.put(e.getKey(), e.getValue().toArray(new String[e.getValue().size()]));
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			RequestHandler handler = new RequestHandler(params, os);
			handler.handle();
			return XmlObject.Factory.parse(os.toString()).xmlText(UwXmlUtils.defaultOptions());
		} catch (ExceptionReport e) {
			return e.getExceptionDocument().xmlText(UwXmlUtils.defaultOptions());
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isExceptionReport(String s) {
		try {
			XmlObject xml = XmlObject.Factory.parse(s);
			return xml instanceof net.opengis.ows.ExceptionReportDocument
					|| xml instanceof net.opengis.ows.x11.ExceptionReportDocument;
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}

	public static String describeProcess(String identifier) {
		Map<String,List<String>> parameter = map();
		parameter.put("version", list("1.0.0"));
		parameter.put("service", list("WPS"));
		parameter.put("request", list("DescribeProcess"));
		parameter.put("identifier", list(identifier));
		return sendGetRequest(parameter);
	}

	public static void main(String[] args) {
		InputStream is = RequestSender.class.getResourceAsStream("request-oldOM-ref-fc.xml");

		String response = sendPostRequest(is);

		if (isExceptionReport(response)) {
			throw new RuntimeException("\n"+response);
		}

//		describeProcess("urn:ogc:def:aggregationProcess:polygonContainment:spatialSum:noPartitioning:temporalMax");


	}
}
