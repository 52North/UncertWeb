package org.n52.sos.ds.pgsql.uncertainty;

/**
 * additional constants for handling the uncertainties
 * 
 * @author Kiesow
 */
public final class PGDAOUncertaintyConstants {

	// if non-static constants are added, PGSQLDAOFactory will have to
	// initialize this class

	// /////////////////////////////////////////////////////////////////////////////////
	// uncertainty type constants
	public static final String u_normalDistType = "norm_dist";
	public static final String u_meanType = "mean";

	// /////////////////////////////////////////////////////////////////////////////////
	// table names
	public static final String uObsUnc = "obs_unc";
	public static final String uUncert = "u_uncertainty";
	public static final String uValUnit = "u_value_unit";
	public static final String uNorm = "u_normal";
	public static final String uMean = "u_mean";
	public static final String uMeanVal = "u_mean_values";

	// /////////////////////////////////////////////////////////////////////////////////
	// column names
	
	// column names of uncertainty table
	public static final String uUUncID = "uncertainty_id";
	public static final String uUUncValID = "uncertainty_values_id";
	public static final String uUUncType = "uncertainty_type";
	public static final String uUValUnitID = "value_unit_id";
	
	// column names of value unit table
	public static final String uVUValUnit = "value_unit";
	
	// column names of normal distribution type table
	public static final String uNNormID = "normal_id";
	public static final String uNMean = "mean";
	public static final String uNStDev = "standardDeviation";

	// column names of mean type table
	public static final String uMMeanID = "mean_id";
	public static final String uMMValID = "mean_values_id";
	
    // column names of mean values table
	public static final String uMVMeanVal = "mean_value";
	 
}
