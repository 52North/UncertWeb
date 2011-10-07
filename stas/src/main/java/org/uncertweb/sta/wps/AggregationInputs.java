package org.uncertweb.sta.wps;

import java.net.URL;
import java.util.List;

import org.n52.wps.io.data.UncertWebIOData;

/**
 * class represents common aggregation inputs of all Input Parameters
 * 
 * @author staschc
 *
 */
public class AggregationInputs {
	
	
	/**variables whose data should be aggregated*/
	private List<String> variables;
	
	/**input data that should be aggregated*/
	private UncertWebIOData input;
	
	/**indicates whether spatial aggregation should be done first; default is false*/
	private boolean isSpatialFirst=false;
	
	/**URL of the server to which the output should be written; might be null*/
	private URL targetServer;
	
	/**type of the server to which data should be written*/
	private String targetServerType; 
	
	/**
	 * constructor with mandatory parameters
	 * 
	 * @param identifierp
	 * 			identifier of the aggregation process
	 * @param variablesp
	 * 			list containing the variables whose values should be aggregated
	 * @param inputp
	 * 			data that should be aggregated
	 */
	public AggregationInputs(List<String> variablesp, UncertWebIOData inputp){
		setVariables(variablesp);
		setInput(inputp);
	}

	/**
	 * @return the variables
	 */
	public List<String> getVariables() {
		return variables;
	}

	/**
	 * @param variables the variables to set
	 */
	public void setVariables(List<String> variables) {
		this.variables = variables;
	}

	/**
	 * @return the input
	 */
	public UncertWebIOData getInput() {
		return input;
	}

	/**
	 * @param input the input to set
	 */
	public void setInput(UncertWebIOData input) {
		this.input = input;
	}

	/**
	 * @return the isSpatialFirst
	 */
	public boolean isSpatialFirst() {
		return isSpatialFirst;
	}

	/**
	 * @param isSpatialFirst the isSpatialFirst to set
	 */
	public void setSpatialFirst(boolean isSpatialFirst) {
		this.isSpatialFirst = isSpatialFirst;
	}

	/**
	 * @return the targetServer
	 */
	public URL getTargetServer() {
		return targetServer;
	}

	/**
	 * @param targetServer the targetServer to set
	 */
	public void setTargetServer(URL targetServer) {
		this.targetServer = targetServer;
	}

	/**
	 * @return the targetServerType
	 */
	public String getTargetServerType() {
		return targetServerType;
	}

	/**
	 * @param targetServerType the targetServerType to set
	 */
	public void setTargetServerType(String targetServerType) {
		this.targetServerType = targetServerType;
	}
}
