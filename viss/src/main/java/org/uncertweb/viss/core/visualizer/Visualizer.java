package org.uncertweb.viss.core.visualizer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Utils;

public abstract class Visualizer {

	protected static final Logger log = LoggerFactory
			.getLogger(Visualizer.class);

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface ShortName {
		public String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Description {
		public String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Compatible {
		public String[] value();
	}

	public static String getShortName(Class<? extends Visualizer> c) {
		ShortName s = c.getAnnotation(ShortName.class);
		if (s != null) {
			return s.value();
		} else {
			log.warn("Class {} has no attached shortname.", c);
			return c.getName();
		}
	}

	public static String getDescription(Class<? extends Visualizer> c) {
		Description s = c.getAnnotation(Description.class);
		return s == null ? null : s.value();
	}

	public static Set<MediaType> getCompatibleMediaTypes(
			Class<? extends Visualizer> c) {
		Compatible s = null;
		Class<?> cl = c;
		while (s == null && cl != null) {
			s = cl.getAnnotation(Compatible.class);
			cl = cl.getSuperclass();
		}
		Set<MediaType> mts = Utils.set();
		if (s != null) {
			for (String mt : s.value()) {
				mts.add(MediaType.valueOf(mt));
			}
		}
		return mts;
	}

	public String getShortName() {
		return getShortName(this.getClass());
	}

	public boolean isCompatible(MediaType mt) {
		return getCompatibleMediaTypes(this.getClass()).contains(mt);
	}

	public String getDescription() {
		return getDescription(this.getClass());
	}

	public abstract boolean isCompatible(Resource r);

	public abstract JSONObject getOptions();

	public abstract Visualization visualize(Resource r, JSONObject params);
}
