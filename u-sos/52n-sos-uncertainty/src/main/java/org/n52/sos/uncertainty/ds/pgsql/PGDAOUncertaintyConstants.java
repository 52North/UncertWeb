package org.n52.sos.uncertainty.ds.pgsql;

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
	// TODO add further uncertainty types here

	// /////////////////////////////////////////////////////////////////////////////////
	// table names
	public static final String uObsUncTn = "obs_unc";
	public static final String uUncertTn = "u_uncertainty";
	public static final String uValUnitTn = "u_value_unit";
	public static final String uNormTn = "u_normal";
	public static final String uMeanTn = "u_mean";
	public static final String uMeanValTn = "u_mean_values";

	// /////////////////////////////////////////////////////////////////////////////////
	// column names
	
	// column names of observation uncertainty relationship table
	public static final String uOUObsIdCn = "observation_id";
	public static final String uOUGmlIdCn = "gml_identifier";
	
	// column names of uncertainty table
	public static final String uUUncIdCn = "uncertainty_id";
	public static final String uUUncValIdCn = "uncertainty_values_id";
	public static final String uUUncTypeCn = "uncertainty_type";
	
	// column names of value unit table
	public static final String uVUValUnitCn = "value_unit";
	public static final String uVUValUnitIdCn = "value_unit_id";
	
	// column names of normal distribution type table
	public static final String uNNormIdCn = "normal_id";
	public static final String uNMeanCn = "mean";
	public static final String uNVarCn = "var";

	// column names of mean type table
	public static final String uMMeanIdCn = "mean_id";
	public static final String uMMeanValIdCn = "mean_values_id";
	
    // column names of mean values table
	public static final String uMVMeanValCn = "mean_value";
	 
}