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

import java.io.File;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.utils.UwIOUtils;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Indexed;
import com.github.jmkgreen.morphia.annotations.Polymorphic;
import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.annotations.Property;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.annotations.Transient;

@Polymorphic
@Entity("resources")
public abstract class AbstractMongoResource<T> implements IResource {
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractMongoResource.class);
    public static final String TIME_PROPERTY = "lastUsage";
    public static final String CHECKSUM_PROPERTY = "checksum";
    public static final String MEDIA_TYPE_PROPERTY = "mediaType";

    @Id
    private ObjectId oid;

    @Property(MEDIA_TYPE_PROPERTY)
    private MediaType mediaType;

    @Indexed
    @Property(TIME_PROPERTY)
    private DateTime lastUsage;
    private File file;

    @Indexed(unique = true)
    @Property(CHECKSUM_PROPERTY)
    private long checksum;

    @Reference
    private Set<IDataSet> dataSets;

    @Transient
    private T content;

    public AbstractMongoResource(MediaType mt, File f, ObjectId oid, long checksum) {
        setMediaType(mt);
        setFile(f);
        setId(oid);
        setChecksum(checksum);
    }

    public AbstractMongoResource(MediaType mt) {
        setMediaType(mt);
    }

    public AbstractMongoResource() {
    }

    @Override
    public void setMediaType(MediaType mt) {
        this.mediaType = mt;
    }

    @Override
    public MediaType getMediaType() {
        return this.mediaType;
    }

    @Override
    public ObjectId getId() {
        return this.oid;
    }

    public void setId(ObjectId oid) {
        this.oid = oid;
    }

    @Override
    public Object getResource() {
        return this.content;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public DateTime getLastUsage() {
        return lastUsage;
    }

    public void setLastUsage(DateTime lastUsage) {
        this.lastUsage = lastUsage;
    }

    public T getContent() {
        if (content == null) {
            content = loadContent();
        }
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    protected T getContentIfLoaded() {
        return content;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    @PrePersist
    public void prePersist() {
        setLastUsage(new DateTime());
    }

    @PostLoad
    public void postLoad() {
        setLastUsage(new DateTime());
    }

    @Override
    public Set<IDataSet> getDataSets() {
        if (dataSets == null) {
            dataSets = createDataSets();
        }
        return Collections.unmodifiableSet(dataSets);
    }

    public void setDataSets(Set<IDataSet> dataSets) {
        this.dataSets = dataSets;
    }

    protected abstract T loadContent();

    protected abstract Set<IDataSet> createDataSets();

    protected void finalize() {
        UwIOUtils.closeQuietly(this);
    }

}
