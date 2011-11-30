package org.uncertml.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.randomvariable.IGaussianCovarianceParameter;
import org.uncertml.distribution.randomvariable.NormalSpatialField;
import org.uncertml.distribution.randomvariable.VariogramFunction;
import org.uncertml.distribution.randomvariable.VariogramFunction.Model;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.statistics.STCovarianceMatrix;
import org.uncertml.x20.NormalSpatialFieldDocument;
import org.uncertml.x20.NormalSpatialFieldType;
import org.uncertml.x20.VariogramFunctionDocument;
import org.uncertml.x20.VariogramFunctionType;
import org.uncertml.x20.CovarianceMatrixDocument.CovarianceMatrix;
import org.uncertml.x20.VariogramFunctionType.Anisotropy;

/**
 * Extended UncertML XML parser that supports generation of usual UncertML plus Random Variables
 * 
 * @author staschc
 *
 */
public class ExtendedXMLParser implements IUncertaintyParser {
	
	private XMLParser uncertMLparser;

	@Override
	public IUncertainty parse(String uncertml)
			throws UncertaintyParserException {
		try {
			XmlObject xb_object = XmlObject.Factory.parse(uncertml);
			return parseXmlObject(xb_object);
		} catch (XmlException e) {
			String message = "Error while parsing uncertml: "+e.getLocalizedMessage();
			throw new RuntimeException(message);
		}
	}
	
	

	@Override
	public IUncertainty parse(InputStream stream)
			throws UncertaintyParserException {
		try {
			XmlObject xb_object = XmlObject.Factory.parse(stream);
			return parseXmlObject(xb_object);
		} catch (XmlException e) {
			String message = "Error while parsing uncertml: "+e.getLocalizedMessage();
			throw new RuntimeException(message);
		} catch (IOException e) {
			String message = "Error while parsing uncertml: "+e.getLocalizedMessage();
			throw new RuntimeException(message);
		}
	}

	@Override
	public IUncertainty parse(File file) throws UncertaintyParserException {
		try {
			XmlObject xb_object = XmlObject.Factory.parse(file);
			return parseXmlObject(xb_object);
		} catch (XmlException e) {
			String message = "Error while parsing uncertml: "+e.getLocalizedMessage();
			throw new RuntimeException(message);
		} catch (IOException e) {
			String message = "Error while parsing uncertml: "+e.getLocalizedMessage();
			throw new RuntimeException(message);
		}
	}

	@Override
	public IUncertainty parse(Reader reader) throws UncertaintyParserException {
		try {
			XmlObject xb_object = XmlObject.Factory.parse(reader);
			return parseXmlObject(xb_object);
		} catch (XmlException e) {
			String message = "Error while parsing uncertml: "+e.getLocalizedMessage();
			throw new RuntimeException(message);
		} catch (IOException e) {
			String message = "Error while parsing uncertml: "+e.getLocalizedMessage();
			throw new RuntimeException(message);
		}
	}
	
	private IUncertainty parseXmlObject(XmlObject xb_object) throws UncertaintyParserException {
		if (xb_object instanceof VariogramFunctionDocument){
			return parseVariogramFunction((VariogramFunctionDocument)xb_object);
		}
		//TODO implement further methods for the different field types
		else{
			return uncertMLparser.parse(xb_object.toString());
		}
	}
	
	private IUncertainty parseNormalSpatialField(NormalSpatialFieldDocument xbObject) throws UncertaintyParserException {
		NormalSpatialFieldType xb_nsftype = ((NormalSpatialFieldDocument)xbObject).getNormalSpatialField();
		IGaussianCovarianceParameter gp = null;
		
		//covarianceMatrix parsing
		CovarianceMatrix covMatrix = xb_nsftype.getCovarianceMatrix();
		if (covMatrix!=null){
			IUncertainty cvm = this.uncertMLparser.parse(covMatrix.toString());
			if (cvm instanceof org.uncertml.statistic.CovarianceMatrix){
				gp = new STCovarianceMatrix((org.uncertml.statistic.CovarianceMatrix)cvm);
			}
		}
		
		//covariance matrix is not set, so parse variogramFunction parameter
		else {
			VariogramFunctionType xb_vf = xb_nsftype.getVariogramFunction();
			VariogramFunctionDocument xb_vfd = VariogramFunctionDocument.Factory.newInstance();
			xb_vfd.setVariogramFunction(xb_vf);
			gp = (VariogramFunction)parseVariogramFunction(xb_vfd);
		}
		
		//parse samples reference
		URL sampleReference = null;
		try {
			sampleReference =  new URL(xb_nsftype.getSamples().getHref());
		} catch (MalformedURLException e) {
			throw new UncertaintyParserException("Error while parsing sample Reference of NormalSpatialField!");
		}
		
		return new NormalSpatialField(sampleReference,gp);
	}

	/**
	 * helper method for parsing a variogram
	 * 
	 * @param xbObject
	 * 			XMLBeans representation of variogram
	 * @return variogram
	 */
	private IUncertainty parseVariogramFunction(VariogramFunctionDocument xbObject) {
		VariogramFunction vario;
		VariogramFunctionType xb_vg = xbObject.getVariogramFunction();
		double sill,range,nugget;
		double kappa = Double.NaN;
		Model model;
		sill = xb_vg.getSill();
		range = xb_vg.getRange();
		nugget = xb_vg.getNugget();
		String modelString = xb_vg.getModel().toString().toUpperCase();
		model = Model.valueOf(modelString);
		if (model.equals(Model.MATERN)){
			kappa = xb_vg.getKappa();
		}
		Anisotropy xbAnis = xb_vg.getAnisotropy();
		org.uncertml.statistics.Anisotropy anis = null;
		if (xbAnis!=null){
			double pDirection = xbAnis.getPrincipalDirection();
			double ratio = xbAnis.getRatio();
			anis = new 	org.uncertml.statistics.Anisotropy(pDirection,ratio);
		}
		vario = new VariogramFunction(sill,range,nugget,kappa,model,anis);
		return vario;
	}

}
