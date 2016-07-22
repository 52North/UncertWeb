package org.uncertweb.api.om;

import org.uncertweb.api.om.exceptions.OMParsingException;

public class GeneralTimeInterval implements IGeneralTime{
	
	private GeneralTimeInstant start, end;
	private final String INTERVAL_SEP="/";
	
	public GeneralTimeInterval(String timeString) throws OMParsingException{
		if (timeString.contains(INTERVAL_SEP)){
			String[] startNEnd = timeString.split(INTERVAL_SEP);
			setStart(new GeneralTimeInstant(startNEnd[0]));
			setEnd(new GeneralTimeInstant(startNEnd[1]));
		}
	}

	/**
	 * @return the start
	 */
	public GeneralTimeInstant getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(GeneralTimeInstant start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public GeneralTimeInstant getEnd() {
		return end;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(GeneralTimeInstant end) {
		this.end = end;
	}
	
	public String toString(){
		return start.toString()+INTERVAL_SEP+end.toString();
	}

}
