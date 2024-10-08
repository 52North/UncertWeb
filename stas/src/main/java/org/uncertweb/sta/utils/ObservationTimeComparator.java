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
package org.uncertweb.sta.utils;

import java.util.Comparator;

import org.joda.time.DateTime;
import org.uncertweb.api.om.observation.AbstractObservation;

/**
 * Compares the {@code TimeObject }s of {@link AbstractObservation}s.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ObservationTimeComparator implements
		Comparator<AbstractObservation> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(AbstractObservation o1, AbstractObservation o2) {
		DateTime a, b;
		a = (o1.getPhenomenonTime().isInstant()) ? o1.getPhenomenonTime()
				.getDateTime() : o1.getPhenomenonTime().getInterval()
				.getStart();
		b = (o2.getPhenomenonTime().isInstant()) ? o2.getPhenomenonTime()
				.getDateTime() : o2.getPhenomenonTime().getInterval()
				.getStart();
		return a.compareTo(b);
	}
}