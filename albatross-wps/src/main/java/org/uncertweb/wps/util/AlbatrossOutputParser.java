package org.uncertweb.wps.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.uncertweb.api.om.GeneralTimeInstant;
import org.uncertweb.api.om.exceptions.OMParsingException;

import au.com.bytecode.opencsv.CSVReader;

/**
 * The output of the albatross model consists of a tab separated file including
 * the travel(s) for individuals. Moreover each individual has additional values
 * like gender or age. A single {@link Travel} object has values like
 * end/startime or activity type. This class provides a methods to parse this
 * output file and to deliver a {@link Set} of {@link Individual} with there
 * corresponding {@link Travel}.
 * 
 * @author s_voss13
 * 
 */
public final class AlbatrossOutputParser {

	private AlbatrossOutputParser() {
	}

	/**
	 * Returns a {@link Set} of {@link Individual} from the given file.
	 * 
	 * @param absolutPath
	 *            valid path to the albatross model output file
	 * @return
	 * @throws IOException
	 * @throws OMParsingException
	 */
	public static Set<HouseHold> parse(String absolutPath) throws IOException,
			OMParsingException {

		Set<Individual> individuals = new HashSet<AlbatrossOutputParser.Individual>();
		Set<HouseHold> houseHolds = new HashSet<AlbatrossOutputParser.HouseHold>();

		FileReader fr = null;
		CSVReader reader = null;
		try {
			fr = new FileReader(absolutPath);
			reader = new CSVReader(fr, '\t',
				'\'', 1);
		
		List<String[]> myEntries = reader.readAll();
		

		Individual currentIndividual = null;
		HouseHold currentHouseHold = null;

		String lastHouseHoldId = "";

		for (String[] currentLine : myEntries) {

			// we have a new household
			if (!currentLine[0].equals(lastHouseHoldId)) {
				currentHouseHold = new HouseHold();
			}

			// we have a new individual
			if (currentLine[10].equals("1")) {

				currentIndividual = new Individual(currentLine[4],
						currentLine[7], currentLine[0], currentLine[11]);
				individuals.add(currentIndividual);
			}

			// in rare cases the ppc number is empty, thus we skip the line
			if (currentLine[16].isEmpty())
				continue;

			currentIndividual.addTravel(new Travel(currentLine[15],
					currentLine[10], currentLine[16], currentLine[11],
					currentLine[17], albatrossToGeneralTimeInstant(
							currentLine[13], currentLine[1]),
					albatrossToGeneralTimeInstant(currentLine[14],
							currentLine[1]), currentLine[1]));

			currentHouseHold.addIndividual(currentIndividual);

			lastHouseHoldId = currentLine[0];

			houseHolds.add(currentHouseHold);

		}
		} catch(Exception e){
			throw new RuntimeException("Error while parsing schedules output of Albatross!");
		}
		finally{
			reader.close();
			fr.close();
		}
		

		return Collections.unmodifiableSet(houseHolds);

	}

	/**
	 * Returns the {@link GeneralTimeInstant} for the albatross model time. The
	 * albatross time can have values [0,27] for hours, if a value > 24 appears
	 * the date will be shifted to the next day.
	 * 
	 * @param time
	 * @param sDay
	 * @return
	 * @throws OMParsingException
	 */
	private static GeneralTimeInstant albatrossToGeneralTimeInstant(
			String time, String sDay) throws OMParsingException {

		int day = Integer.valueOf(sDay);

		if (time.length() == 3) {
			time = "0" + time;
		}

		int hours = Integer.valueOf(time.substring(0, 2));

		// this can be only the case for the end time
		// we don not use % because 1. values > 27 are not allowed 2. this would
		// make it so generic that interval > 7 days would work and no one could
		// distinguish between a whole week or n weeks (in case of 1234...
		// hours)
		if (hours > 24) {

			hours = hours - 24;
			day++;

			// but what happens if the day changes to 8? it is the following
			// monday...
			if (day > 7) {

				day = day - 7;
			}
		}

		if (hours < 10) {

			time = "0" + hours + time.substring(2, 4);
		} else {

			time = hours + time.substring(2, 4);
		}

		return new GeneralTimeInstant("D" + day + "h" + time.substring(0, 2)
				+ "m" + time.substring(2, 4));

	}
	
	public static class HouseHold {

		private Set<Individual> individuals = new HashSet<AlbatrossOutputParser.Individual>();

		public Set<Individual> getIndividuals() {
			return Collections.unmodifiableSet(individuals);
		}

		public void addIndividual(Individual individual) {

			individuals.add(individual);
		}

	}

	/**
	 * 
	 * @author s_voss13
	 * 
	 */
	public static class Individual {

		public Individual(String age, String gender, String houseHoldID,
				String workstatus) {

			this.age = age;
			this.gender = gender;
			this.houseHoldID = houseHoldID;
			this.workstatus = workstatus;
		}

		private String age;
		private String gender;
		private String houseHoldID;

		/**
		 * 0:Work, 1: Business, 2: Bring/Get goods and persons, 3: shop from one
		 * store, 4: shop from multiple store, 5: service, 6: social, 7:
		 * leisure, 8;tour, 9:at home
		 */
		private String workstatus;

		private Set<Travel> travel = new HashSet<AlbatrossOutputParser.Travel>();

		public void addTravel(Travel travel) {

			this.travel.add(travel);
		}

		public Set<Travel> getTravel() {

			return Collections.unmodifiableSet(this.travel);
		}

		public String getAge() {
			return age;
		}

		public String getGender() {
			return gender;
		}

		public String getHouseHoldID() {
			return houseHoldID;
		}

		/**
		 * 0:Work, 1: Business, 2: Bring/Get goods and persons, 3: shop from one
		 * store, 4: shop from multiple store, 5: service, 6: social, 7:
		 * leisure, 8;tour, 9:at home
		 */
		public String getWorkstatus() {
			return workstatus;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Individual [age=").append(age).append(", gender=")
					.append(gender).append(", houseHoldID=")
					.append(houseHoldID).append(", workstatus=")
					.append(workstatus).append("]");
			return builder.toString();
		}

	}

	/**
	 * 
	 * @author s_voss13
	 * 
	 */
	public static class Travel {

		public Travel(String isHome, String actionnumber, String ppc,
				String activityType, String travelMode,
				GeneralTimeInstant beginTime, GeneralTimeInstant endTime,
				String day) {

			this.isHome = isHome;
			this.actionnumber = actionnumber;
			this.ppc = ppc;
			this.activityType = activityType;
			this.travelMode = travelMode;
			this.beginTime = beginTime;
			this.endTime = endTime;
			this.day = day;
		}

		private String isHome;
		private String actionnumber;
		private String ppc;
		private String activityType;

		/**
		 * yes, 0: car driver, 1: slow(bike or walk), 2: public, 3: car
		 * passenger
		 */
		private String travelMode;
		private GeneralTimeInstant beginTime;
		private GeneralTimeInstant endTime;
		private String day;

		public String isHome() {
			return isHome;
		}

		public String getActionnumber() {
			return actionnumber;
		}

		public String getPpc() {
			return ppc;
		}

		public String getActivityType() {
			return activityType;
		}

		/**
		 * yes, 0: car driver, 1: slow(bike or walk), 2: public, 3: car
		 * passenger
		 */
		public String getTravelMode() {
			return travelMode;
		}

		public GeneralTimeInstant getBeginTime() {
			return beginTime;
		}

		public GeneralTimeInstant getEndTime() {
			return endTime;
		}

		public String getDay() {
			return day;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Travel [isHome=").append(isHome)
					.append(", actionnumber=").append(actionnumber)
					.append(", ppc=").append(ppc).append(", activityType=")
					.append(activityType).append(", travelMode=")
					.append(travelMode).append(", beginTime=")
					.append(beginTime).append(", endTime=").append(endTime)
					.append(", day=").append(day).append("]");
			return builder.toString();
		}

	}

}
