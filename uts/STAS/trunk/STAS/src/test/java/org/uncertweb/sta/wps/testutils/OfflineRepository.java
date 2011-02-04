package org.uncertweb.sta.wps.testutils;

import org.n52.wps.server.IAlgorithm;
import org.uncertweb.sta.wps.STARepository;
import org.uncertweb.sta.wps.method.grouping.spatial.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.TemporalGrouping;

public class OfflineRepository extends STARepository {

	@Override
	protected IAlgorithm instantiate(String id, String title, Class<? extends SpatialGrouping> sg, Class<? extends TemporalGrouping> tg) {
		return new OfflineProcess(id, title, sg, tg);
	}

}
