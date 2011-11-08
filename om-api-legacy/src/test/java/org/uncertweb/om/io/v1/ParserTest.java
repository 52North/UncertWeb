package org.uncertweb.om.io.v1;

import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Test;

public class ParserTest {

	@Test
	public void testMeasurements() {
		InputStream in = getClass().getResourceAsStream("/measurement.xml");
		if (in == null) {
			fail();
		}
		OMDecoder dec = new OMDecoder();
	}

	@Test
	public void testObservations() {
		InputStream in = getClass().getResourceAsStream("/observation.xml");
		if (in == null) {
			fail();
		}

	}

	@Test
	public void testSurface() {
		InputStream in = getClass().getResourceAsStream("/samplingsurface.xml");
		if (in == null) {
			fail();
		}

	}

}
