/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24,
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.sta.wps;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.joda.time.Period;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AlgorithmParameterException;
import org.uncertweb.sta.wps.api.ProcessInputHandler;
import org.uncertweb.utils.UwTimeUtils;

/**
 * Class that handles a {@code String} input and creates a {@link Period}
 * object.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class PeriodInputHandler extends ProcessInputHandler<Period> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Period processInputs(Map<String, List<IData>> inputs) {
		String id = checkForOnlyOneInput().getId();
		List<IData> input = inputs.get(id);
		if (input != null && !input.isEmpty()) {
			if (input.size() > 1) {
				log.warn("Input '{}' has more than one IData. This class is not capable of multiple inputs.", id);
			}
			String parameter = (String) input.get(0).getPayload();
			if (parameter == null || parameter.trim().isEmpty()) {
				throw new AlgorithmParameterException(
						MessageFormat.format("Parameter '{1}' not found.", id));
			}
			return UwTimeUtils.parsePeriod(parameter);
		}
		return null;
	}

}
