package org.uncertweb.sta.wps;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.request.ExecuteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.sta.wps.method.MethodFactory;
import org.uncertweb.sta.wps.method.grouping.spatial.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.TemporalGrouping;

public class STARepository implements IAlgorithmRepository {
	private static final Logger log = LoggerFactory
			.getLogger(STARepository.class);

	private Map<String, Class<?>[]> algos;

	public STARepository() {
		algos = new HashMap<String, Class<?>[]>();
		for (String sgClass : MethodFactory.getInstance()
				.getSpatialGroupingMethods()) {
			Class<?> clazz = null;
			try {
				clazz = (Class<?>) Class.forName(sgClass);
			} catch (ClassNotFoundException e) {
				log.error("Class {} not found.", sgClass);
				continue;
			}
			String className = sgClass.substring(sgClass.lastIndexOf(".") + 1);
			for (String tgClass : MethodFactory.getInstance()
					.getTemporalGroupingMethods()) {

				Class<?> clazz2;
				try {
					clazz2 = (Class<?>) Class.forName(tgClass);
				} catch (ClassNotFoundException e) {
					log.error("Class {} not found.", tgClass);
					continue;
				}
				String className2 = tgClass
						.substring(tgClass.lastIndexOf(".") + 1);
				String identifier = className + "." + className2;
				algos.put(identifier, new Class<?>[] { clazz, clazz2 });
				log.info("Registered Algorithm: {}", identifier);
			}
		}
	}

	@Override
	public Collection<String> getAlgorithmNames() {
		return algos.keySet();
	}

	@Override
	public Collection<IAlgorithm> getAlgorithms() {
		LinkedList<IAlgorithm> result = new LinkedList<IAlgorithm>();
		for (String id : algos.keySet()) {
			IAlgorithm a = instantiate(id);
			if (a != null)
				result.add(a);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private IAlgorithm instantiate(String id) {
		Class<?>[] methodClasses = algos.get(id);
		IAlgorithm a = instantiate(id, id.replace(".", " "),
				(Class<? extends SpatialGrouping>) methodClasses[0],
				(Class<? extends TemporalGrouping>) methodClasses[1]);
		if (a.processDescriptionIsValid())
			return a;
		log.error("ProcessDescription is not valid for {}.", id);
		return null;
	}
	
	protected IAlgorithm instantiate(String id, String title, Class<? extends SpatialGrouping> sg, Class<? extends TemporalGrouping> tg) {
		return new GenericObservationAggregationProcess(id, title, sg, tg);
	}
	
	
	@Override
	public boolean containsAlgorithm(String processID) {
		return algos.containsKey(processID);
	}

	@Override
	public IAlgorithm getAlgorithm(String processID, ExecuteRequest executeRequest) {
		return instantiate(processID);
	}

	@Override
	public ProcessDescriptionType getProcessDescription(String arg0) {
		return instantiate(arg0).getDescription();
	}

}
