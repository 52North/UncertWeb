package org.uncertweb.ems.extension.activityprofilemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Class to store diary entries from self-administered activity diaries
 * @author l_gerh01
 *
 */
public class Diary {
	private String id;
	private ArrayList<String> startTime;
	private ArrayList<String> endTime;
	private ArrayList<Map<String,String>> diary;
	private DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm"); //ISODateTimeFormat.dateTime();//
	// using anonymous internal class for initialisation (double brace initialisation)
	private ArrayList<String> attributes = new ArrayList<String>() {{
		add("microenvironment");add("microenvironmentDetail");add("activity");add("noOfPersons");add("smoker");add("windowOpen");}};
	private String startTimeAtt = "starttime";
	private String endTimeAtt = "endtime";
	private String dateAtt = "date";
	private String meAtt = "microenvironment";
	
	public Diary(String id){
		this.id = id;
		diary = new ArrayList<Map<String,String>>();
	}
	
	public void addSingleEntry(int entryID, String key, String value){
		
	}
	
//	public Map<String,String> getEntriesByTime(DateTime start, DateTime end, String io){
//		// find the entry which falls into this period
//		for(int i=0; i<)
//	}
	
	public ArrayList<String> getAttributeNames(){
		return attributes;
	}
	
	public ArrayList<DateTime> getStartTimes(){
		ArrayList<DateTime> timeList = new ArrayList<DateTime>(diary.size());
		for(int i=0; i<diary.size(); i++){
			String date = diary.get(i).get(dateAtt)+" "+diary.get(i).get(startTimeAtt);
			timeList.add(dateFormat.parseDateTime(date));
		}
		return timeList;
	}
	
	public ArrayList<DateTime> getEndTimes(){
		ArrayList<DateTime> timeList = new ArrayList<DateTime>(diary.size());
		for(int i=0; i<diary.size(); i++){
			String date = diary.get(i).get(dateAtt)+" "+diary.get(i).get(endTimeAtt);
			timeList.add(dateFormat.parseDateTime(date));
		}
		return timeList;
	}
	
	/**
	 * converts MEs into IO and returns list for each diary entry
	 * @return
	 */
	public ArrayList<String> getIOList(){
		ArrayList<String> ioList = new ArrayList<String>(diary.size());
		for(int i=0; i<diary.size(); i++){
			String me = diary.get(i).get(meAtt);
			if(me.equals("outdoor")||me.equals("transport"))
				ioList.add("outdoor");
			else
				ioList.add("indoor");
		}
		return ioList;
	}
	
	
//	public Set<String> getAttributes(){
//		return diary.get(0).keySet();
//	}
	
	public String getEntry(int entryID, String key){
		return diary.get(entryID).get(key);
	}
	
	public Map<String,String> getEntries(int entryID){
		return diary.get(entryID);
	}
	
	
	// SETTERS
	
	public void addEntries(String[] keys, String[] values){
		Map<String,String> diaryEntry = new HashMap<String,String>();
    	for (int i=0;i<values.length;i++){
    		diaryEntry.put(keys[i], values[i]);
		}
    	diary.add(diaryEntry);
	}
	
	public int size(){
		return(diary.size());
	}
}
