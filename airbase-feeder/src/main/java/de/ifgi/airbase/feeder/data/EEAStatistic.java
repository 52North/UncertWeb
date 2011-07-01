package de.ifgi.airbase.feeder.data;

/**
 * @author Christian Autermann
 */
public class EEAStatistic {
	private String stationEuropeanCode;
	private int componentCode;
	private String componentName;
	private String componentCaption;
	private String measurementUnit;
	private String measurementEuropeanGroupCode;
	private String statisticsPeriod;
	private int statisticsYear;
	private String statisticsAverageGroup;
	private String statisticShortname;
	private String statisticName;
	private float statisticValue;
	private float statisticsPercentageValid;
	private int statisticsNumberValid;
	private boolean statisticsCalculated;

	/**
	 * @return the stationEuropeanCode
	 */
	public String getStationEuropeanCode() {
		return this.stationEuropeanCode;
	}

	/**
	 * @param stationEuropeanCode
	 *            the stationEuropeanCode to set
	 */
	public void setStationEuropeanCode(String stationEuropeanCode) {
		this.stationEuropeanCode = stationEuropeanCode;
	}

	/**
	 * @return the componentCode
	 */
	public int getComponentCode() {
		return this.componentCode;
	}

	/**
	 * @param componentCode
	 *            the componentCode to set
	 */
	public void setComponentCode(int componentCode) {
		this.componentCode = componentCode;
	}

	/**
	 * @return the componentName
	 */
	public String getComponentName() {
		return this.componentName;
	}

	/**
	 * @param componentName
	 *            the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	/**
	 * @return the componentCaption
	 */
	public String getComponentCaption() {
		return this.componentCaption;
	}

	/**
	 * @param componentCaption
	 *            the componentCaption to set
	 */
	public void setComponentCaption(String componentCaption) {
		this.componentCaption = componentCaption;
	}

	/**
	 * @return the measurementUnit
	 */
	public String getMeasurementUnit() {
		return this.measurementUnit;
	}

	/**
	 * @param measurementUnit
	 *            the measurementUnit to set
	 */
	public void setMeasurementUnit(String measurementUnit) {
		this.measurementUnit = measurementUnit;
	}

	/**
	 * @return the measurementEuropeanGroupCode
	 */
	public String getMeasurementEuropeanGroupCode() {
		return this.measurementEuropeanGroupCode;
	}

	/**
	 * @param measurementEuropeanGroupCode
	 *            the measurementEuropeanGroupCode to set
	 */
	public void setMeasurementEuropeanGroupCode(
			String measurementEuropeanGroupCode) {
		this.measurementEuropeanGroupCode = measurementEuropeanGroupCode;
	}

	/**
	 * @return the statisticsPeriod
	 */
	public String getStatisticsPeriod() {
		return this.statisticsPeriod;
	}

	/**
	 * @param statisticsPeriod
	 *            the statisticsPeriod to set
	 */
	public void setStatisticsPeriod(String statisticsPeriod) {
		this.statisticsPeriod = statisticsPeriod;
	}

	/**
	 * @return the statisticsYear
	 */
	public int getStatisticsYear() {
		return this.statisticsYear;
	}

	/**
	 * @param statisticsYear
	 *            the statisticsYear to set
	 */
	public void setStatisticsYear(int statisticsYear) {
		this.statisticsYear = statisticsYear;
	}

	/**
	 * @return the statisticsAverageGroup
	 */
	public String getStatisticsAverageGroup() {
		return this.statisticsAverageGroup;
	}

	/**
	 * @param statisticsAverageGroup
	 *            the statisticsAverageGroup to set
	 */
	public void setStatisticsAverageGroup(String statisticsAverageGroup) {
		this.statisticsAverageGroup = statisticsAverageGroup;
	}

	/**
	 * @return the statisticShortname
	 */
	public String getStatisticShortname() {
		return this.statisticShortname;
	}

	/**
	 * @param statisticShortname
	 *            the statisticShortname to set
	 */
	public void setStatisticShortname(String statisticShortname) {
		this.statisticShortname = statisticShortname;
	}

	/**
	 * @return the statisticName
	 */
	public String getStatisticName() {
		return this.statisticName;
	}

	/**
	 * @param statisticName
	 *            the statisticName to set
	 */
	public void setStatisticName(String statisticName) {
		this.statisticName = statisticName;
	}

	/**
	 * @return the statisticValue
	 */
	public float getStatisticValue() {
		return this.statisticValue;
	}

	/**
	 * @param statisticValue
	 *            the statisticValue to set
	 */
	public void setStatisticValue(float statisticValue) {
		this.statisticValue = statisticValue;
	}

	/**
	 * @return the statisticsPercentageValid
	 */
	public float getStatisticsPercentageValid() {
		return this.statisticsPercentageValid;
	}

	/**
	 * @param statisticsPercentageValid
	 *            the statisticsPercentageValid to set
	 */
	public void setStatisticsPercentageValid(float statisticsPercentageValid) {
		this.statisticsPercentageValid = statisticsPercentageValid;
	}

	/**
	 * @return the statisticsNumberValid
	 */
	public int getStatisticsNumberValid() {
		return this.statisticsNumberValid;
	}

	/**
	 * @param statisticsNumberValid
	 *            the statisticsNumberValid to set
	 */
	public void setStatisticsNumberValid(int statisticsNumberValid) {
		this.statisticsNumberValid = statisticsNumberValid;
	}

	/**
	 * @return the statisticsCalculated
	 */
	public boolean isStatisticsCalculated() {
		return this.statisticsCalculated;
	}

	/**
	 * @param statisticsCalculated
	 *            the statisticsCalculated to set
	 */
	public void setStatisticsCalculated(boolean statisticsCalculated) {
		this.statisticsCalculated = statisticsCalculated;
	}

}
