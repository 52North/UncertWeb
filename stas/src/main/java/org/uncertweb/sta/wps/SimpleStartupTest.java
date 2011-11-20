package org.uncertweb.sta.wps;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleStartupTest {
	
	private static final String CONFIG_PATH = SimpleStartupTest.class.getResource("/wps_config/wps_config.xml").getFile();
	protected static final Logger log = LoggerFactory.getLogger(SimpleStartupTest.class);

	static {
		// Logging.GEOTOOLS.forceMonolineConsoleOutput();
		try {
			WPSConfig.forceInitialization(CONFIG_PATH);
		} catch (XmlException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ParserFactory.initialize(WPSConfig.getInstance().getRegisteredParser());
		GeneratorFactory.initialize(WPSConfig.getInstance().getRegisteredGenerator());
	}
	
	
	public static void main(String[] args) {
		for (IParser p : ParserFactory.getInstance().getAllParsers()) {
			System.out.println(p.getClass().getName());
		}
		
		for (IGenerator p : GeneratorFactory.getInstance().getAllGenerators()) {
			System.out.println(p.getClass().getName());
		}
		STARepository r = new STARepository();
		for (String name : r.getAlgorithmNames()) {
			System.out.println(r.getProcessDescription(name));
			
		}
	}
}
