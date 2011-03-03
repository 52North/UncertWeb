package org.uncertweb.sta.wps;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;

public abstract class IProcessInput<T> {

	public abstract Set<SingleProcessInput<?>> getProcessInputs();

	public abstract T handle(Map<String, List<IData>> inputs);

	public abstract String getId();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IProcessInput other = (IProcessInput) obj;
		if (getId() == null && other.getId() != null)
			return false;
		if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}
