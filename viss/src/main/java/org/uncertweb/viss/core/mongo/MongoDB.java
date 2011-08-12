package org.uncertweb.viss.core.mongo;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.converters.DefaultConverters;
import com.google.code.morphia.converters.TypeConverter;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

@SuppressWarnings("unchecked")
public class MongoDB {
	private static final Logger log = LoggerFactory.getLogger(MongoDB.class);

	private static final String HOST_PROPERTY = "host";
	private static final String PORT_PROPERTY = "port";
	private static final String AUTH_PROPERTY = "auth";
	private static final String USER_PROPERTY = "user";
	private static final String PASS_PROPERTY = "pass";
	private static final String DATABASE_PROPERTY = "database";
	private static final String CONVERTER_PROPERTY = "converter";

	private static MongoDB instance;

	public static MongoDB getInstance() {
		return (instance == null) ? instance = new MongoDB() : instance;
	}

	private final Mongo mongo;
	private final Morphia morphia;
	private final Datastore datastore;
	private final String database;

	protected MongoDB() {
		try {

			InputStream is = getClass()
					.getResourceAsStream("/mongo.properties");
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

			this.mongo = new Mongo(new ServerAddress(host,
					Integer.valueOf(port)));
			this.morphia = new Morphia();

			DefaultConverters dc = this.morphia.getMapper().getConverters();
			for (String className : p.getProperty(CONVERTER_PROPERTY, "")
					.split(",")) {
				Class<? extends TypeConverter> c = (Class<? extends TypeConverter>) Class
						.forName(className);
				log.info("Registering Morphia TypeConverter {}", c.getName());
				dc.addConverter(c);
			}

			String auth = p.getProperty(AUTH_PROPERTY);
			auth = (auth == null || auth.trim().isEmpty()) ? "false" : auth;
			String dbna = p.getProperty(DATABASE_PROPERTY);
			this.database = (auth == null || dbna.trim().isEmpty()) ? "viss"
					: dbna;

			if (Boolean.valueOf(auth)) {
				this.datastore = this.morphia.createDatastore(this.mongo,
						this.database, p.getProperty(USER_PROPERTY, "mongo"), p
								.getProperty(PASS_PROPERTY, "mongo")
								.toCharArray());
			} else {
				this.datastore = this.morphia.createDatastore(this.mongo,
						this.database);
			}
		} catch (Exception e) {
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
}
