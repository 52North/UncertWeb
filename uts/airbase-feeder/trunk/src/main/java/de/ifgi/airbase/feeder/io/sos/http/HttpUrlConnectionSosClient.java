
package de.ifgi.airbase.feeder.io.sos.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Properties;

import de.ifgi.airbase.feeder.util.Utils;

/**
 * @author Christian Autermann
 */
public class HttpUrlConnectionSosClient extends TransactionalSosClient {

    private static final String CONNECTION_TIMEOUT_PROPERTY = "sun.net.client.defaultConnectTimeout";
    private static final String READ_TIMEOUT_PROPERTY = "sun.net.client.defaultReadTimeout";
    private static final String SEND_DATA_PROPERTY = "eea.sendData";
    private static final String MIME_TYPE_HTTP_HEADER = "Content-Type";
    private static final String XML_MIME_TYPE = "application/xml";
    private static final String HTTP_METHOD = "POST";
    private static final int CONNECTION_TIMEOUT = getInteger(CONNECTION_TIMEOUT_PROPERTY);
    private static final int READ_TIMEOUT = getInteger(READ_TIMEOUT_PROPERTY);
    private boolean sendingData = getBoolean(SEND_DATA_PROPERTY);

    /**
     * 
     */
    public HttpUrlConnectionSosClient() {
        super();
        if ( !this.sendingData)
            log.warn("Not sending any data, just printing it out!");
    }

    private static int getInteger(String key) {
        String value = Utils.get(key);
        int val = Integer.MAX_VALUE;
        if (value != null) {
            try {
                val = Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                log.error("Could not parse key.", e);
            }
        }
        return val;
    }

    private static boolean getBoolean(String key) {
        String value = Utils.get(key);
        boolean val = true;
        if (value != null) {
            val = Boolean.parseBoolean(value);
        }
        return val;
    }

    @Override
    protected InputStream post(String xml) throws IOException, SocketTimeoutException {
        if ( !this.sendingData) {
            log.warn("Not sending any data!");
            System.out.println(xml);
            throw new RuntimeException("Debugging, not sending any data!");
        }

        OutputStreamWriter wr = null;
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty(CONNECTION_TIMEOUT_PROPERTY, String.valueOf(CONNECTION_TIMEOUT));
        systemProperties.setProperty(READ_TIMEOUT_PROPERTY, String.valueOf(READ_TIMEOUT));
        try {
            HttpURLConnection conn = (HttpURLConnection) this.getUrl().openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod(HTTP_METHOD);
            conn.setRequestProperty(MIME_TYPE_HTTP_HEADER, XML_MIME_TYPE);
            log.debug("Sending Request...");
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(xml);
            wr.flush();
            return conn.getInputStream();
        }
        catch (SocketTimeoutException e) {
            return null;
        }
        finally {
            if (wr != null)
                wr.close();
            System.getProperties().remove(CONNECTION_TIMEOUT_PROPERTY);
            System.getProperties().remove(READ_TIMEOUT_PROPERTY);
        }
    }
}
