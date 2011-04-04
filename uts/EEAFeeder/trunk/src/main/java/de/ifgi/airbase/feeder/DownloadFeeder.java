/**
 * 
 */

package de.ifgi.airbase.feeder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.util.Utils;

/**
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class DownloadFeeder extends Feeder {
    private static final Logger log = LoggerFactory.getLogger(DownloadFeeder.class);

    private static final String DOWNLOAD_BASERURL_PROPERTY = "eea.download.baseurl";
    private static final String DOWNLOAD_SUFFIX_PROPERTY = "eea.download.suffix";
    private static final String DOWNLOAD_COUNTRIES_PROPERTY = "eea.download.countries";
    private static final String DOWNLOAD_ALL_COUNTRIES_PROPERTY = "eea.download.countries.all";
    private static final String DOWNLOAD_OVERWRITE_PROPERTY = "eea.download.overwrite";
    private static final String COUNTRIES_DELIMITER = ";";

    private static final Map<String, String> COUNTRIES_TO_LOCALE;

    private static final int BUFFER_SIZE = 1024 * 4; // 4k buffer

    private static boolean downloadAllCountries = false;

    private static boolean overwriteDownloads = false;

    // provide a list with country name and locale
    static {
        downloadAllCountries = Boolean.parseBoolean(Utils.get(DOWNLOAD_ALL_COUNTRIES_PROPERTY));
        overwriteDownloads = Boolean.parseBoolean(Utils.get(DOWNLOAD_OVERWRITE_PROPERTY));

        // Tried to do it with locales, but not really possible, and the list seems to be fixed
        // Locale[] locales = Locale.getAvailableLocales();
        // see http://www.java2s.com/Tutorial/Java/0220__I18N/Getalistofcountrynames.htm
        COUNTRIES_TO_LOCALE = new HashMap<String, String>();
        COUNTRIES_TO_LOCALE.put("Andorra", "AD");
        COUNTRIES_TO_LOCALE.put("Austria", "AT");
        COUNTRIES_TO_LOCALE.put("Belgium", "BE");
        COUNTRIES_TO_LOCALE.put("Bosnia and Herzegovina", "BA");
        COUNTRIES_TO_LOCALE.put("Bulgaria", "BG");
        COUNTRIES_TO_LOCALE.put("Croatia", "HR");
        COUNTRIES_TO_LOCALE.put("Cyprus", "CY");
        COUNTRIES_TO_LOCALE.put("Czech Republic", "CZ");
        COUNTRIES_TO_LOCALE.put("Denmark", "DK");
        COUNTRIES_TO_LOCALE.put("Estonia", "EE");
        COUNTRIES_TO_LOCALE.put("Finland", "FI");
        COUNTRIES_TO_LOCALE.put("France", "FR");
        COUNTRIES_TO_LOCALE.put("Germany", "DE");
        COUNTRIES_TO_LOCALE.put("Greece", "GR");
        COUNTRIES_TO_LOCALE.put("Hungary", "HU");
        COUNTRIES_TO_LOCALE.put("Iceland", "IS");
        COUNTRIES_TO_LOCALE.put("Ireland", "IE");
        COUNTRIES_TO_LOCALE.put("Italy", "IT");
        COUNTRIES_TO_LOCALE.put("Latvia", "LV");
        COUNTRIES_TO_LOCALE.put("Liechtenstein", "LI");
        COUNTRIES_TO_LOCALE.put("Lithuania", "LT");
        COUNTRIES_TO_LOCALE.put("Luxembourg", "LU");
        COUNTRIES_TO_LOCALE.put("Macedonia, Former Yugoslav Rep", "MK");
        COUNTRIES_TO_LOCALE.put("Malta", "MT");
        COUNTRIES_TO_LOCALE.put("Netherlands", "NL");
        COUNTRIES_TO_LOCALE.put("Norway", "NO");
        COUNTRIES_TO_LOCALE.put("Poland", "PL");
        COUNTRIES_TO_LOCALE.put("Portugal", "PT");
        COUNTRIES_TO_LOCALE.put("Romania", "RO");
        COUNTRIES_TO_LOCALE.put("Serbia", "RS");
        COUNTRIES_TO_LOCALE.put("Slovakia", "SK");
        COUNTRIES_TO_LOCALE.put("Slovenia", "SI");
        COUNTRIES_TO_LOCALE.put("Sweden", "SE");
        COUNTRIES_TO_LOCALE.put("Switzerlands", "CH");
        COUNTRIES_TO_LOCALE.put("Turkey", "TR");
        COUNTRIES_TO_LOCALE.put("United Kingdom", "GB");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        log.info("Starting DownloadFeeder, overwriting existing download: " + overwriteDownloads);

        // create temp dir
        String directory;
        try {
            directory = Utils.createTempDir();
        }
        catch (IOException e) {
            log.error("Could not create temporary directory!", e);
            return;
        }

        // download files
        String[] countries = new String[1];
        if (downloadAllCountries)
            countries = COUNTRIES_TO_LOCALE.keySet().toArray(countries);
        else
            countries = Utils.get(DOWNLOAD_COUNTRIES_PROPERTY).split(COUNTRIES_DELIMITER);

        String baseurl = Utils.get(DOWNLOAD_BASERURL_PROPERTY);
        String suffix = Utils.get(DOWNLOAD_SUFFIX_PROPERTY);
        downloadFiles(baseurl, countries, suffix, directory);

        // start feeding
        Feeder eea2sos = new Feeder();
        eea2sos.process(directory);
    }

    /**
     * @param baseurl
     * @param countries
     * @param suffix
     * @param directory
     */
    private static void downloadFiles(String baseurl, String[] countries, String suffix, String directory) {
        log.info("Downloading files of {} countries from {}", countries.length, baseurl);

        // create urls
        URL[] urls = new URL[countries.length];

        for (int i = 0; i < countries.length; i++) {

            // remove "," and replace " " with "-", to small letters
            String country = countries[i].toLowerCase().replace(",", "").replace(" ", "-");
            country = country.toLowerCase() + "/" + country.toLowerCase();
            String urlString = baseurl + "/" + country + "/" + suffix;

            try {
                URL url = new URL(urlString);
                urls[i] = url;
            }
            catch (MalformedURLException e) {
                log.error("Could not create URL from " + urlString);
            }
        }

        // create target files
        String filePrefix = "AirBase_";
        String fileSuffix = "_v4.zip";
        String[] targetPaths = new String[countries.length];
        for (int i = 0; i < countries.length; i++) {
            String countryCode = COUNTRIES_TO_LOCALE.get(countries[i]);
            String filename = filePrefix + countryCode + fileSuffix;
            targetPaths[i] = directory + "/" + filename;
        }

        // download files
        downloadFilesToPaths(urls, targetPaths);
    }

    /**
     * @param urls
     * @param targetPaths
     */
    private static void downloadFilesToPaths(URL[] urls, String[] targetPaths) {
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            String path = targetPaths[i];
            log.debug("Downloading URL " + url + " to " + path);

            if (new File(targetPaths[i]).exists() && !overwriteDownloads) {
                log.warn("File already found, not downloading again: " + path);
                continue;
            }

            try {
                InputStream is = url.openStream();
                FileOutputStream fos = new FileOutputStream(path);

                BufferedOutputStream bout = new BufferedOutputStream(fos, BUFFER_SIZE);
                byte data[] = new byte[BUFFER_SIZE];
                int x = 0;
                while ( (x = is.read(data, 0, BUFFER_SIZE)) >= 0) {
                    bout.write(data, 0, x);
                }
                bout.close();
                is.close();
            }
            catch (IOException e) {
                log.error("Could not open stream to " + url.toString(), e);
            }
        }
    }

}
