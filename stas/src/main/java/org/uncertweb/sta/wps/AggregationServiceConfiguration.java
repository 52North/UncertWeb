package org.uncertweb.sta.wps;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * loads and represents the AggregationProcessConfiguration; processes need
 * to be configured in aggregationProcessConfig.xml 
 * 
 * @author staschc
 *
 */
public class AggregationServiceConfiguration {
	
	private static Logger LOGGER = Logger.getLogger(AggregationServiceConfiguration.class);
	
	/** singleton instance*/
	private static AggregationServiceConfiguration instance;
	
	/**
	 * map holds the configuration (class name of algorithm and semantics) for the process identifiers
	 * 
	 */
	private Map<String, AggregationProcessConfiguration> configs4pIdentifiers;
	
	/**
	 * constructor
	 */
	private AggregationServiceConfiguration(){
		readConfig();
	}
	
	/**
	 * 
	 * @return only existing instance of this class.
	 */
	public static AggregationServiceConfiguration getInstance(){
		if (instance==null){
			instance = new AggregationServiceConfiguration();
		}
		return instance;
	}
	
	/**
	 * returns semantics about the process as second element for a process identifier
	 * 
	 * @param processIdentifier
	 * 			identifier of an aggregation process
	 * @return list containing the classname of the algorithm implementation as first element and semantics about the process as second element for a process identifier
	 * 
	 */
	public String getClassSemantics4ProcIdentifier(String processIdentifier){
		return this.configs4pIdentifiers.get(processIdentifier).getSemantics();
	}
	
	/**
	 * returns a list containing the classname of the algorithm implementation as first element and semantics about the process as second element for a process identifier
	 * 
	 * @param processIdentifier
	 * 			identifier of an aggregation process
	 * @return list containing the classname of the algorithm implementation as first element and semantics about the process as second element for a process identifier
	 * 
	 */
	public String getClassName4ProcIdentifier(String processIdentifier){
		return this.configs4pIdentifiers.get(processIdentifier).getClassName();
	}
	
	/**
	 * 
	 * @return all aggregation process identifiers
	 */
	public Set<String> getAllProcessIdentifiers(){
		return this.configs4pIdentifiers.keySet();
	}
	
	/**
	 * helper method for reading the configuration
	 * 
	 */
	private void readConfig(){
		
		String configIs = this.getClass().getResource("/aggregationProcessConfig.xml").getFile();
		File fXmlFile = new File(configIs);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList processes = doc.getElementsByTagName("AggregationProcess");
			this.configs4pIdentifiers = new HashMap<String, AggregationProcessConfiguration>(processes.getLength());
			for (int i=0;i<processes.getLength();i++){
				NodeList childs = processes.item(i).getChildNodes();
				String identifier = null;
				String clazzName = null;
				String semantics = null;
				for (int j=0;j<childs.getLength();j++){
					Node child = childs.item(j);
					if (child instanceof Element){
						String name = child.getNodeName();
						if (name.equals("ProcessIdentifier")){
							identifier = child.getTextContent();
						}
						else if (name.equals("ClassName")){
							clazzName = child.getTextContent();
						}
						else if (name.equals("Semantics")){
							semantics = child.getTextContent();
						}
					}
				}
				this.configs4pIdentifiers.put(identifier, new AggregationProcessConfiguration(identifier,clazzName,semantics));
			}			
		} catch (Exception e) {
			LOGGER.debug("Error while reading config file for aggregation processes: " + e.getLocalizedMessage());
			throw new RuntimeException(e);
		} 
	}
	
	/**
	 * represents a single process configuration
	 * 
	 * @author staschc
	 *
	 */
	private class AggregationProcessConfiguration {
		
		/**identifier of the aggregation process*/
		private String identifier;
		
		/**className of the Aggregation process*/
		private String className;
		
		/** semantics of the process*/
		private String semantics;
		
		/**
		 * constructor 
		 * 
		 * @param id
		 * @param clazz
		 * @param sem
		 */
		public AggregationProcessConfiguration(String id, String clazz, String sem){
			setIdentifier(id);
			setClassName(clazz);
			setSemantics(sem);
		}
		
		/**
		 * @return the identifier
		 */
		@SuppressWarnings("unused")
		public String getIdentifier() {
			return identifier;
		}
		
		/**
		 * @param identifier the identifier to set
		 */
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		/**
		 * @return the className
		 */
		public String getClassName() {
			return className;
		}
		/**
		 * @param className the className to set
		 */
		public void setClassName(String className) {
			this.className = className;
		}
		/**
		 * @return the semantics
		 */
		public String getSemantics() {
			return semantics;
		}
		/**
		 * @param semantics the semantics to set
		 */
		public void setSemantics(String semantics) {
			this.semantics = semantics;
		}
	}
	
	
}
