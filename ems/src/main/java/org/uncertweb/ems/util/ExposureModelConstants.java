package org.uncertweb.ems.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

public abstract class ExposureModelConstants {


	public static abstract class ExposureValueTypes {
		public static final String INDOOR_SOURCES= "indoor";
		public static final String OUTDOOR_SOURCES= "outdoor";
		public static final String TOTAL_SOURCES= "total";		
	}
	
	public static abstract class ProcessInputs {
		public static final String INPUT_IDENTIFIER_ACTIVITY_PROFILE = "activityProfile";
		public static final String INPUT_IDENTIFIER_AIR_QUALITY = "airQualityData";
		public static final String INPUT_IDENTIFIER_NUMBER_OF_SAMPLES = "numberOfSamples";
		public static final String INPUT_IDENTIFIER_RESOLUTION = "minuteResolution";
		public static final String INPUT_IDENTIFIER_OUTPUT_UNCERTAINTY = "outputUncertaintyType";
		public static final String OUTPUT_IDENTIFIER = "result";	
		
		public static final Map<String, Class<?>> INPUT_DATA_TYPES = new HashMap<String, Class<?>>(){
			{
				put(INPUT_IDENTIFIER_ACTIVITY_PROFILE, UncertWebIODataBinding.class);
				put(INPUT_IDENTIFIER_AIR_QUALITY, NetCDFBinding.class);
				put(INPUT_IDENTIFIER_NUMBER_OF_SAMPLES,LiteralIntBinding.class);
				put(INPUT_IDENTIFIER_RESOLUTION, LiteralIntBinding.class);
				put(INPUT_IDENTIFIER_OUTPUT_UNCERTAINTY, LiteralStringBinding.class);
				put(OUTPUT_IDENTIFIER, UncertWebIODataBinding.class);			
			}
		};
		
	}
	
	public static ArrayList<String> allowedOutputUncertaintyTypes = new ArrayList<String>(){
		{
			add("http://www.uncertml.org/samples/realisation");
			add("http://www.uncertml.org/samples/continuous-realisation");
			add("http://www.uncertml.org/statistics/mean");
			add("http://www.uncertml.org/statistics/standard-deviation");
			add("http://www.uncertml.org/statistics/variance");	
		}		
	};

	
	
}
