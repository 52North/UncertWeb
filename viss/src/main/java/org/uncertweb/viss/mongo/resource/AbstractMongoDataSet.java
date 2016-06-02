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

import java.util.Set;

import org.bson.types.ObjectId;
import org.geotools.geometry.Envelope2D;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.netcdf.NcUwHelper;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.resource.time.AbstractTemporalExtent;
import org.uncertweb.viss.core.vis.IVisualization;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Polymorphic;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.annotations.Transient;
import com.vividsolutions.jts.geom.Point;

@Polymorphic
@Entity("datasets")
public abstract class AbstractMongoDataSet<T> implements IDataSet {

    @Id
    private ObjectId oid;

    @Reference
    private IResource resource;

    @Embedded
    private Set<IVisualization> visualizations = UwCollectionUtils.set();

    @Embedded
    private AbstractTemporalExtent temporalExtent;

    @Transient
    private T content;

    public AbstractMongoDataSet(IResource resource) {
        setResource(resource);
    }

    public AbstractMongoDataSet() {
    }

    @Override
    public void setResource(IResource r) {
        this.resource = r;
    }

    @Override
    public ObjectId getId() {
        return this.oid;
    }

    public void setId(Object o) {
        this.oid = (ObjectId) o;
    }

    @Override
    public IResource getResource() {
        return this.resource;
    }

    @Override
    public Set<IVisualization> getVisualizations() {
        return this.visualizations;
    }

    @Override
    public void addVisualization(IVisualization v) {
        this.visualizations.add(v);
    }

    @Override
    public void removeVisualization(IVisualization v) {
        this.visualizations.remove(v);
    }

    @Override
    public AbstractTemporalExtent getTemporalExtent() {
        if (this.temporalExtent == null) {
            this.temporalExtent = loadTemporalExtent();
        }
        return this.temporalExtent;
    }

    @Override
    public T getContent() {
        if (this.content == null) {
            setContent(loadContent());
        }
        return this.content;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setContent(Object c) {
        this.content = (T) c;
    }

    @Override
    public boolean hasTime(TimeObject t) {
        return getTemporalExtent().contains(t);
    }

    @Override
    public abstract Envelope2D getSpatialExtent();

    protected abstract T loadContent();

    protected abstract AbstractTemporalExtent loadTemporalExtent();

    @Override
    public boolean hasPoint(Point p) {
        Envelope2D e = getSpatialExtent();
        return e.contains(NcUwHelper.toDirectPosition(p,
                                                      e
                                                      .getCoordinateReferenceSystem()));
    }

}
