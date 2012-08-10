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
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.annotation.TemporalPartitioningPredicate;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * {@link TemporalGrouping} that maps all {@link Observation}s to one single
 * {@link ObservationTime}.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
@TemporalPartitioningPredicate(Constants.MethodNames.Grouping.Temporal.ONE_CONTAINING_TIME_RANGE)
public class OneContainingTimeRangeGrouping extends TemporalGrouping {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<ObservationMapping<TimeObject>> iterator() {
		DateTime start = null, end = null;
		log.info("Calculating TimeRange for {} Observations.",
				getObservations().size());
		for (AbstractObservation o : getObservations()) {

			if (o.getPhenomenonTime().isInterval()) {
				if (start == null
						|| o.getPhenomenonTime().getInterval().getStart()
								.isBefore(start)) {
					start = o.getPhenomenonTime().getInterval().getStart();
				}
				if (end == null
						|| o.getPhenomenonTime().getInterval().getEnd()
								.isAfter(end)) {
					end = o.getPhenomenonTime().getInterval().getEnd();
				}
			} else {

				if (start == null
						|| o.getPhenomenonTime().getDateTime().isBefore(start)) {
					start = o.getPhenomenonTime().getDateTime();
				}
				if (end == null
						|| o.getPhenomenonTime().getDateTime().isAfter(end)) {
					end = o.getPhenomenonTime().getDateTime();
				}
			}
		}
		TimeObject time = (start.equals(end)) ? new TimeObject(start) : new TimeObject(start, end);
		return UwCollectionUtils.list(new ObservationMapping<TimeObject>(time,
						getObservations())).iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractProcessInput<?>> getAdditionalInputDeclarations() {
		return UwCollectionUtils.set();
	}

}
