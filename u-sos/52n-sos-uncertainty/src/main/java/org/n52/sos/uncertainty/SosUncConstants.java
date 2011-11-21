package org.n52.sos.uncertainty;

import javax.xml.namespace.QName;

import org.n52.sos.SosConstants;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.uncertainty.decode.impl.OM2Constants;


public final class SosUncConstants {
	
	// ////////////////////////////////////////////////////////
    // resultModel constants
	/**
     * Constant for uncertainty observations, which are returned if the value of an
     * observation is a numerical value
     */
    public static final QName RESULT_MODEL_UNCERTAINTY_OBSERVATION = new QName(OM2Constants.NS_OM2, "OM_UncertaintyObservation",
            OM2Constants.NS_OM2_PREFIX);
	
    /**
     * Constant for result model of a measurement as return type in observation
     * collection
     */
    public static final QName RESULT_MODEL_MEASUREMENT = new QName(OMConstants.NS_OM, "OM_Measurement",
    		OM2Constants.NS_OM2_PREFIX);
    
    /**
     * Array of constants for result models.
     */
    public static final QName[] RESULT_MODELS = { RESULT_MODEL_UNCERTAINTY_OBSERVATION, SosConstants.RESULT_MODEL_OBSERVATION, SosConstants.RESULT_MODEL_MEASUREMENT,
    	SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION, SosConstants.RESULT_MODEL_SPATIAL_OBSERVATION };
    
	/**
	 * enumeration of value types
	 * 
	 * @author Stasch, Kiesow
	 */
	public enum ValueTypes {
		uncertaintyType, textType, numericType, booleanType, countType, categoryType, isoTimeType, spatialType, commonType, externalReferenceType, referenceValueTextType, referenceValueNumericType, referenceValueExternalReferenceType;

		/**
		 * method checks whether the string parameter is contained in this
		 * enumeration
		 * 
		 * @param s
		 *            the name which should be checked
		 * @return true if the name is contained in the enumeration
		 */
		public static boolean contains(String s) {
			boolean contained = false;
			contained = (s.equals(ValueTypes.uncertaintyType.name()))
					|| (s.equals(ValueTypes.textType.name()))
					|| (s.equals(ValueTypes.numericType.name()))
					|| (s.equals(ValueTypes.booleanType.name()))
					|| (s.equals(ValueTypes.countType.name()))
					|| (s.equals(ValueTypes.categoryType.name()))
					|| (s.equals(ValueTypes.isoTimeType.name()))
					|| (s.equals(ValueTypes.spatialType.name()))
					|| (s.equals(ValueTypes.externalReferenceType.name()))
					|| (s.equals(ValueTypes.referenceValueTextType.name()))
					|| (s.equals(ValueTypes.referenceValueNumericType.name()))
					|| (s.equals(ValueTypes.referenceValueExternalReferenceType
							.name()));
			return contained;
		}
	}
}
