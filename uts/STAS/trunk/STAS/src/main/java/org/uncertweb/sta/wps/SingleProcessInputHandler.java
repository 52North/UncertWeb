package org.uncertweb.sta.wps;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;

public class SingleProcessInputHandler<T> extends ProcessInputHandler<T> {

	@Override
	@SuppressWarnings({"unchecked","rawtypes"})
	protected T processInputs(Map<String, List<IData>> inputs) {
		String id = this.getNeededInputs().iterator().next().getId();
		log.info(id);
		List<IData> data = inputs.get(id);
		if (data == null)
			return null;
		switch(data.size()) {
		case 0: return null;
		case 1: return (T) data.get(0).getPayload();
		default: 
			List t = new LinkedList();
			for (IData d : data) { t.add(d.getPayload()); }
			return (T) t;
		}
	}
	
}
