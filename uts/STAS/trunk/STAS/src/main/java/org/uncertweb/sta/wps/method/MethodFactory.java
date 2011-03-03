package org.uncertweb.sta.wps.method;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.grouping.spatial.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.TemporalGrouping;

/**
 * @author Christian Autermann
 */
public class MethodFactory {

	private static final String CONFIG = "/methods.conf";
	private static final Logger log = LoggerFactory.getLogger(MethodFactory.class);
	private static MethodFactory singleton;
	
	private HashSet<String> temporalMethods;
	private HashSet<String> spatialMethods;
	private HashSet<String> aggregationMethods;
	
	
	
	private MethodFactory() {
		loadMethods();
	}
	
	public static MethodFactory getInstance() {
		if (singleton == null)
			singleton = new MethodFactory();
		return singleton;
	}

	public Set<String> getTemporalGroupingMethods() {
		return temporalMethods;
	}

	public Set<String> getSpatialGroupingMethods() {
		return spatialMethods;
	}

	public Set<String> getAggregationMethods() {
		return aggregationMethods;
	}
	
	private void loadMethods() {
		temporalMethods = new HashSet<String>();
		spatialMethods = new HashSet<String>();
		aggregationMethods = new HashSet<String>();
		for (Class<?> clazz : parseConfigFile()) {
			if (fitsInterface(TemporalGrouping.class, clazz)) {
				temporalMethods.add(clazz.getName());
				log.info("Method class registered: {}", clazz.getName());
			}
			if (fitsInterface(SpatialGrouping.class, clazz)) {
				spatialMethods.add(clazz.getName());
				log.info("Method class registered: {}", clazz.getName());
			}
			if (fitsInterface(AggregationMethod.class, clazz)) {
				aggregationMethods.add(clazz.getName());
				log.info("Method class registered: {}", clazz.getName());
			}
		}
	}
	
	private LinkedList<Class<?>> parseConfigFile() {
		try {
			LinkedList<Class<?>> result = new LinkedList<Class<?>>();
			List<String> lines = IOUtils.readLines(getClass().getResourceAsStream(CONFIG));
			for (String line : lines) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#") ) {
					result.add(Class.forName(line));
				}
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Can not load methods from configuration file.",e);
		}
	}

	private boolean fitsInterface(Class<?> interfase, Class<?> test) {
		int modifiers = test.getModifiers();
		return !Modifier.isInterface(modifiers) && 
			   !Modifier.isAbstract(modifiers) && 
			   interfase.isAssignableFrom(test) ;
	}
	
	public static void main(String[] args) {
		MethodFactory.getInstance();
	}
}