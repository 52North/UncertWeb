package org.uncertweb.sta.wps;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.uncertweb.sta.utils.Utils;


public class CompositeProcessInput<T> extends IProcessInput<T> {
	
	private Set<IProcessInput<?>> inputs;
	private ProcessInputHandler<T> handler;
	private String id;
	
	public CompositeProcessInput(String id, ProcessInputHandler<T> handler, IProcessInput<?>... inputs) {
		this.inputs = Utils.set(inputs);
		this.id = id;
		this.handler = handler;
		this.handler.setNeededInputs(this.getProcessInputs());
	}
	
	public Set<SingleProcessInput<?>> getProcessInputs() {
		HashSet<SingleProcessInput<?>> inputs = new HashSet<SingleProcessInput<?>>();
		for (IProcessInput<?> p : this.inputs) {
			inputs.addAll(p.getProcessInputs());
		}
		return inputs;
	}

	public T handle(Map<String, List<IData>> inputs) {
		return this.handler.process(inputs);
	}

	@Override
	public String getId() {
		return this.id;
	}
}
