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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.ObservationTimeComparator;
import org.uncertweb.sta.wps.PeriodInputHandler;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.SingleProcessInput;
import org.uncertweb.sta.wps.api.annotation.TemporalPartitioningPredicate;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * Groups an {@code Observation} collection by their SamplingTime in intervals
 * of a given size. The {@code Observation}s will be sorted and the first
 * {@code SamplingTime} starts the first interval.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
@TemporalPartitioningPredicate(Constants.MethodNames.Grouping.Temporal.TEMPORAL_GRIDDING)
public class TemporalGridding extends TemporalGrouping {

	/**
	 * The {@link Period} of time in which observations will be grouped.
	 */
	public static final SingleProcessInput<Period> TIME_RANGE = new SingleProcessInput<Period>(
			Constants.Process.Inputs.TIME_RANGE_ID, LiteralStringBinding.class,
			1, 1, null, null, new PeriodInputHandler());

	/**
	 * Iterator that iterates over the {@link Interval}s.
	 */
	private class TimeRangeMappingIterator implements
			Iterator<ObservationMapping<TimeObject>> {

		/**
		 * The sorted {@link Observation}s iterator.
		 */
		private Iterator<? extends AbstractObservation> iter;

		/**
		 * The first {@code Observation} of the next interval.
		 */
		private AbstractObservation o;

		/**
		 * The next {@code Interval}.
		 */
		private Interval ci;

		/**
		 * Creates a new {@code Iterator}. Sorts the {@code Observation}s by
		 * their {@code SampleTime} and creates the first {@code Interval}.
		 */
		public TimeRangeMappingIterator() {
			List<? extends AbstractObservation> observations = getObservations();
			Collections.sort(observations, new ObservationTimeComparator());
			Period p = (Period) getInputs().get(TIME_RANGE);

			iter = observations.iterator();
			if (iter.hasNext()) {
				o = iter.next();
				DateTime begin = null;
				if (o.getPhenomenonTime().isInterval()) {
					begin = o.getPhenomenonTime().getInterval().getStart();
				} else {
					begin = o.getPhenomenonTime().getDateTime();
				}
				ci = new Interval(begin, p);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			return (o != null);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ObservationMapping<TimeObject> next() {
			List<AbstractObservation> obs = UwCollectionUtils.list();
			while (hasNext() && isInRange(ci, o)) {
				obs.add(o);
				if (iter.hasNext()) {
					o = iter.next();
				} else {
					o = null;
				}
			}
			ObservationMapping<TimeObject> mapping = new ObservationMapping<TimeObject>(
					new TimeObject(ci), obs);
			ci = new Interval(ci.getEnd(), ci.toPeriod());
			return mapping;
		}

		/**
		 * Tests if the {@code  Observation} is in the range of the
		 * {@code Interval}.
		 *
		 * @param time the interval
		 * @param test the observation
		 * @return <code>true</code> if the {@code Observation} is in the
		 *         {@code Interval}, otherwise <code>false</code>
		 */
		private boolean isInRange(Interval time, AbstractObservation test) {
			if (test.getPhenomenonTime().isInterval()) {
				return time.contains(test.getPhenomenonTime().getInterval());
			} else {
				return time.contains(test.getPhenomenonTime().getDateTime());
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<ObservationMapping<TimeObject>> iterator() {
		return new TimeRangeMappingIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractProcessInput<?>> getAdditionalInputDeclarations() {
		HashSet<AbstractProcessInput<?>> set = new HashSet<AbstractProcessInput<?>>();
		set.add(TIME_RANGE);
		return set;
	}
}
