package org.n52.sos.decode.impl.uncertainty;

/**
 * additional constants for handling O&M 2 observations and uncertainties
 * 
 * @author Kiesow
 */
public final class OM2Constants {

	// if non-static constants are added, this class will have to
	// be initialized first

	// observation type constants
	public static final String OBS_TYPE_BOOLEAN = "OM_BooleanObservation";
	public static final String OBS_TYPE_DISCNUM = "OM_DiscreteNumericObservation";
	public static final String OBS_TYPE_MEASUREMENT = "OM_Measurement";
	public static final String OBS_TYPE_REFERENCE = "OM_ReferenceObservation";
	public static final String OBS_TYPE_TEXT = "OM_TextObservation";
	public static final String OBS_TYPE_UNCERTAINTY = "OM_UncertaintyObservation";

}
