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
package org.uncertweb.sta.wps.method.grouping.impl;

import java.util.Iterator;
import java.util.Set;

import org.joda.time.DateTime;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.intamap.om.ObservationTimeInstant;
import org.uncertweb.intamap.om.ObservationTimeInterval;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;

/**
 * {@link TemporalGrouping} that maps all {@link Observation}s to one single
 * {@link ObservationTime}.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class IgnoreTimeGrouping extends TemporalGrouping {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<ObservationMapping<ObservationTime>> iterator() {
		DateTime start = null, end = null;
		log.info("Calculating TimeRange for {} Observations.", getObservations().size());
		for (Observation o : getObservations()) {
			if (o.getObservationTime() instanceof ObservationTimeInterval) {
				ObservationTimeInterval time = (ObservationTimeInterval) o.getObservationTime();
				if (start == null || time.getStart().isBefore(start)) {
					start = time.getStart();
				}
				if (end == null || time.getEnd().isAfter(end)) {
					end = time.getEnd();
				}
			} else {
				ObservationTimeInstant time = (ObservationTimeInstant) o.getObservationTime();
				if (start == null || time.isBefore(start)) {
					start = time.getDateTime();
				}
				if (end == null || time.isAfter(end)) {
					end = time.getDateTime();
				}
			}
		}
		ObservationTime time = (start.equals(end)) ? new ObservationTimeInstant(start)
		 										   : new ObservationTimeInterval(start, end);
		return Utils.mutableSingletonList(new ObservationMapping<ObservationTime>(time, getObservations())).iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractProcessInput<?>> getAdditionalInputDeclarations() {
		return Utils.set();
	}

}
