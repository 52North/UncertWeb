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
package org.uncertweb.viss.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmkgreen.morphia.Datastore;
import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.converters.DefaultConverters;
import com.github.jmkgreen.morphia.converters.TypeConverter;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MongoDB {
	private static final Logger LOG = LoggerFactory.getLogger(MongoDB.class);
	private static final String PROPERTIES_FILE = "/mongo.properties";
	private static final String HOST_PROPERTY = "host";
	private static final String PORT_PROPERTY = "port";
	private static final String AUTH_PROPERTY = "auth";
	private static final String USER_PROPERTY = "user";
	private static final String PASS_PROPERTY = "pass";
	private static final String DATABASE_PROPERTY = "database";
	private static MongoDB instance;
	private final Mongo mongo;
	private final Morphia morphia;
	private final Datastore datastore;
	private final String database;

	protected MongoDB() {
		try {

			InputStream is = getClass().getResourceAsStream(PROPERTIES_FILE);
			Properties p = new Properties();

			if (is != null) {
				try {
					p.load(is);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}

			String host = p.getProperty(HOST_PROPERTY);
			host = (host == null || host.trim().isEmpty()) ? "localhost" : host;
			String port = p.getProperty(PORT_PROPERTY);
			port = (port == null || port.trim().isEmpty()) ? "27017" : port;
            this.mongo = new MongoClient(new ServerAddress(host, Integer.valueOf(port)));

			this.morphia = new Morphia();
			String pkg = getClass().getPackage().getName();
			Reflections r = new Reflections(pkg);
			LOG.info("Search for Converters in {}", pkg);
			DefaultConverters dc = this.morphia.getMapper().getConverters();
			for (Class<? extends TypeConverter> c : r
			    .getSubTypesOf(TypeConverter.class)) {
				LOG.info("Registering Morphia TypeConverter {}", c.getName());
				dc.addConverter(c);
			}

			String auth = p.getProperty(AUTH_PROPERTY);
			auth = (auth == null || auth.trim().isEmpty()) ? "false" : auth.trim();
			String dbna = p.getProperty(DATABASE_PROPERTY);
            this.database = dbna.trim().isEmpty() ? "viss" : dbna;

			if (Boolean.valueOf(auth)) {
				this.datastore = this.morphia.createDatastore(this.mongo,
				    this.database, p.getProperty(USER_PROPERTY, "mongo"), p
				        .getProperty(PASS_PROPERTY, "mongo").toCharArray());
			} else {
				this.datastore = this.morphia
				    .createDatastore(this.mongo, this.database);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
	}

	public Mongo getMongo() {
		return this.mongo;
	}

	public Morphia getMorphia() {
		return this.morphia;
	}

	public Datastore getDatastore() {
		return this.datastore;
	}

	public String getDatabase() {
		return this.database;
	}
    public static MongoDB getInstance() {
        return (instance == null) ? instance = new MongoDB() : instance;
    }
}
