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
package org.uncertweb.viss.mongo.resource;

import java.net.URI;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.resource.time.ITemporalExtent;

public class MongoOMDataSet extends
		AbstractMongoDataSet<IObservationCollection> {

	private String phenomenon;
	
	public MongoOMDataSet(MongoOMResource resource, URI phenomenon) {
		super(resource);
		this.phenomenon = phenomenon.toString();
	}

	public MongoOMDataSet() {
	}

	@Override
	public String getPhenomenon() {
		return this.phenomenon;
	}

	@Override
	public UncertaintyType getType() {
		Set<UncertaintyType> set = UwCollectionUtils.set();
		for (AbstractObservation ao : getContent().getObservations()) {
			IResource r = (IResource) ao.getResult().getValue();
			for (IDataSet ds : r.getDataSets()) {
				set.add(ds.getType());
			}
		}
		if (set.size() == 1) {
			return set.iterator().next();
		} else {
			return null;
		}
	}

	@Override
	protected ITemporalExtent loadTemporalExtent() {
		Set<DateTime> instants = UwCollectionUtils.set();
		Set<Interval> intervals = UwCollectionUtils.set();
		for (AbstractObservation ao : getContent().getObservations()) {
			//TODO respect temporal extent of referenced resource
			TimeObject to = ao.getPhenomenonTime();
			if (to.getInterval() != null) {
				intervals.add(to.getInterval());
			} else if (to.getDateTime() != null) {
				instants.add(to.getDateTime());
			}
		}
		return getExtent(instants, intervals);
	}

	@Override
	protected IObservationCollection loadContent() {
		return ((MongoOMResource) getResource()).getObservationsForPhenomenon(getPhenomenon());
	}

	@Override
	public String getUom() {
		String uom = null;
		for (AbstractObservation ao : getContent().getObservations()) {
			IResource referencedResource = (IResource) ao.getResult().getValue();
			String uom2 = referencedResource.getDataSets().iterator().next().getUom();
			if (uom == null) {
				uom = uom2;
			} else if (!uom.equals(uom2)) {
				throw VissError.internal("Different UOMs");
			}
		}
		return uom;
	}

}
