package org.uncertweb.sta.wps;

import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;

public class ClassProcessInputHandler extends ProcessInputHandler<Class<?>> {

	@Override
	protected Class<?> processInputs(Map<String, List<IData>> inputs) {
		String id = this.getNeededInputs().iterator().next().getId();
		String parameter = (String) inputs.get(id).get(0).getPayload();
		if (parameter != null) {
			try {
				return Class.forName(parameter.trim());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} catch (ClassCastException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
