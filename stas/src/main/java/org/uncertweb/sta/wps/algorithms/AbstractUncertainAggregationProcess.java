package org.uncertweb.sta.wps.algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.AggregationInputs;
import org.uncertweb.sta.wps.UncertainAggregationInputs;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.SingleProcessInput;

/**
 * class is superclass for error-aware spatio-temporal aggregation processes
 *
 * @author staschc
 *
 */
public abstract class AbstractUncertainAggregationProcess extends
		AbstractAggregationProcess {

	// //////////////////////////////////////////////////////////////////////////7
	// Common Aggregation Process Inputs as described in Profile
	/**
	 * input that indicates the numbers of MonteCarlo runs
	 */
	public final SingleProcessInput<Integer> NUMBER_REAL = new SingleProcessInput<Integer>(
			Constants.Process.Inputs.NUMBER_REAL, LiteralIntBinding.class,
			0, 1, null, 1);

	/**
	 * Indicates the type of uncertainty that should be returned
	 */
	protected final SingleProcessInput<String> OUTPUT_UNCERTAINTY_TYPE = new SingleProcessInput<String>(
			Constants.Process.Inputs.OUTPUT_UNCERTAINTY,
			LiteralStringBinding.class, 0, 100, null,
			"http://www.uncertml.org/samples/realisation");

	/**
	 *
	 * @return the identifier of the process
	 */
	public abstract String getIdentifier();

	/**
	 *
	 * @return the identifier of the process
	 */
	public abstract List<String> getSupportedUncertaintyTypes();

	/**
	 *
	 * @return the identifier of the process
	 */
	public abstract Map<String, IData> runMonteCarlo(Map<String, List<IData>> inputData);

	/**
	 * returns the standard parameters of every error aware aggregation process
	 *
	 * @param inputData
	 *            map containing the inputs as served by the 52N WPS framework
	 * @return Returns {@link AggregationInputs} containing standard inputs of
	 *         every error aware aggregation process
	 */
	protected UncertainAggregationInputs getUncertainAggregationInputs4Inputs(
			Map<String, List<IData>> inputData) {

		// extract Variable
		List<IData> uoutputTypesList = inputData
				.get(Constants.Process.Inputs.OUTPUT_UNCERTAINTY);
		ArrayList<String> uoutputTypes = null;
		if (uoutputTypesList!=null){
			uoutputTypes = new ArrayList<String>(
					uoutputTypesList.size());
			Iterator<IData> uotIter = uoutputTypesList.iterator();
			List<String> supportedUncertaintTypes = getSupportedUncertaintyTypes();
			while (uotIter.hasNext()) {
				String outputUncertaintyType = ((LiteralStringBinding) uotIter
						.next()).getPayload();
				if (supportedUncertaintTypes.contains(outputUncertaintyType)){
					uoutputTypes.add(outputUncertaintyType);
				}
				else {
					throw new RuntimeException("Uncertainty type " + outputUncertaintyType + " is not supported by the aggregation algorithm " + getIdentifier()+" !");
				}
			}
		}

		int numberOfRealisations = 1;
		List<IData> norList = inputData
				.get(Constants.Process.Inputs.NUMBER_REAL);
		if (norList != null && norList.size() == 1) {
			LiteralIntBinding spfBb = (LiteralIntBinding) norList.get(0);
			numberOfRealisations = spfBb.getPayload();
		}

		return new UncertainAggregationInputs(uoutputTypes,
				numberOfRealisations);
	}

	/**
	 * returns type of data binding class for passed input identifier
	 *
	 * @param identifier
	 *            identifier of input
	 * @return data binding class
	 */
	protected Class<?> getCommonInputType(String identifier) {
		if (identifier.equals(Constants.Process.Inputs.INPUT_DATA)) {
			return UncertWebIODataBinding.class;
		}
		else if (identifier.equals(Constants.Process.Inputs.VARIABLE)) {
			return LiteralStringBinding.class;
		}

		else if (identifier
				.equals(Constants.Process.Inputs.SPATIAL_BEFORE_TEMPORAL)) {
			return LiteralStringBinding.class;
		}

		else if (identifier.equals(Constants.Process.Inputs.TARGET_SERVER)) {
			return LiteralStringBinding.class;
		}

		else if (identifier
				.equals(Constants.Process.Inputs.TARGET_SERVER_TYPE)) {
			return LiteralStringBinding.class;
		}

		////////////////////////////////////////////////
		// inputs of error aware aggregation processes
		else if (identifier.equals(Constants.Process.Inputs.NUMBER_REAL)) {
			return LiteralIntBinding.class;
		}

		else if (identifier
				.equals(Constants.Process.Inputs.OUTPUT_UNCERTAINTY)) {
			return LiteralStringBinding.class;
		}

		else
			return null;
	}

	/**
	 *
	 *
	 * @return {@link Set} containing the common process inputs of all
	 *         aggregation processes
	 */
	protected Set<AbstractProcessInput<?>> getCommonProcessInputs() {
		Set<AbstractProcessInput<?>> set = super.getCommonProcessInputs();
		set.add(NUMBER_REAL);
		set.add(OUTPUT_UNCERTAINTY_TYPE);
		return set;
	}

}
