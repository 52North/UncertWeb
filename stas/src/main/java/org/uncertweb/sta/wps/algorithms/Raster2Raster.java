package org.uncertweb.sta.wps.algorithms;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.ProcessOutput;
import org.uncertweb.sta.wps.api.SingleProcessInput;


/**
 * represents a Raster2Raster approach with 
 * 
 * @author staschc
 *
 */
public class Raster2Raster extends AbstractAggregationProcess{
	
	/**
	 * identifier of aggregation process
	 */
	public static final String IDENTIFIER = "urn:ogc:def:aggregationProcess:spatialGridding:spatialMean:noTemporalGrouping:noTemporalAggregation";

	/**
	 * The URL of the SOS from which the {@link ObservationCollection} will be
	 * fetched. Can also be a GET request.
	 */
	public static final SingleProcessInput<String> TARGETGRID = new SingleProcessInput<String>(
			"TargetGrid",
			UncertWebIODataBinding.class, 0, 1, null, null);
	
	/**
	 * xoffset of target grid
	 */
	public static final SingleProcessInput<String> XOFFSET = new SingleProcessInput<String>(
			"XOffset",
			LiteralDoubleBinding.class, 0, 1, null, null);
	
	/**
	 * yoffset of target grid
	 */
	public static final SingleProcessInput<String> YOFFSET = new SingleProcessInput<String>(
			"YOffset",
			LiteralDoubleBinding.class, 0, 1, null, null);
	
	/**
	 * constructor
	 * 
	 */
	public Raster2Raster(){
		
	}
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	protected Set<AbstractProcessInput<?>> getInputs() {
		Set<AbstractProcessInput<?>> result = super.getCommonProcessInputs();
		result.add(TARGETGRID);
		result.add(XOFFSET);
		result.add(YOFFSET);
		return result;
	}

	@Override
	protected Set<ProcessOutput> getOutputs() {
		return getCommonProcessOutputs();
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		// TODO Auto-generated method stub
		return null;
	}

}
