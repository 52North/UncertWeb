package org.uncertweb.sta.wps;

import static java.text.MessageFormat.format;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.AlgorithmParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProcessInputHandler<T> {

	protected static final Logger log = LoggerFactory.getLogger(ProcessInputHandler.class);
	private Set<SingleProcessInput<?>> inputs;
	
	public T process(Map<String, List<IData>> inputs) {
		
		if (inputs == null) throw new NullPointerException();
		
		Map<String, List<IData>> rawInputs = new HashMap<String, List<IData>>();
		
		for (SingleProcessInput<?> p : this.getNeededInputs()) {
			List<IData> d = inputs.get(p.getId());
			if (d == null || d.isEmpty()) {
				if (p.getMinOccurs().compareTo(BigInteger.ZERO) > 0) {
					throw new AlgorithmParameterException(format("Missing '{1}' parameter.", p.getId()));
				}
			} else {
				BigInteger size = BigInteger.valueOf(d.size());
				if (p.getMinOccurs().compareTo(size) > 0) {
					throw new AlgorithmParameterException(format("Parameter '{1}' occurs only {2} times.", p.getId(), d.size()));
				} else if (p.getMaxOccurs().compareTo(size) < 0) {
					throw new AlgorithmParameterException(format("Parameter '{1}' can only occur {2} times.", p.getId(), p.getMaxOccurs()));
				} else {
					rawInputs.put(p.getId(), d);
				}
			}
		}
		
		return processInputs(rawInputs);
	}

	
	public void setNeededInputs(Set<SingleProcessInput<?>> inputs) {
		this.inputs = inputs;
	}
	
	protected Set<SingleProcessInput<?>> getNeededInputs() {
		return this.inputs;
	}
	
	protected abstract T processInputs(Map<String, List<IData>> inputs);

}
