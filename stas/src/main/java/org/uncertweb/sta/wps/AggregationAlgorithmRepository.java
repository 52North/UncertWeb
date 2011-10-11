package org.uncertweb.sta.wps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.request.ExecuteRequest;
import org.uncertweb.sta.wps.algorithms.vector2vector.PolyConMeanTempGridMax;

/**
 * 
 * repository that loads the aggregation algorithms as defined in the config file
 * 
 * @author staschc
 *
 */
public class AggregationAlgorithmRepository implements IAlgorithmRepository{
	
	private static Logger LOGGER = Logger.getLogger(PolyConMeanTempGridMax.class);
	
	/**
	 * Mapping between process identifier and methods.
	 */
	private Map<String, IAlgorithm> algorithms;
	
	
	public AggregationAlgorithmRepository(){
		this.algorithms=loadComplexProcesses();
	}

	@Override
	public boolean containsAlgorithm(String processID) {
		return algorithms.containsKey(processID);
	}

	@Override
	public IAlgorithm getAlgorithm(String processID,
			ExecuteRequest executeRequest) {
		return algorithms.get(processID);
	}

	@Override
	public Collection<String> getAlgorithmNames() {
		return algorithms.keySet();
	}

	@Override
	public Collection<IAlgorithm> getAlgorithms() {
		return algorithms.values();
	}

	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		// TODO check whether this hack is correct!
		return algorithms.get(processID).getDescription();
	}
	
	/**
	 * helper method; loads the algorithms as defined in the aggregationProcessConfig.xml file
	 * 
	 * @return
	 */
	private static Map<String, IAlgorithm> loadComplexProcesses() {
		Map<String, IAlgorithm> result = new HashMap<String,IAlgorithm>();
		AggregationServiceConfiguration processConfig = AggregationServiceConfiguration.getInstance();
		Iterator<String> processIter = processConfig.getAllProcessIdentifiers().iterator();
		while (processIter.hasNext()){
			String processID = processIter.next();
			String clazz = processConfig.getClassName4ProcIdentifier(processID);
			try {
				IAlgorithm algo = (IAlgorithm) AggregationAlgorithmRepository.class.getClassLoader().loadClass(clazz).newInstance();
				result.put(processID, algo);
			} catch (Exception e) {
				LOGGER.debug("Error while loading aggregation algorithms: "+e.getLocalizedMessage());			 
			}
		}
		return result;
	}
	
	
}
