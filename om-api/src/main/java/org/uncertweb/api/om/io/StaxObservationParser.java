package org.uncertweb.api.om.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.opengis.om.x20.FoiPropertyType;

import org.apache.xmlbeans.XmlException;
import org.uncertweb.api.om.OMConstants;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.ObservationCollection;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

public class StaxObservationParser implements IObservationParser{
	/**
	 * XMLBeans encoder is used to 
	 */
	private XBObservationParser xbParser;
	
	
	
	public StaxObservationParser(){
		this.xbParser = new XBObservationParser();
	}
	

	@Override
	public IObservationCollection parse(String xmlString)
			throws OMParsingException {
		if (xmlString.contains("Collection")){
			InputStream in = new ByteArrayInputStream(xmlString.getBytes());
			return parseObservationCollection(in);
		}
		else {
			IObservationCollection obsCol = new ObservationCollection();
			obsCol.addObservation(xbParser.parseObservation(xmlString));
			return obsCol;
		}
	}

	@Override
	public IObservationCollection parseObservationCollection(String xmlObsCol)
			throws OMParsingException {
		InputStream in = new ByteArrayInputStream(xmlObsCol.getBytes());
		return parseObservationCollection(in);
	}

	@Override
	public AbstractObservation parseObservation(String xmlObs)
			throws OMParsingException {
		//delegate single observation to XmlBeans parser
		return xbParser.parseObservation(xmlObs);
	}
	
	public IObservationCollection parseObservationCollection(InputStream in) throws OMParsingException{
		IObservationCollection obsCol = new ObservationCollection();
		xbParser.setIsCollection(true);
		List<String> observationNames = OMConstants.getObservationNames();
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			XMLEventReader parser = factory.createXMLEventReader(in);
			StringBuilder obsString = null;
			while( parser.hasNext() ) {
			    
			    XMLEvent event = parser.nextEvent();
			    int eventType = event.getEventType();
			    switch (eventType) {
			        case XMLStreamConstants.END_DOCUMENT:
			            parser.close();
			        break;
			        case XMLStreamConstants.START_ELEMENT:
			            StartElement element = event.asStartElement();
			            String name = element.getName().getLocalPart();
			            if (observationNames.contains(name)){
			            	if (obsString!=null){
			            		obsCol.addObservation(xbParser.parseObservation(obsString.toString()));
							}
			            	obsString= new StringBuilder();
			            	String fullStartElement = appendNamespaces(element.toString());
			            	obsString.append(fullStartElement);
			            }
			            else if (!name.contains("Collection")){
			            	obsString.append(removeNamespaces(element.toString()));
			            }
			        break;
			        case XMLStreamConstants.CHARACTERS:
			            Characters characters = event.asCharacters();
			            if( !characters.isWhiteSpace() )
			            	obsString.append(characters.getData());
			                break;
			        case XMLStreamConstants.END_ELEMENT:
			        	obsString.append(removeNamespaces(event.asEndElement().toString()));
			            break;
			    }
			}
			
		} catch (XMLStreamException e) {
			throw new OMParsingException("Error while reading observation input with STAX parser: "+e.getLocalizedMessage());
		}
		finally{
			xbParser.setIsCollection(false);
		}
		
		return obsCol;
	}
	
	public synchronized SpatialSamplingFeature parseSamplingFeature(
			FoiPropertyType xb_featureOfInterest) throws IllegalArgumentException, MalformedURLException, URISyntaxException, XmlException {
	
		return xbParser.parseSamplingFeature(xb_featureOfInterest);
	}
	
	private String appendNamespaces(String eString) {
		String plainElement = removeNamespaces(eString);
		String fullElement = plainElement.replace(">", "");
		if (!plainElement.contains(OMConstants.NS_GML)){
			fullElement +="  xmlns:"+OMConstants.NS_GML_PREFIX+"=\""+OMConstants.NS_GML+"\"";
		}
		if (!plainElement.contains(OMConstants.NS_GMD)){
			fullElement +="  xmlns:"+OMConstants.NS_GMD_PREFIX+"=\""+OMConstants.NS_GMD+"\"";
		}
		if (!plainElement.contains(OMConstants.NS_OM)){
			fullElement +="  xmlns:"+OMConstants.NS_OM_PREFIX+"=\""+OMConstants.NS_OM+"\"";
		}
		if (!plainElement.contains(OMConstants.NS_XSI)){
			fullElement +="  xmlns:"+OMConstants.NS_XSI_PREFIX+"=\""+OMConstants.NS_XSI+"\"";
		}
		if (!plainElement.contains(OMConstants.NS_SAMS)){
			fullElement +="  xmlns:"+OMConstants.NS_SAMS_PREFIX+"=\""+OMConstants.NS_SAMS+"\"";
		}
		if (!plainElement.contains(OMConstants.NS_SAM_PREFIX)){
			fullElement +="  xmlns:"+OMConstants.NS_SAM_PREFIX+"=\""+OMConstants.NS_SA+"\"";
		}
		if (!plainElement.contains(OMConstants.NS_SA_PREFIX)){
			fullElement +="  xmlns:"+OMConstants.NS_SA_PREFIX+"=\""+OMConstants.NS_SA+"\"";
		}
		if (!plainElement.contains(OMConstants.NS_SF_PREFIX)){
			fullElement +="  xmlns:"+OMConstants.NS_SF_PREFIX+"=\""+OMConstants.NS_SA+"\"";
		}
		if (!plainElement.contains(OMConstants.NS_XLINK)){
			fullElement +="  xmlns:"+OMConstants.NS_XLINK_PREFIX+"=\""+OMConstants.NS_XLINK+"\"";
		}
		if (!plainElement.contains(OMConstants.NS_UNCERTML)){
			fullElement +="  xmlns:"+OMConstants.NS_UNCERTML_PREFIX+"=\""+OMConstants.NS_UNCERTML+"\"";
		}
		fullElement+=">";
		return fullElement;
	}


	private String removeNamespaces(String eString){
		eString= eString.replace("['"+OMConstants.NS_GMD+"']:", "");
		eString= eString.replace("['"+OMConstants.NS_GML+"']:", "");
		eString= eString.replace("['"+OMConstants.NS_OM+"']:", "");
		eString= eString.replace("['"+OMConstants.NS_SA+"']:", "");
		eString= eString.replace("['"+OMConstants.NS_SAMS+"']:", "");
		eString= eString.replace("['"+OMConstants.NS_SFT+"']:", "");
		eString= eString.replace("['"+OMConstants.NS_XLINK+"']:", "");
		eString= eString.replace("['"+OMConstants.NS_XSI+"']:", "");
		eString= eString.replace("['"+OMConstants.NS_UNCERTML+"']:", "");
		return eString;
	}
	
}
