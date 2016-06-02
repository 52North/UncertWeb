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
import org.uncertml.distribution.randomvariable.CovarianceMatrixParameter;
import org.uncertml.distribution.randomvariable.INormalCovarianceParameter;
import org.uncertml.distribution.randomvariable.NormalSpatialField;
import org.uncertml.distribution.randomvariable.NormalSpatioTemporalField;
import org.uncertml.distribution.randomvariable.NormalTimeSeries;
import org.uncertml.distribution.randomvariable.SampleReference;
import org.uncertml.distribution.randomvariable.VariogramFunction;
import org.uncertml.distribution.randomvariable.VariogramFunction.Model;
import org.uncertml.distribution.randomvariable.util.RandomVariablesUtil;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.x20.CovarianceMatrixDocument.CovarianceMatrix;
import org.uncertml.x20.CovarianceParameterType;
import org.uncertml.x20.NormalSpatialFieldDocument;
import org.uncertml.x20.NormalSpatialFieldType;
import org.uncertml.x20.NormalSpatioTemporalFieldDocument;
import org.uncertml.x20.NormalSpatioTemporalFieldType;
import org.uncertml.x20.NormalTimeSeriesDocument;
import org.uncertml.x20.NormalTimeSeriesType;
import org.uncertml.x20.ReferenceType;
import org.uncertml.x20.VariogramFunctionDocument;
import org.uncertml.x20.VariogramFunctionType;
import org.uncertml.x20.VariogramFunctionType.Anisotropy;

/**
 * Extended UncertML XML parser that supports generation of usual UncertML plus
 * Random Variables
 *
 * @author staschc
 *
 */
public class ExtendedXMLParser implements IUncertaintyParser {

	/**
	 * common uncertml parser from UncertML API
	 *
	 */
	@Override
	public IUncertainty parse(String uncertml)
			throws UncertaintyParserException {
		try {
			XmlObject xb_object = XmlObject.Factory.parse(uncertml);
			return parseXmlObject(xb_object);
		} catch (XmlException e) {
			String message = "Error while parsing uncertml: "
					+ e.getLocalizedMessage();
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
			String message = "Error while parsing uncertml: "
					+ e.getLocalizedMessage();
			throw new RuntimeException(message);
		} catch (IOException e) {
			String message = "Error while parsing uncertml: "
					+ e.getLocalizedMessage();
			throw new RuntimeException(message);
		}
	}

	@Override
	public IUncertainty parse(File file) throws UncertaintyParserException {
		try {
			XmlObject xb_object = XmlObject.Factory.parse(file);
			return parseXmlObject(xb_object);
		} catch (XmlException e) {
			String message = "Error while parsing uncertml: "
					+ e.getLocalizedMessage();
			throw new RuntimeException(message);
		} catch (IOException e) {
			String message = "Error while parsing uncertml: "
					+ e.getLocalizedMessage();
			throw new RuntimeException(message);
		}
	}

	@Override
	public IUncertainty parse(Reader reader) throws UncertaintyParserException {
		try {
			XmlObject xb_object = XmlObject.Factory.parse(reader);
			return parseXmlObject(xb_object);
		} catch (XmlException e) {
			String message = "Error while parsing uncertml: "
					+ e.getLocalizedMessage();
			throw new RuntimeException(message);
		} catch (IOException e) {
			String message = "Error while parsing uncertml: "
					+ e.getLocalizedMessage();
			throw new RuntimeException(message);
		}
	}

	/**
	 * helper method for parsing and XML Object and returning the Uncertainty
	 * object; method first try to parse the extended elements and, if none is
	 * matched, passes the object to the usual XmlParser of the UncertML API
	 *
	 * @param xb_object
	 *            XMLBeans representation of uncertainty
	 * @return Java representation of uncertainty
	 * @throws UncertaintyParserException
	 *             if parsing fails
	 */
	private IUncertainty parseXmlObject(XmlObject xb_object)
			throws UncertaintyParserException {

		if (xb_object instanceof VariogramFunctionDocument) {
			return parseVariogramFunction((VariogramFunctionDocument) xb_object);
		} else if (xb_object instanceof NormalSpatialFieldDocument) {
			return parseNormalSpatialField((NormalSpatialFieldDocument) xb_object);
		} else if (xb_object instanceof NormalTimeSeriesDocument) {
			return parseNormalTimeSeries((NormalTimeSeriesDocument) xb_object);
		} else if (xb_object instanceof NormalSpatioTemporalFieldDocument) {
			return parseNormalSpatioTemporalField((NormalSpatioTemporalFieldDocument) xb_object);
		} else {
			String xmlString = xb_object.xmlText();
			XMLParser umlParser = new XMLParser();
			return umlParser.parse(xmlString);
		}
	}

	/**
	 * helper method for parsing a spatio-temporal field
	 *
	 * @param xbObject
	 * @return
	 * @throws UncertaintyParserException
	 */
	private IUncertainty parseNormalSpatioTemporalField(
			NormalSpatioTemporalFieldDocument xbObject)
			throws UncertaintyParserException {
		INormalCovarianceParameter gp = null;
		NormalSpatioTemporalFieldType xb_nsftype = xbObject
				.getNormalSpatioTemporalField();

		// covarianceMatrix parsing
		CovarianceMatrix covMatrix = xb_nsftype.getCovarianceParameter()
				.getCovarianceMatrix();
		if (covMatrix != null) {
			XMLParser umlParser = new XMLParser();
			IUncertainty cvm = umlParser.parse(covMatrix.toString());
			if (cvm instanceof org.uncertml.statistic.CovarianceMatrix) {
				gp = new CovarianceMatrixParameter(
						(org.uncertml.statistic.CovarianceMatrix) cvm);
			}
		}

		// covariance matrix is not set, so parse variogramFunction parameter
		else {
			VariogramFunctionType xb_vf = xb_nsftype.getCovarianceParameter()
					.getVariogramFunction();
			VariogramFunctionDocument xb_vfd = VariogramFunctionDocument.Factory
					.newInstance();
			xb_vfd.setVariogramFunction(xb_vf);
			gp = (VariogramFunction) parseVariogramFunction(xb_vfd);
		}

		// parse samples reference#
		ReferenceType xb_samples = xb_nsftype.getSamples();
		String spTrendString = xb_nsftype.getSpatialTrend();
		String tTrendString = xb_nsftype.getTemporalTrend();
		if (xb_samples!=null){
			SampleReference ref = null;
			try {
				URL sampleReference = new URL(xb_samples.getHref());
				String mimeType = xb_nsftype.getSamples().getType();
				ref = new SampleReference(mimeType, sampleReference);
			} catch (MalformedURLException e) {
				throw new UncertaintyParserException(
						"Error while parsing sample Reference of NormalSpatialField!");
			}

			if (spTrendString==null&&tTrendString==null){
				return new NormalSpatioTemporalField(ref, gp);
			}
		}

		else {
			if (spTrendString==null&&tTrendString==null){
				throw new UncertaintyParserException("Either trend parameters or samples or both have to be set in Normal spatio-temporal field");
			}
			else {
				double[] spatialTrend = RandomVariablesUtil
						.parseTrendCoefficients(spTrendString);
				double[] temporalTrend = RandomVariablesUtil
						.parseTrendCoefficients(tTrendString);
				return new NormalSpatioTemporalField(gp, spatialTrend,
						temporalTrend);
			}
		}

		double[] spatialTrend = RandomVariablesUtil
				.parseTrendCoefficients(spTrendString);
		double[] temporalTrend = RandomVariablesUtil
				.parseTrendCoefficients(tTrendString);
		return new NormalSpatioTemporalField(gp, spatialTrend,
				temporalTrend);
	}

	/**
	 * parses a normal timeseries
	 *
	 * @param xbObject
	 * 			XMLBeans representation of NormalTimeSeries
	 * @return NormalTimeSeries object
	 * @throws UncertaintyParserException#
	 * 			if parsing fails
	 */
	private IUncertainty parseNormalTimeSeries(NormalTimeSeriesDocument xbObject)
			throws UncertaintyParserException {
		NormalTimeSeriesType xb_nsftype = ((NormalTimeSeriesDocument) xbObject)
				.getNormalTimeSeries();
		INormalCovarianceParameter gp = parseCovarianceParameter(xb_nsftype
				.getCovarianceParameter());

		ReferenceType xb_samples = xb_nsftype.getSamples();
		String temporalTrendString = xb_nsftype.getTemporalTrend();
		if (xb_samples!=null){
			SampleReference ref = parseSampleReference(xb_samples);
			if (temporalTrendString!=null&&!temporalTrendString.equals("")){
				double[] temporalTrend = RandomVariablesUtil
						.parseTrendCoefficients(temporalTrendString);
				return new NormalTimeSeries(ref, gp, temporalTrend);
			}
			else {
				return new NormalTimeSeries(ref, gp);
			}
		}
		else {
			if (temporalTrendString!=null&&!temporalTrendString.equals("")){
				double[] temporalTrend = RandomVariablesUtil
						.parseTrendCoefficients(temporalTrendString);
				return new NormalTimeSeries(gp, temporalTrend);
			}
			else {
				throw new UncertaintyParserException("Either temporal trend or samples or both have to be set in normal time series!!");
			}
		}


	}

	/**
	 * parses a spatial normal field
	 *
	 * @param xbObject
	 * @return
	 * @throws UncertaintyParserException
	 */
	private IUncertainty parseNormalSpatialField(
			NormalSpatialFieldDocument xbObject)
			throws UncertaintyParserException {
		NormalSpatialFieldType xb_nsftype = ((NormalSpatialFieldDocument) xbObject)
				.getNormalSpatialField();
		INormalCovarianceParameter gp = parseCovarianceParameter(xb_nsftype
				.getCovarianceParameter());
		ReferenceType xb_samples = xb_nsftype.getSamples();
		String spatialTrendString = xb_nsftype.getSpatialTrend();
		SampleReference ref = null;
		if (xb_samples != null) {
			ref = parseSampleReference(xb_samples);
			if (spatialTrendString == null) {
				return new NormalSpatialField(ref, gp);
			} else {
				double[] spatialTrend = RandomVariablesUtil
						.parseTrendCoefficients(spatialTrendString);
				return new NormalSpatialField(ref, gp, spatialTrend);
			}
		} else if (spatialTrendString != null && !spatialTrendString.equals("")){
			if (spatialTrendString != null && !spatialTrendString.equals("")) {
				double[] spatialTrend = RandomVariablesUtil
						.parseTrendCoefficients(spatialTrendString);
				return new NormalSpatialField(gp, spatialTrend);
			}
			else {
				throw new UncertaintyParserException("Either trend parameter or samples or both have to be contained in random field!!");
			}
		}
		else {
			throw new UncertaintyParserException("Either trend parameter or samples or both have to be contained in random field!!");
		}
	}



	/**
	 * helper method for parsing the sample reference
	 *
	 * @param samples
	 *            XMLBeans representation of sample reference
	 * @return returns parsed sample reference
	 * @throws UncertaintyParserException
	 *             if parsing fails
	 */
	private SampleReference parseSampleReference(ReferenceType samples)
			throws UncertaintyParserException {
		try {
			URL sampleReference = new URL(samples.getHref());
			String mimeType = samples.getMimeType();
			return new SampleReference(mimeType, sampleReference);
		} catch (MalformedURLException e) {
			throw new UncertaintyParserException(
					"Error while parsing sample Reference of NormalSpatialField!");
		}
	}

	/**
	 * helper method for parsing a covariance parameters
	 *
	 * @param covarianceParameter
	 *            XMLBeans representation of covariance parameter
	 * @return Returns parsed parameter
	 * @throws UncertaintyParserException
	 *             if parsing fails
	 */
	private INormalCovarianceParameter parseCovarianceParameter(
			CovarianceParameterType covarianceParameter)
			throws UncertaintyParserException {
		INormalCovarianceParameter result = null;
		// covarianceMatrix parsing
		CovarianceMatrix covMatrix = covarianceParameter.getCovarianceMatrix();
		if (covMatrix != null) {
			XMLParser umlParser = new XMLParser();
			IUncertainty cvm = umlParser.parse(covMatrix.toString());
			if (cvm instanceof org.uncertml.statistic.CovarianceMatrix) {
				result = new CovarianceMatrixParameter(
						(org.uncertml.statistic.CovarianceMatrix) cvm);
			}
		}

		// covariance matrix is not set, so parse variogramFunction parameter
		else {
			VariogramFunctionType xb_vf = covarianceParameter
					.getVariogramFunction();
			VariogramFunctionDocument xb_vfd = VariogramFunctionDocument.Factory
					.newInstance();
			xb_vfd.addNewVariogramFunction().set(xb_vf);
			result = (VariogramFunction) parseVariogramFunction(xb_vfd);

		}
		return result;
	}

	/**
	 * helper method for parsing a variogram
	 *
	 * @param xbObject
	 *            XMLBeans representation of variogram
	 * @return variogram
	 */
	private IUncertainty parseVariogramFunction(
			VariogramFunctionDocument xbObject) {
		VariogramFunction vario;
		VariogramFunctionType xb_vg = xbObject.getVariogramFunction();
		double sill, range, nugget;
		double kappa = Double.NaN;
		Model model;
		sill = xb_vg.getSill();
		range = xb_vg.getRange();
		nugget = xb_vg.getNugget();
		String modelString = xb_vg.getModel().toString();
		model = Model.valueOf(modelString);
		if (model.equals(Model.Mat)) {
			kappa = xb_vg.getKappa();
		}
		Anisotropy xbAnis = xb_vg.getAnisotropy();
		org.uncertml.statistics.Anisotropy anis = null;
		if (xbAnis != null) {
			double pDirection = xbAnis.getPrincipalDirection();
			double ratio = xbAnis.getRatio();
			anis = new org.uncertml.statistics.Anisotropy(pDirection, ratio);
		}
		vario = new VariogramFunction(sill, range, nugget, kappa, model, anis);
		return vario;
	}

}
