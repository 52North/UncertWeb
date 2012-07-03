package org.uncertweb.api.om.io;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.uncertweb.api.om.observation.AbstractObservation;

public abstract class AbstractHookedObservationEncoder<T> implements
		IObservationEncoder {

	public interface EncoderHook<T> {
		public void encode(AbstractObservation ao, T encodedObject);
	}

	/** encoder hooks */
	private final Set<EncoderHook<T>> hooks = new HashSet<EncoderHook<T>>();

	public AbstractHookedObservationEncoder(Collection<EncoderHook<T>> hooks) {
		if (hooks != null) {
			for (EncoderHook<T> eh : hooks) {
				registerHook(eh);
			}
		}
	}

	/**
	 * @return the hooks of this encoder
	 */
	public final Set<EncoderHook<T>> getHooks() {
		return this.hooks;
	}
	
	public final Set<EncoderHook<T>> getHooks(Class<?> toEncode) {
		return this.hooks;
	}

	/**
	 * @param hook
	 *            the hook to register
	 */
	public final void registerHook(EncoderHook<T> hook) {
		if (hook != null) {
			synchronized (hooks) {
				hooks.add(hook);
			}
		}
	}
}
