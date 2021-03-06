package org.n52.wps.server;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.ResponseFormType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.request.ExecuteRequest;

/**
 * This class has to be extended in order to be served through the WPS.
 * The class file should also include a description file of the algorithm. This file has to be
 * valid against the describeProcess.xsd. The file has to be placed in the folder of the class file and has
 * to be named the same as the Algorithm.
 *
 * <p>If you want to apply a different initialization method, just override the initializeDescription() method.
 *
 * NOTE: This class is an adapter and it is recommended to extend this.
 * @author foerster
 *
 */
public abstract class AbstractAlgorithm implements IAlgorithm
{
	protected ProcessDescriptionType description;
	private final String wkName;
	private static Logger LOGGER = Logger.getLogger(AbstractAlgorithm.class);
	protected ExecuteRequest executeRequest;

	/**
	 * default constructor, calls the initializeDescription() Method
	 */
	public AbstractAlgorithm() {
		this.description = initializeDescription();
		this.wkName = this.getClass().getName();
	}

	/**
	 * default constructor, calls the initializeDescription() Method
	 */
	public AbstractAlgorithm(String wellKnownName) {
		this.wkName = wellKnownName; // Has to be initialized before the description.
		this.description = initializeDescription();
	}

	/**
	 * This method should be overwritten, in case you want to have a way of initializing.
	 *
	 * In detail it looks for a xml descfile, which is located in the same directory as the implementing class and has the same
	 * name as the class, but with the extension XML.
	 * @return
	 */
	protected ProcessDescriptionType initializeDescription() {
		String className = this.getClass().getName().replace(".", "/");
		InputStream xmlDesc = this.getClass().getResourceAsStream("/" + className + ".xml");
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);
			if(doc.getProcessDescriptions().getProcessDescriptionArray().length == 0) {
				LOGGER.warn("ProcessDescription does not contain correct any description");
				return null;
			}

			// Checking that the process name (full class name or well-known name) matches the identifier.
			if(!doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().getStringValue().equals(this.getClass().getName()) &&
					!doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().getStringValue().equals(this.getWellKnownName())) {
				doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().setStringValue(this.getClass().getName());
				LOGGER.warn("Identifier was not correct, was changed now temporary for server use to " + this.getClass().getName() + ". Please change it later in the description!");
			}

			return doc.getProcessDescriptions().getProcessDescriptionArray(0);
		}
		catch(IOException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " + this.getClass().getName(), e);
		}
		catch(XmlException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " + this.getClass().getName(), e);
		}
		return null;
	}

	public ProcessDescriptionType getDescription()  {
		return description;
	}

	public boolean processDescriptionIsValid() {
		return description.validate();
	}

	public String getWellKnownName() {
		return this.wkName;
	}

	public void setExecuteRequest(ExecuteRequest executeRequest){
		this.executeRequest = executeRequest;
	}

	public List<String> getOutputIdentifiers(){
		List<String> outputIdentifiers = new ArrayList<String>();
		ResponseFormType xb_responseForm = this.executeRequest.getExecute().getResponseForm();
		OutputDefinitionType xb_rawDataOutput = xb_responseForm.getRawDataOutput();
		if (xb_rawDataOutput!=null){
			outputIdentifiers.add(xb_rawDataOutput.getIdentifier().getStringValue());
		}
		else {
			DocumentOutputDefinitionType[] xb_outputArray = xb_responseForm.getResponseDocument().getOutputArray();
			for (DocumentOutputDefinitionType xb_output:xb_outputArray){
				outputIdentifiers.add(xb_output.getIdentifier().getStringValue());
			}
		}
		return outputIdentifiers;
	}
}
