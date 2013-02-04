package de.ifgi.airbase.feeder.io.sos;
import java.io.IOException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.io.sos.http.RequestFailedException;
import de.ifgi.airbase.feeder.io.sos.http.TransactionalSosClient;
import de.ifgi.airbase.feeder.util.Utils;

/**
 * 
 * @author Christian Autermann
 *
 */
public abstract class SosClient {
	protected static final Logger log = LoggerFactory.getLogger(SosClient.class);

	/**
	 * Instantiates a {@link SosClient}.
	 * 
	 * @param url
	 *            the {@link URL}
	 * @param path
	 *            where the {@link SosClient} should print requests
	 *            and responses. Can be <code>null</code>.
	 * @return the {@link SosClient}
	 */
	public static final SosClient newInstance() {
		String clazz = Utils.get("eea.sosClient");
		SosClient sosClient = null;
		if (clazz == null) {
			sosClient = new TransactionalSosClient();
		} else {
			try {
				sosClient = (SosClient) Class.forName(clazz).newInstance();
			} catch (Exception e) {
				log.warn("Can not in instantiate specified SosClient. Falling back to default one", e);
				sosClient = new TransactionalSosClient();
			}
		}
		return sosClient;
	}
	
	
	/**
	 * Registers a {@code EEAStation} to a SOS.
	 * 
	 * @param station
	 *            the {@code EEAStation}
	 * @throws IOException
	 *             if an IO error occurs.
	 */
	public abstract void registerStation(EEAStation station) throws IOException, RequestFailedException;

	/**
	 * Inserts a {@code EEARawDataFile} into a SOS.
	 * 
	 * @param file
	 *            the {@code EEARawDataFile} to be inserted
	 * @throws IOException
	 *             if an IO error occurs.
	 */
	public abstract void insertObservations(EEARawDataFile file) throws IOException, RequestFailedException;

}