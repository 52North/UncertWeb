package org.uncertweb.aqms.austal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.opengis.feature.simple.SimpleFeature;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.aqms.overlay.TotalConcentration;
import org.uncertweb.aqms.util.Utils;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;



public class AustalModelRun {
	private String austalAddress = "";
	private String resPath = "";
	private String inputPath;
	private Geometry receptorPoints = null;

	private String uncertaintyPrefix = "u_";
	private String certaintyPrefix = "c_";

	private static Logger logger = Logger.getLogger(AustalModelRun.class);

	public AustalModelRun(String austalAddress, String resPath){
		this.austalAddress = austalAddress;
		this.resPath = resPath;
		inputPath = resPath + "\\inputs";
	}

	public void setReceptorPoints(FeatureCollection<?,?> fc){
		FeatureIterator<?> iterator = fc.features();
		while (iterator.hasNext()) {
			SimpleFeature feature = (SimpleFeature) iterator.next();
			if (feature.getDefaultGeometry() instanceof MultiLineString) {
				receptorPoints = (MultiLineString) feature.getDefaultGeometry();
			}
		}
	}

	public UncertaintyObservationCollection executeU_AustalWPSOM(DateTime startDate, DateTime endDate, int numberOfRealisations){
		UncertaintyObservationCollection result = null;

		// connect to Austal WPS
		WPSClientSession session = WPSClientSession.getInstance();
		try {
			session.connect(austalAddress);
		} catch (WPSClientException e1) {
			e1.printStackTrace();
		}

		// Make execute request
		ExecuteDocument execDoc = null;

		// Read execute request template
		try {
			execDoc = ExecuteDocument.Factory.parse(new File(
					resPath + "\\request_templates\\u_austal_example.xml"));
		} catch (XmlException e) {
				e.printStackTrace();
		} catch (IOException e) {
				e.printStackTrace();
		}

		// Get WPS data inputs and change them
		InputType[] types = execDoc.getExecute().getDataInputs()
				.getInputArray();
		DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();
		for (InputType inputType : types) {
			String id = inputType.getIdentifier().getStringValue();

			if (id.equals("NumberOfRealisations")) {
				Node wpsLiteralDataValueNode = inputType.getData().getLiteralData()
						.getDomNode().getChildNodes().item(0);
				String newNumbReal = ((Integer) numberOfRealisations)
						.toString();
				wpsLiteralDataValueNode.setNodeValue(newNumbReal);
			}else if(id.equals("start-date")){
				Node wpsLiteralDataValueNode = inputType.getData().getLiteralData()
						.getDomNode().getChildNodes().item(0);
				String newStartDate = startDate.toString(dateFormat);
				wpsLiteralDataValueNode.setNodeValue(newStartDate);
			}else if(id.equals("end-date")){
				Node wpsLiteralDataValueNode = inputType.getData().getLiteralData()
						.getDomNode().getChildNodes().item(0);
				String newEndDate = endDate.toString(dateFormat);
				wpsLiteralDataValueNode.setNodeValue(newEndDate);
			}
		}

		// Run WPS and get output (= Realisation object)
		ExecuteResponseDocument responseDoc = null;
		try {
			responseDoc = (ExecuteResponseDocument) session.execute(
				austalAddress, execDoc);

			OutputDataType oType = responseDoc.getExecuteResponse().getProcessOutputs().getOutputArray(0);
			// all output elements
			Node wpsComplexData = oType.getData().getComplexData().getDomNode();
			// the complex data node
			Node unRealisation = wpsComplexData.getChildNodes().item(0);
			// the realisation node
			IObservationCollection iobs = new XBObservationParser().parseObservationCollection(nodeToString(unRealisation));
			result = (UncertaintyObservationCollection) iobs;
			return result;

		} catch (WPSClientException e) {// Auto-generated catch block
				e.printStackTrace();
		} catch (OMParsingException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return result;
	}

	//TODO: implement NetCDF handling
	public void executeU_AustalWPSNetCDF(){

	}

	public UncertaintyObservationCollection executeUPSOM(String upsAddress, int numberOfRealisations,  AustalProperties austalProps){
			UncertaintyObservationCollection result = null;

			// connect to UPS
			WPSClientSession session = WPSClientSession.getInstance();
			try {
				session.connect(upsAddress);
			} catch (WPSClientException e1) {
				e1.printStackTrace();
			}

			// add inputs for request
			Map<String, Object> inputs = new HashMap<String, Object>();

			// UPS properties
			inputs.put("ServiceURL", austalAddress);
			inputs.put("NumberOfRealisations", numberOfRealisations);
			inputs.put("IdentifierSimulatedProcess", "org.uncertweb.austalwps.AUSTAL2000Process");
			inputs.put("UncertainProcessOutputs", "uncertweb:Realisations");

			// uncertain inputs
			inputs.put(uncertaintyPrefix+"wind-speed", "file:///"+inputPath+"\\windspeed.xml");
			inputs.put(uncertaintyPrefix+"wind-direction", "file:///"+inputPath+"\\winddirection.xml");
			inputs.put(uncertaintyPrefix+"street-emissions", "file:///"+inputPath+"\\streets.xml");

			//certain inputs
			inputs.put(certaintyPrefix+"dd", austalProps.getDD());
			inputs.put(certaintyPrefix+"nx", austalProps.getNX());
			inputs.put(certaintyPrefix+"ny", austalProps.getNY());
			inputs.put(certaintyPrefix+"z0", austalProps.getZ0());
			inputs.put(certaintyPrefix+"qs", austalProps.getQS());
			inputs.put(certaintyPrefix+"stability-class", "file:///"+inputPath+"\\stabilityclass.xml");
			inputs.put(certaintyPrefix+"variable-emissions", "file:///"+inputPath+"\\variableemissions.xml");
			inputs.put(certaintyPrefix+"static-emissions", "file:///"+inputPath+"\\staticemissions.xml");
			List<String> austalParams = austalProps.getOtherParameters();
			if(austalParams!=null){
				String[] parInputs = austalParams.toArray(new String[0]);
				inputs.put(certaintyPrefix+"model-parameters", parInputs);
			}

			// central point
			Point point = austalProps.getCentralPoint();
			String centralPointFile = writeGMLobject2File("central-point", point);
			inputs.put(certaintyPrefix+"central-point", "file:///" + centralPointFile);

			// receptor points
			String receptorPointsFile = writeGMLobject2File("receptor-points", receptorPoints);
			inputs.put(certaintyPrefix+"receptor-points", "file:///" + receptorPointsFile);

			// Make execute request
			ExecuteDocument execDoc = null;
			try {
				execDoc = Utils.createExecuteDocumentManually(upsAddress, "org.uncertweb.ups.UPSGenericAustalProcess", inputs, UncertWebDataConstants.MIME_TYPE_OMX_XML);
			} catch (Exception e) {
				logger.debug(e);
			}

			// Run WPS and get output (= Realisation object)
			ExecuteResponseDocument responseDoc = null;
			try {
				responseDoc = (ExecuteResponseDocument) session.execute(
					upsAddress, execDoc);

				OutputDataType oType = responseDoc.getExecuteResponse().getProcessOutputs().getOutputArray(0);
				// all output elements
				Node wpsComplexData = oType.getData().getComplexData().getDomNode();
				// the complex data node
				Node unRealisation = wpsComplexData.getChildNodes().item(0);
				// the realisation node
				IObservationCollection iobs = new XBObservationParser().parseObservationCollection(nodeToString(unRealisation));
				result = (UncertaintyObservationCollection) iobs;
				return result;
			} catch (WPSClientException e) {// Auto-generated catch block
					e.printStackTrace();
			} catch (OMParsingException e) {
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			return result;
	}

	private String writeGMLobject2File(String identifier, Geometry geometry){
		int srs = 31467;
		String coordinates = "";
		File fIn = new File(resPath+"\\central-point.xml");
		File f = new File(inputPath + identifier + ".xml");

		// extract details from geometry
		if (geometry instanceof MultiLineString) {
			MultiLineString lineString = (MultiLineString) geometry;
			if(lineString.getSRID()!=0)
				srs = lineString.getSRID();
			// loop through coordinates and add them to the string
			for (int i = 0; i < lineString.getCoordinates().length; i++) {
				Coordinate coord = lineString.getCoordinates()[i];
				coordinates = coordinates +coord.x + ","+coord.y+" ";
			}
		} else if (geometry instanceof Point) {
			Point point = (Point) geometry;
			Coordinate coord = point.getCoordinate();
			coordinates = coordinates +coord.x + ","+coord.y;
			if(point.getSRID()!=0)
				srs = point.getSRID();
		}

		// write details to file
		try {
			BufferedReader bread = new BufferedReader(new FileReader(fIn));
			String content = "";
			String line = "";

			// substitute values
			while ((line = bread.readLine()) != null) {
				if(line.contains("srsName")){
					String[] s = line.split("#");
					if(s.length==2){
						s[1] = "#"+srs+"\">";
					}
					line = s[0] + s[1];
				}else if(line.contains("%$coords$%")){
					line = line.replace("%$coords$%", "" + coordinates);
				}
				content = content.concat(line);
			}

			// write new file
			BufferedWriter b = new BufferedWriter(new FileWriter(fIn));
			b.write(content);
			b.flush();
			b.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return f.getAbsoluteFile().toString();
	}

	//TODO: Implement NetCDF UPS case
	public void executeUPSNetCDF(DateTime startDate, DateTime endDate, int numberOfRealisations){

	}

	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));

		return stringWriter.toString();
	}
}

