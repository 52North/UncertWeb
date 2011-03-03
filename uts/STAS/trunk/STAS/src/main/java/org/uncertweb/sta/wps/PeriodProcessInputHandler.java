package org.uncertweb.sta.wps;

import java.util.List;
import java.util.Map;

import org.joda.time.Period;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AlgorithmParameterException;
import org.uncertweb.intamap.utils.TimeUtils;

public class PeriodProcessInputHandler extends ProcessInputHandler<Period> {
	
	@Override
	protected Period processInputs(Map<String, List<IData>> inputs) {
		String id = this.getNeededInputs().iterator().next().getId();
		String parameter = (String) inputs.get(id).get(0).getPayload();
		if (parameter == null || parameter.trim().isEmpty()) {
			throw new AlgorithmParameterException("Parameter \"" + id
					+ "\" not found.");
		}
		return TimeUtils.parsePeriod(parameter).toPeriod();
	}

}
