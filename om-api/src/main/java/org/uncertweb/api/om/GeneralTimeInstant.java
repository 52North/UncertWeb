package org.uncertweb.api.om;

import org.uncertweb.api.om.exceptions.OMParsingException;


/**
 * class represents a general time string as an extension to the ISO:8601 standard allowing times like
 * "every weekday". For more information take a look at {@link https://wiki.aston.ac.uk/foswiki/bin/view/UncertWeb/TemporalEncodings}
 *
 * @author staschc
 *
 */
public class GeneralTimeInstant implements IGeneralTime{

	/**
	 * Seperators used in GeneralTime strings
	 *
	 * @author staschc
	 *
	 */
	abstract class Seperators{
		static final String MONTH_SEP="M";
		static final String DAY_SEP="D";
		static final String HOUR_SEP="h";
		static final String MINUTE_SEP="m";
		static final String SECOND_SEP="s";
	}

	/**
	 *
	 */
	private int month=Integer.MIN_VALUE,day=Integer.MIN_VALUE,hour=Integer.MIN_VALUE,minute=Integer.MIN_VALUE,second=Integer.MIN_VALUE;

	/**
	 * @throws OMParsingException
	 *
	 */
	public GeneralTimeInstant(String timeString) throws OMParsingException{
		boolean valid = false;
		if (timeString.contains(Seperators.MONTH_SEP)){
			int pos = timeString.indexOf(Seperators.MONTH_SEP);
			String monthS = timeString.substring(pos+1, pos+3);
			int month = Integer.parseInt(monthS);
			if (month>=1&&month<=12){
				setMonth(month);
				valid=true;
			}
			else{
				valid=false;
				}
		}
		if (timeString.contains(Seperators.DAY_SEP)){
			int pos = timeString.indexOf(Seperators.DAY_SEP);
			String dayS = timeString.substring(pos+1, pos+2);
			int day = Integer.parseInt(dayS);
			if (day>=1&&day<=7){
				setDay(day);
				valid=true;
			}
			else{
				valid=false;
				}
		}
		if (timeString.contains(Seperators.HOUR_SEP)){
			int pos = timeString.indexOf(Seperators.HOUR_SEP);
			String hoursS = timeString.substring(pos+1, pos+3);
			int hours = Integer.parseInt(hoursS);
			if (hours>=0&&hours<=24){
				setHour(hours);
				valid=true;
			}
			else{
				valid=false;
				}
		}

		if (timeString.contains(Seperators.MINUTE_SEP)){
			int pos = timeString.indexOf(Seperators.MINUTE_SEP);
			String minS = timeString.substring(pos+1, pos+3);
			int min = Integer.parseInt(minS);
			if (min>=0&&min<=59){
				setMinute(min);
				valid=true;
			}
			else{
				valid=false;
				}
		}

		if (timeString.contains(Seperators.SECOND_SEP)){
			int pos = timeString.indexOf(Seperators.SECOND_SEP);
			String secS = timeString.substring(pos+1, pos+3);
			int sec = Integer.parseInt(secS);
			if (sec>=0&&sec<=59){
				setSecond(sec);
				valid=true;
			}
			else{
				valid=false;
				}

		}
		if (!valid){
			throw new OMParsingException("The String of general time is not valid!");
		}
	}

	public String toString(){
		String result = "";

		if (this.getMonth()!=Integer.MIN_VALUE){
			result+=Seperators.MONTH_SEP;
			if (this.getMonth()<10){
				result+=0;
			}
			result=result+this.getMonth();
		}

		if (this.getDay()!=Integer.MIN_VALUE){
			result+=Seperators.DAY_SEP;
			result=result+this.getDay();
		}

		if (this.getHour()!=Integer.MIN_VALUE){
			result+=Seperators.HOUR_SEP;
			if (this.getHour()<10){
				result+=0;
			}
			result=result+this.getHour();
		}
		if (this.getMinute()!=Integer.MIN_VALUE){
			result+=Seperators.MINUTE_SEP;
			if (this.getMinute()<10){
				result+=0;
			}
			result=result+this.getMinute();
		}
		if (this.getSecond()!=Integer.MIN_VALUE){
			result+=Seperators.SECOND_SEP;
			if (this.getSecond()<10){
				result+=0;
			}
			result=result+this.getSecond();
		}
		return result;
	}

	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this.month = month;
	}

	/**
	 * @return the day
	 */
	public int getDay() {
		return day;
	}

	/**
	 * @param day the day to set
	 */
	public void setDay(int day) {
		this.day = day;
	}

	/**
	 * @return the hour
	 */
	public int getHour() {
		return hour;
	}

	/**
	 * @param hour the hour to set
	 */
	public void setHour(int hour) {
		this.hour = hour;
	}

	/**
	 * @return the minute
	 */
	public int getMinute() {
		return minute;
	}

	/**
	 * @param minute the minute to set
	 */
	public void setMinute(int minute) {
		this.minute = minute;
	}

	/**
	 * @return the second
	 */
	public int getSecond() {
		return second;
	}

	/**
	 * @param second the second to set
	 */
	public void setSecond(int second) {
		this.second = second;
	}

}
