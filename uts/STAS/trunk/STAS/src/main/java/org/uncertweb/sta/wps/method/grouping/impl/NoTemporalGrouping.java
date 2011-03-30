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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.annotation.TemporalPartitioningPredicate;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;

/**
 * Groups a collection of {@code Observation}s by their {@code SamplingTime}.
 * Only {@code Observation}s with the same {@code SamplingTime} are grouped
 * together.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
@TemporalPartitioningPredicate(Constants.MethodNames.Grouping.Temporal.NO_GROUPING)
public class NoTemporalGrouping extends TemporalGrouping {

	/**
	 * {@code Iterator} that groups the {@code Observation} by their
	 * {@code SamplingTime} and iterates over them.
	 */
	private class MappingIterator implements
			Iterator<ObservationMapping<ObservationTime>> {

		/**
		 * The {@code List}-{@code Iterator} we delegate to.
		 */
		private Iterator<LinkedList<Observation>> i = null;

		/**
		 * Creates a new {@code Iterator}.
		 */
		public MappingIterator() {
			LinkedList<LinkedList<Observation>> list = new LinkedList<LinkedList<Observation>>();
			for (Observation o : getObservations()) {
				/* due some mystery in HashMap we have to do it this way... */
				LinkedList<Observation> toAddTo = null;
				for (LinkedList<Observation> lo : list) {
					if (lo.element().getObservationTime().hashCode() == o
							.getObservationTime().hashCode()) {
						toAddTo = lo;
					}
				}
				if (toAddTo == null) {
					toAddTo = new LinkedList<Observation>();
					list.add(toAddTo);
				}
				toAddTo.add(o);
			}
			log.info("Got {} distinct ObservationTimes.", list.size());
			i = list.iterator();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			return i.hasNext();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ObservationMapping<ObservationTime> next() {
			LinkedList<Observation> list = i.next();
			return new ObservationMapping<ObservationTime>(list.element()
					.getObservationTime(), list);
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
	public Iterator<ObservationMapping<ObservationTime>> iterator() {
		return new MappingIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractProcessInput<?>> getAdditionalInputDeclarations() {
		return Utils.set();
	}

	/**
	 * Creates a new {@code TemporalGrouping}.
	 */
	public NoTemporalGrouping() {}

	/**
	 * Creates a new {@code TemporalGrouping}.
	 * 
	 * @param obs the observations to group
	 */
	public NoTemporalGrouping(List<Observation> obs) {
		setInputs(obs, null);
	}

}
