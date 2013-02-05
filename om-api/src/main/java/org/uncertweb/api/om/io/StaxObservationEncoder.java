package org.uncertweb.api.om.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.opengis.om.x20.OMObservationDocument;

import org.uncertweb.api.om.OMConstants;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.AbstractHookedObservationEncoder.EncoderHook;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.CategoryObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ObservationCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;


/**
 * Hack for stax based encoding to support encoding of large datafiles; 
 * currently a file size of about 80 MBs is supported; might be re-written to a full STAX
 * implementation in case there is a need for large XML documents.
 * 
 * Currently, the single observations are still parsed using XMLbeans, only the Collection root elements
 * are encoded using STAX. The large Streams still cause OutofMemory Heap Errors for very large (>100MB) datafiles.
 * 
 * @author staschc
 *
 */
public class StaxObservationEncoder implements IObservationEncoder {

	
	/**
	 * XMLBeans encoder is used to 
	 */
	private XBObservationEncoder xbEncoder;
	
	/**
	 * constructor; initializes XmlBeans encoder
	 * 
	 */
	public StaxObservationEncoder() { 
		this(null);
	}

	/**
	 * constructor; initializes XmlBeans encoder
	 * 
	 */
	public StaxObservationEncoder(Collection<EncoderHook<OMObservationDocument>> hooks) { 
		this.xbEncoder = new XBObservationEncoder(hooks);
	}


	
	@Override
	public String encodeObservationCollection(IObservationCollection obsCol)
			throws OMEncodingException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		encodeObservationCollection(obsCol,bout);
		String result = bout.toString();
		try {
			bout.close();
		} catch (IOException e) {
			throw new RuntimeException("Error while closing result stream: " + e.getLocalizedMessage());
		}
		return result;
	}

	@Override
	public void encodeObservationCollection(IObservationCollection obsCol,
			File f) throws OMEncodingException {
		try {
			FileOutputStream fos = new FileOutputStream(f);
			encodeObservationCollection(obsCol,fos);
			fos.close();
		} catch (FileNotFoundException e) {
			throw new OMEncodingException(e);
		} catch (IOException e) {
			throw new OMEncodingException(e);
		}
	}

	@Override
	public void encodeObservationCollection(IObservationCollection obsCol,
			OutputStream out) throws OMEncodingException {
		this.xbEncoder.setIsCol(true);
		try {
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = factory.createXMLStreamWriter(out);
			
			
			//set character escaping on false!
			((com.sun.xml.internal.stream.writers.XMLStreamWriterImpl)writer).setEscapeCharacters(false);
			writer.writeStartDocument();
			
			if (obsCol instanceof UncertaintyObservationCollection){
				writer.writeStartElement(UncertaintyObservationCollection.NAME);
			} else if (obsCol instanceof MeasurementCollection){
				writer.writeStartElement(MeasurementCollection.NAME);
			}else if (obsCol instanceof BooleanObservationCollection){
				writer.writeStartElement(BooleanObservationCollection.NAME);
			}else if (obsCol instanceof TextObservationCollection){
				writer.writeStartElement(TextObservationCollection.NAME);
			}else if (obsCol instanceof CategoryObservationCollection){
				writer.writeStartElement(CategoryObservationCollection.NAME);
			}else if (obsCol instanceof DiscreteNumericObservationCollection){
				writer.writeStartElement(DiscreteNumericObservationCollection.NAME);
			}else if (obsCol instanceof ReferenceObservationCollection){
				writer.writeStartElement(ReferenceObservationCollection.NAME);
			}
			else if (obsCol instanceof ObservationCollection){
				writer.writeStartElement(ObservationCollection.NAME);
			}
			else {
				throw new RuntimeException("No matching observation collection type found. Observation Collection type is " + obsCol.getClass().getCanonicalName());
			}
			writer.writeDefaultNamespace(OMConstants.NS_OM);
			writer.writeNamespace(OMConstants.NS_OM_PREFIX, OMConstants.NS_OM);
			writer.writeNamespace(OMConstants.NS_GML_PREFIX, OMConstants.NS_GML);
			writer.writeNamespace(OMConstants.NS_SA_PREFIX, OMConstants.NS_SA);
			writer.writeNamespace(OMConstants.NS_SF_PREFIX, OMConstants.NS_SA);
			writer.writeNamespace(OMConstants.NS_SAMS_PREFIX, OMConstants.NS_SAMS);
			writer.writeNamespace(OMConstants.NS_XLINK_PREFIX, OMConstants.NS_XLINK);
			writer.writeNamespace(OMConstants.NS_GMD_PREFIX, OMConstants.NS_GMD);
			writer.writeNamespace(OMConstants.NS_XSI_PREFIX, OMConstants.NS_XSI);
			writer.writeAttribute(OMConstants.NS_XSI_PREFIX,OMConstants.NS_XSI,"schemaLocation",OMConstants.NS_OM+" "+OMConstants.OM_SCHEMA_LOCATION);
			
			Iterator<? extends AbstractObservation> iter = obsCol.getObservations().iterator();
			while (iter.hasNext()){
				writer.writeCharacters(prepareObservationString(encodeObservation(iter.next())));
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			writer.close();

		} catch (XMLStreamException e) {
			throw new OMEncodingException(e);
		}
		finally {
			this.xbEncoder.setIsCol(false);
		}
	}

	@Override
	public void encodeObservationCollection(IObservationCollection obsCol,
			Writer writerx) throws OMEncodingException {
		this.xbEncoder.setIsCol(true);
		try {
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = factory.createXMLStreamWriter(writerx);
			
			
			//set character escaping on false!
			((com.sun.xml.internal.stream.writers.XMLStreamWriterImpl)writer).setEscapeCharacters(false);
			writer.writeStartDocument();
			
			if (obsCol instanceof UncertaintyObservationCollection){
				writer.writeStartElement(UncertaintyObservationCollection.NAME);
			} else if (obsCol instanceof MeasurementCollection){
				writer.writeStartElement(MeasurementCollection.NAME);
			}else if (obsCol instanceof BooleanObservationCollection){
				writer.writeStartElement(BooleanObservationCollection.NAME);
			}else if (obsCol instanceof TextObservationCollection){
				writer.writeStartElement(TextObservationCollection.NAME);
			}else if (obsCol instanceof CategoryObservationCollection){
				writer.writeStartElement(CategoryObservationCollection.NAME);
			}else if (obsCol instanceof DiscreteNumericObservationCollection){
				writer.writeStartElement(DiscreteNumericObservationCollection.NAME);
			}else if (obsCol instanceof ReferenceObservationCollection){
				writer.writeStartElement(ReferenceObservationCollection.NAME);
			}
			else if (obsCol instanceof ObservationCollection){
				writer.writeStartElement(ObservationCollection.NAME);
			}
			
			writer.writeDefaultNamespace(OMConstants.NS_OM);
			writer.writeNamespace(OMConstants.NS_OM_PREFIX, OMConstants.NS_OM);
			writer.writeNamespace(OMConstants.NS_GML_PREFIX, OMConstants.NS_GML);
			writer.writeNamespace(OMConstants.NS_SA_PREFIX, OMConstants.NS_SA);
			writer.writeNamespace(OMConstants.NS_SF_PREFIX, OMConstants.NS_SA);
			writer.writeNamespace(OMConstants.NS_SAMS_PREFIX, OMConstants.NS_SAMS);
			writer.writeNamespace(OMConstants.NS_XLINK_PREFIX, OMConstants.NS_XLINK);
			writer.writeNamespace(OMConstants.NS_GMD_PREFIX, OMConstants.NS_GMD);
			writer.writeNamespace(OMConstants.NS_XSI_PREFIX, OMConstants.NS_XSI);
			writer.writeAttribute(OMConstants.NS_XSI_PREFIX,OMConstants.NS_XSI,"schemaLocation",OMConstants.NS_OM+" "+OMConstants.OM_SCHEMA_LOCATION);
			Iterator<? extends AbstractObservation> iter = obsCol.getObservations().iterator();
			while (iter.hasNext()){
				writer.writeCharacters(prepareObservationString(encodeObservation(iter.next())));
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			writer.close();

		} catch (XMLStreamException e) {
			throw new OMEncodingException(e);
		}
		finally {
			this.xbEncoder.setIsCol(false);
		}
		
	}

	@Override
	public String encodeObservation(AbstractObservation obs)
			throws OMEncodingException {
		
		return this.xbEncoder.encodeObservation(obs);
	}

	@Override
	public void encodeObservation(AbstractObservation obs, File f)
			throws OMEncodingException {
		this.xbEncoder.encodeObservation(obs,f);
	}

	@Override
	public void encodeObservation(AbstractObservation obs, OutputStream out)
			throws OMEncodingException {
		this.xbEncoder.encodeObservation(obs,out);
	}

	@Override
	public void encodeObservation(AbstractObservation obs, Writer writer)
			throws OMEncodingException {
		this.xbEncoder.encodeObservation(obs,writer);
	}
	
	/**
	 * 
	 * 
	 * @param obsString
	 * @return
	 */
	private String prepareObservationString(String obsString){
		String result = obsString.replace("xsi:schemaLocation=\""+OMConstants.NS_OM+" " +OMConstants.OM_SCHEMA_LOCATION+ "\"", "");
		result = result.replace(createXMLNSAttribute(OMConstants.NS_GML_PREFIX, OMConstants.NS_GML), "");
		result = result.replace(createXMLNSAttribute(OMConstants.NS_SA_PREFIX, OMConstants.NS_SA), "");
		result = result.replace(createXMLNSAttribute(OMConstants.NS_SAMS_PREFIX, OMConstants.NS_SAMS), "");
		result = result.replace(createXMLNSAttribute(OMConstants.NS_XLINK_PREFIX, OMConstants.NS_XLINK), "");
		result = result.replace(createXMLNSAttribute(OMConstants.NS_GMD_PREFIX, OMConstants.NS_GMD), "");
		result = result.replace(createXMLNSAttribute(OMConstants.NS_XSI_PREFIX, OMConstants.NS_XSI), "");
		return result;
	}
	
	private String createXMLNSAttribute(String prefix, String nsUrl){
		return "xmlns:"+prefix+"=\""+nsUrl+"\"";
	}

}
