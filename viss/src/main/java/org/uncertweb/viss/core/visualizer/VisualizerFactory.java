package org.uncertweb.viss.core.visualizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Utils;

@SuppressWarnings("unchecked")
public class VisualizerFactory {
	private static final Logger log = LoggerFactory
			.getLogger(VisualizerFactory.class);
	private static Map<String, Class<? extends Visualizer>> creatorsByShortName = Utils
			.map();
	private static Map<MediaType, Set<Class<? extends Visualizer>>> creatorsByMediaType = Utils
			.map();

	static {
		InputStream is = Visualizer.class
				.getResourceAsStream("/creators.config");
		if (is == null) {
			throw new RuntimeException("can not load creator config.");
		}
		try {
			List<?> creatorNames = IOUtils.readLines(is);
			for (Object o : creatorNames) {
				String name = ((String) o).trim();
				if (!name.startsWith("#") && !name.isEmpty()) {
					try {
						analyzeVisualizer((Class<? extends Visualizer>) Class
								.forName(name));
					} catch (Exception e) {
						throw new RuntimeException(
								"can not instantiate creator", e);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void analyzeVisualizer(Class<? extends Visualizer> c)
			throws InstantiationException, IllegalAccessException {
		String shortName = Visualizer.getShortName(c);
		log.info("Registered Visualizer \"{}\" from class {}", shortName,
				c.getName());
		creatorsByShortName.put(shortName, c);
		for (MediaType mt : Visualizer.getCompatibleMediaTypes(c)) {
			Set<Class<? extends Visualizer>> set = creatorsByMediaType.get(mt);
			if (set == null) {
				creatorsByMediaType.put(mt, set = Utils.set());
			}
			set.add(c);
		}
		// TODO
	}

	public static Set<Visualizer> getVisualizers() {
		Set<Visualizer> vs = Utils.set();
		for (String name : getNames()) {
			vs.add(getVisualizer(name));
		}
		return vs;
	}

	public static Visualizer getVisualizer(String shortname) {
		return fromName(shortname, creatorsByShortName);
	}

	public static Set<Class<? extends Visualizer>> getVisualizerForMediaType(
			MediaType mt) {
		Set<Class<? extends Visualizer>> set = creatorsByMediaType.get(mt);
		if (set == null)
			set = Utils.set();
		return Collections.unmodifiableSet(set);
	}

	private static Visualizer fromName(String name,
			Map<String, Class<? extends Visualizer>> map) {
		Class<? extends Visualizer> vc = map.get(name);
		if (vc == null)
			throw VissError.noSuchVisualizer();
		try {
			return vc.newInstance();
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	public static Set<String> getNames() {
		return Collections.unmodifiableSet(creatorsByShortName.keySet());
	}

	public static Set<Class<? extends Visualizer>> getClasses() {
		return Collections.unmodifiableSet(Utils.asSet(creatorsByShortName
				.values()));
	}

	public static Set<Visualizer> getVisualizerForResource(Resource resource) {
		// TODO Auto-generated method stub
		return null;
	}
}