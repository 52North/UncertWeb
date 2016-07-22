package org.uncertweb.sta.wps.algorithms;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.AggregationInputs;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.ExtendedSelfDescribingAlgorithm;
import org.uncertweb.sta.wps.api.SingleProcessInput;


/**
 * abstract super class for all aggregation processes; provides helper method for extracting the common parameters
 * of every aggregation process
 *
 * @author staschc
 *
 */
public abstract class AbstractAggregationProcess extends ExtendedSelfDescribingAlgorithm {




	////////////////////////////////////////////////////////////////////////////7
	// Common Aggregation Process Inputs as described in Profile
	/**
	 * The URL of the SOS from which the {@link ObservationCollection} will be
	 * fetched. Can also be a GET request.
	 */
	public static final SingleProcessInput<String> VARIABLE = new SingleProcessInput<String>(
			Constants.Process.Inputs.VARIABLE,
			LiteralStringBinding.class, 1, 100, null, null);



	/**
	 * Indicates if the temporal aggregation should run before the spatial
	 * aggregation.
	 */
	public static final SingleProcessInput<Boolean> SPATIAL_BEFORE_TEMPORAL = new SingleProcessInput<Boolean>(
			Constants.Process.Inputs.SPATIAL_BEFORE_TEMPORAL,
			LiteralBooleanBinding.class, 0, 1, null,
			Constants.getDefaultFlag(Constants.Process.Inputs.SPATIAL_BEFORE_TEMPORAL, true));


	/**
	 * The URL of the TargetServer to which aggregated data should be written
	 *
	 */
	public static final SingleProcessInput<String> TARGET_SERVER = new SingleProcessInput<String>(
			Constants.Process.Inputs.TARGET_SERVER,
			LiteralStringBinding.class, 0, 1, null, null);

	/**
	 * The URL of the TargetServer to which aggregated data should be written
	 *
	 */
	public static final SingleProcessInput<String> TARGET_SERVER_TYPE = new SingleProcessInput<String>(
			Constants.Process.Inputs.TARGET_SERVER_TYPE,
			LiteralStringBinding.class, 0, 1, null, null);

	/**
	 *
	 * @return the identifier of the process
	 */
	public abstract String getIdentifier();


	/**
	 * returns the standard parameters of every aggregation process
	 *
	 * @param inputData
	 * 			map containing the inputs as served by the 52N WPS framework
	 * @return Returns {@link AggregationInputs} containing standard inputs of every aggregation process
	 */
	protected AggregationInputs getAggregationInputs4Inputs(Map<String, List<IData>> inputData){

		AggregationInputs result = null;

		//extract Variable
		List<IData> variableList = inputData.get(Constants.Process.Inputs.VARIABLE);
		ArrayList<String> variables = new ArrayList<String>(variableList.size());
		Iterator<IData> variableIter = variableList.iterator();
		while (variableIter.hasNext()){
			String variable = ((LiteralStringBinding)variableIter.next()).getPayload();
			variables.add(variable);
		}

		//create Aggregation Inputs object
		result = new AggregationInputs(variables);

		//extract SpatialFirst
		List<IData> spatialFirstList = inputData.get(Constants.Process.Inputs.SPATIAL_BEFORE_TEMPORAL);
		if (spatialFirstList!=null&&spatialFirstList.size()==1){
			LiteralBooleanBinding spfBb = (LiteralBooleanBinding)spatialFirstList.get(0);
			result.setSpatialFirst(spfBb.getPayload());
		}

		//extract TargetServer
		List<IData> targetServerList = inputData.get(Constants.Process.Inputs.TARGET_SERVER);
		if (targetServerList!=null&&targetServerList.size()==1){
			LiteralStringBinding tsSb = (LiteralStringBinding)targetServerList.get(0);
			try {
				result.setTargetServer(new URL(tsSb.getPayload()));
			} catch (MalformedURLException e) {
				throw new RuntimeException("Error while retrieving target server URL in aggregation process:"+e.getLocalizedMessage());
			}
		}

		//extract TargetServerType
		List<IData> targetServerType = inputData.get(Constants.Process.Inputs.TARGET_SERVER_TYPE);
		if (targetServerType!=null&&targetServerType.size()==1){
			LiteralStringBinding spfBb = (LiteralStringBinding)targetServerType.get(0);
			result.setTargetServerType(spfBb.getPayload());
		}

		return result;
	}

	/**
	 * returns type of data binding class for passed input identifier
	 *
	 * @param identifier
	 * 			identifier of input
	 * @return data binding class
	 */
	protected Class<?> getCommonInputType(String identifier){
		if (identifier.equals(Constants.Process.Inputs.INPUT_DATA)){
			return UncertWebIODataBinding.class;
		}
		else if (identifier.equals(Constants.Process.Inputs.VARIABLE)){
			return LiteralStringBinding.class;
		}
		else if (identifier.equals(Constants.Process.Inputs.SPATIAL_BEFORE_TEMPORAL)){
			return LiteralStringBinding.class;
		}

		else if (identifier.equals(Constants.Process.Inputs.TARGET_SERVER)){
			return LiteralStringBinding.class;
		}
		else if (identifier.equals(Constants.Process.Inputs.TARGET_SERVER_TYPE)){
			return LiteralStringBinding.class;
		}
		else return null;
	}


	/**
	 *
	 *
	 * @return {@link Set} containing the common process inputs of all aggregation processes
	 */
	protected Set<AbstractProcessInput<?>> getCommonProcessInputs(){
		Set<AbstractProcessInput<?>> set = new HashSet<AbstractProcessInput<?>>();
		set.add(VARIABLE);
		set.add(SPATIAL_BEFORE_TEMPORAL);
		set.add(TARGET_SERVER);
		set.add(TARGET_SERVER_TYPE);
		return set;
	}

}
