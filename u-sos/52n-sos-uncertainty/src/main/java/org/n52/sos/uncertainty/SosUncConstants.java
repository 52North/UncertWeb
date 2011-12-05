package org.n52.sos.uncertainty;

import javax.xml.namespace.QName;

import org.n52.sos.SosConstants;
import org.n52.sos.uncertainty.decode.impl.OM2Constants;


public final class SosUncConstants {
	
	/** Constant for the content type of the response */
    public static final String CONTENT_TYPE_OM2 = "text/xml;subtype=\"om/2.0.0\"";
	
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
    public static final QName RESULT_MODEL_MEASUREMENT = new QName(OM2Constants.NS_OM2, "OM_Measurement",
    		OM2Constants.NS_OM2_PREFIX);
    
    /**
     * Array of constants for result models.
     */
    public static final QName[] RESULT_MODELS = { RESULT_MODEL_UNCERTAINTY_OBSERVATION, SosConstants.RESULT_MODEL_OBSERVATION, SosConstants.RESULT_MODEL_MEASUREMENT,
    	SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION, SosConstants.RESULT_MODEL_SPATIAL_OBSERVATION };
}
