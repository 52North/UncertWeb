package org.uncertweb.sta.wps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.n52.wps.server.request.ExecuteRequest;
import org.reflections.Reflections;
import org.uncertweb.sta.wps.algorithms.AbstractAggregationProcess;

/**
 * 
 * repository that loads the aggregation algorithms as defined in the config file
 * 
 * @author staschc
 *
 */
public class AggregationAlgorithmRepository implements IAlgorithmRepository{
	
	protected static Logger log = Logger.getLogger(AggregationAlgorithmRepository.class);
	
	
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
		
		Reflections r = new Reflections("org.uncertweb.sta.wps.algorithms");
		
		for (Class<? extends AbstractAggregationProcess> c : r
				.getSubTypesOf(AbstractAggregationProcess.class)) {
			try {
				//Hack for checking that no abstract class is returned; there seems to be no possibility to check whether the class is abstract;
				//abstract class causes newInstance to throw exception
				if (!c.getCanonicalName().contains("Abstract")){
					AbstractAggregationProcess algo = c.newInstance();
					result.put(algo.getIdentifier(), algo);
				}
			} catch (Exception e) {
				String errorMsg = "Error while loading aggregation algorithms:"+e.getLocalizedMessage();
				throw new RuntimeException(errorMsg);
			}	
		}
		return result;

	}
	
	
}
