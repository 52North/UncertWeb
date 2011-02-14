package org.uncertweb.api.om.converter;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.XBObservationParser;

/**
 * Util class which contains some helper methods which are used in several classes
 * 
 * @author staschc
 *
 */
public class ShapeFileConverterUtil {
	
	/**
	 * parses a timestring which can contain either just one single ISO 8601 String or two comma-seperated time strings
	 *
	 * 
	 * @param phenTime
	 * 			timestring which can contain either just one single ISO 8601 String or two comma-seperated time strings
	 * @return 
	 * 			represents the timeobject which has been parsed
	 * @throws Exception
	 * 			if timestring contains more than two time strings or if phenTime is empty string
	 */
	public static  TimeObject parsePhenTime(String phenTime) throws Exception{
		if (phenTime!=null&&!phenTime.equals("")){
			XBObservationParser xbParser = new XBObservationParser();
			TimeObject to = null;
			String[] times = phenTime.split(",");
			if (times.length==1){
				DateTime timePosition = xbParser.parseTimePosition(times[0]);
				to = new TimeObject(null,timePosition);
				return to;
			}
			else if (times.length==2){
				DateTime beginPosition = xbParser.parseTimePosition(times[0]);
				DateTime endPosition = xbParser.parseTimePosition(times[1]);
				to = new TimeObject(null,new Interval(beginPosition,endPosition));
				return to;
			}
			else {
				throw new Exception("Phenomenon Time can only be time instant or time period!");
			}
		}
		else {
			throw new Exception("PHENTIME property needs to be specified in properties file!!");
		}
		
	}
}
