package de.ifgi.airbase.feeder.data;

import org.joda.time.Period;

/**
 * @author Christian Autermann
 */
public enum EEAMeasurementType {
	/***/
	THREE_HOURS, // undocumented...
	/***/
	HOUR,
	/***/
	EIGHT_HOURS,
	/***/
	DAY,
	/***/
	DAY_MAX,
	/***/
	WEEK,
	/***/
	TWO_WEEKS,
	/***/
	FOUR_WEEKS,
	/***/
	MONTH,
	/***/
	THREE_MONTH,
	/***/
	YEAR,
	/***/
	VAR;

	/**
	 * @param s
	 *            the string representation
	 * @return the {@link EEAMeasurementType}
	 */
	public static EEAMeasurementType getValue(String s) {
		if (s.equals("hour"))
			return HOUR;
		else if (s.equals("3hour"))
			return THREE_HOURS;
		else if (s.equals("hour8"))
			return EIGHT_HOURS;
		else if (s.equals("day"))
			return DAY;
		else if (s.equals("dymax"))
			return DAY_MAX;
		else if (s.equals("week"))
			return WEEK;
		else if (s.equals("2week"))
			return TWO_WEEKS;
		else if (s.equals("4week"))
			return FOUR_WEEKS;
		else if (s.equals("month"))
			return MONTH;
		else if (s.equals("3month"))
			return THREE_MONTH;
		else if (s.equals("year"))
			return YEAR;
		else if (s.equals("var"))
			return VAR;
		else
			throw new Error("Unknown DataType: " + s);
	}

	public Period getPeriod() {
		switch (this) {
		case HOUR:
			return Period.hours(1);
		case THREE_HOURS:
			return Period.hours(3);
		case EIGHT_HOURS:
			return Period.hours(8);
		case DAY:
		case DAY_MAX:
			return Period.days(1);
		case WEEK:
			return Period.weeks(1);
		case TWO_WEEKS:
			return Period.weeks(2);
		case FOUR_WEEKS:
			return Period.weeks(4);
		case MONTH:
			return Period.months(1);
		case THREE_MONTH:
			return Period.months(3);
		case YEAR:
			return Period.years(1);
		case VAR:
		default:
			throw new Error("Period not known");
		}
	}
}