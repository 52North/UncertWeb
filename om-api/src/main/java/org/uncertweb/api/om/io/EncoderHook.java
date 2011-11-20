package org.uncertweb.api.om.io;

import net.opengis.om.x20.OMObservationDocument;

import org.uncertweb.api.om.observation.AbstractObservation;

public interface EncoderHook {
	public void encode(AbstractObservation ao, OMObservationDocument xml);
}
