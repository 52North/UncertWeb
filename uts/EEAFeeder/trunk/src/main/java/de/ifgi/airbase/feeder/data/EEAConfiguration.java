package de.ifgi.airbase.feeder.data;

/**
 * @author Christian Autermann
 */
public class EEAConfiguration {

	/**
	 * @author Christian Autermann
	 */
	public static class EEAComponent {

		private String name, caption;
		private int code;

		/**
		 * @param name the name
		 * @param caption the caption
		 * @param code the code
		 */
		public EEAComponent(String name, String caption, int code) {
			this.name = name;
			this.code = code;
			this.caption = caption;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * @param caption
		 *            the caption to set
		 */
		public void setCaption(String caption) {
			this.caption = caption;
		}

		/**
		 * @return the caption
		 */
		public String getCaption() {
			return this.caption;
		}

		/**
		 * @param code
		 *            the code to set
		 */
		public void setCode(int code) {
			this.code = code;
		}

		/**
		 * @return the code
		 */
		public int getCode() {
			return this.code;
		}

		@Override
		public int hashCode() {
			return this.code;
		}
	}
	private String componentCaption;
	private String componentName;
	private EEAStation station;
	private boolean componentFwd;
	private int componentCode;
	private int measurementEuropeanGroupCode;
	private String measurementUnit;
	private String measurementGroupStartDate;
	private String measurementGroupEndDate;
	private String measurementLatestAIRBASE;
	private String measurementEuropeanCode;
	private String measurementStartDate;
	private String measurementEndDate;
	private String measurementAutomatic;
	private String measurementTechniquePrinciple;
	private String measurementEquipment;
	private String integrationTimeFrequency;
	private String calibrationUnit;
	private String heightSamplingPoint;
	private String lengthSamplingLine;
	private String locationSamplingPoint;
	private String samplingTime;
	private String samplingTimeUnit;
	private String calibrationFrequency;
	private String integrationTimeUnit;
	private String calibrationMethod;
	private String calibrationDescription;
	private String stationCode;

	/**
	 * @return the componentFwd
	 */
	public boolean isComponentFwd() {
		return this.componentFwd;
	}

	/**
	 * @param componentFwd
	 *            the componentFwd to set
	 */
	public void setComponentFwd(boolean componentFwd) {
		this.componentFwd = componentFwd;
	}

	/**
	 * @return the measurementEuropeanGroupCode
	 */
	public int getMeasurementEuropeanGroupCode() {
		return this.measurementEuropeanGroupCode;
	}

	/**
	 * @param measurementEuropeanGroupCode
	 *            the measurementEuropeanGroupCode to set
	 */
	public void setMeasurementEuropeanGroupCode(int measurementEuropeanGroupCode) {
		this.measurementEuropeanGroupCode = measurementEuropeanGroupCode;
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
	 * @return the measurementGroupStartDate
	 */
	public String getMeasurementGroupStartDate() {
		return this.measurementGroupStartDate;
	}

	/**
	 * @param measurementGroupStartDate
	 *            the measurementGroupStartDate to set
	 */
	public void setMeasurementGroupStartDate(String measurementGroupStartDate) {
		this.measurementGroupStartDate = measurementGroupStartDate;
	}

	/**
	 * @return the measurementGroupEndDate
	 */
	public String getMeasurementGroupEndDate() {
		return this.measurementGroupEndDate;
	}

	/**
	 * @param measurementGroupEndDate
	 *            the measurementGroupEndDate to set
	 */
	public void setMeasurementGroupEndDate(String measurementGroupEndDate) {
		this.measurementGroupEndDate = measurementGroupEndDate;
	}

	/**
	 * @return the measurementLatestAIRBASE
	 */
	public String getMeasurementLatestAIRBASE() {
		return this.measurementLatestAIRBASE;
	}

	/**
	 * @param measurementLatestAIRBASE
	 *            the measurementLatestAIRBASE to set
	 */
	public void setMeasurementLatestAIRBASE(String measurementLatestAIRBASE) {
		this.measurementLatestAIRBASE = measurementLatestAIRBASE;
	}

	/**
	 * @return the measurementEuropeanCode
	 */
	public String getMeasurementEuropeanCode() {
		return this.measurementEuropeanCode;
	}

	/**
	 * @param measurementEuropeanCode
	 *            the measurementEuropeanCode to set
	 */
	public void setMeasurementEuropeanCode(String measurementEuropeanCode) {
		this.measurementEuropeanCode = measurementEuropeanCode;
	}

	/**
	 * @return the measurementStartDate
	 */
	public String getMeasurementStartDate() {
		return this.measurementStartDate;
	}

	/**
	 * @param measurementStartDate
	 *            the measurementStartDate to set
	 */
	public void setMeasurementStartDate(String measurementStartDate) {
		this.measurementStartDate = measurementStartDate;
	}

	/**
	 * @return the measurementEndDate
	 */
	public String getMeasurementEndDate() {
		return this.measurementEndDate;
	}

	/**
	 * @param measurementEndDate
	 *            the measurementEndDate to set
	 */
	public void setMeasurementEndDate(String measurementEndDate) {
		this.measurementEndDate = measurementEndDate;
	}

	/**
	 * @return the measurementAutomatic
	 */
	public String getMeasurementAutomatic() {
		return this.measurementAutomatic;
	}

	/**
	 * @param measurementAutomatic
	 *            the measurementAutomatic to set
	 */
	public void setMeasurementAutomatic(String measurementAutomatic) {
		this.measurementAutomatic = measurementAutomatic;
	}

	/**
	 * @return the measurementTechniquePrinciple
	 */
	public String getMeasurementTechniquePrinciple() {
		return this.measurementTechniquePrinciple;
	}

	/**
	 * @param measurementTechniquePrinciple
	 *            the measurementTechniquePrinciple to set
	 */
	public void setMeasurementTechniquePrinciple(
			String measurementTechniquePrinciple) {
		this.measurementTechniquePrinciple = measurementTechniquePrinciple;
	}

	/**
	 * @return the measurementEquipment
	 */
	public String getMeasurementEquipment() {
		return this.measurementEquipment;
	}

	/**
	 * @param measurementEquipment
	 *            the measurementEquipment to set
	 */
	public void setMeasurementEquipment(String measurementEquipment) {
		this.measurementEquipment = measurementEquipment;
	}

	/**
	 * @return the integrationTimeFrequency
	 */
	public String getIntegrationTimeFrequency() {
		return this.integrationTimeFrequency;
	}

	/**
	 * @param integrationTimeFrequency
	 *            the integrationTimeFrequency to set
	 */
	public void setIntegrationTimeFrequency(String integrationTimeFrequency) {
		this.integrationTimeFrequency = integrationTimeFrequency;
	}

	/**
	 * @return the calibrationUnit
	 */
	public String getCalibrationUnit() {
		return this.calibrationUnit;
	}

	/**
	 * @param calibrationUnit
	 *            the calibrationUnit to set
	 */
	public void setCalibrationUnit(String calibrationUnit) {
		this.calibrationUnit = calibrationUnit;
	}

	/**
	 * @return the heightSamplingPoint
	 */
	public String getHeightSamplingPoint() {
		return this.heightSamplingPoint;
	}

	/**
	 * @param heightSamplingPoint
	 *            the heightSamplingPoint to set
	 */
	public void setHeightSamplingPoint(String heightSamplingPoint) {
		this.heightSamplingPoint = heightSamplingPoint;
	}

	/**
	 * @return the lengthSamplingLine
	 */
	public String getLengthSamplingLine() {
		return this.lengthSamplingLine;
	}

	/**
	 * @param lengthSamplingLine
	 *            the lengthSamplingLine to set
	 */
	public void setLengthSamplingLine(String lengthSamplingLine) {
		this.lengthSamplingLine = lengthSamplingLine;
	}

	/**
	 * @return the locationSamplingPoint
	 */
	public String getLocationSamplingPoint() {
		return this.locationSamplingPoint;
	}

	/**
	 * @param locationSamplingPoint
	 *            the locationSamplingPoint to set
	 */
	public void setLocationSamplingPoint(String locationSamplingPoint) {
		this.locationSamplingPoint = locationSamplingPoint;
	}

	/**
	 * @return the samplingTime
	 */
	public String getSamplingTime() {
		return this.samplingTime;
	}

	/**
	 * @param samplingTime
	 *            the samplingTime to set
	 */
	public void setSamplingTime(String samplingTime) {
		this.samplingTime = samplingTime;
	}

	/**
	 * @return the samplingTimeUnit
	 */
	public String getSamplingTimeUnit() {
		return this.samplingTimeUnit;
	}

	/**
	 * @param samplingTimeUnit
	 *            the samplingTimeUnit to set
	 */
	public void setSamplingTimeUnit(String samplingTimeUnit) {
		this.samplingTimeUnit = samplingTimeUnit;
	}

	/**
	 * @return the calibration_frequency
	 */
	public String getCalibration_frequency() {
		return this.calibrationFrequency;
	}

	/**
	 * @param calibrationFrequency
	 *            the calibration_frequency to set
	 */
	public void setCalibrationFrequency(String calibrationFrequency) {
		this.calibrationFrequency = calibrationFrequency;
	}

	/**
	 * @return the integrationTimeUnit
	 */
	public String getIntegrationTimeUnit() {
		return this.integrationTimeUnit;
	}

	/**
	 * @param integrationTimeUnit
	 *            the integrationTimeUnit to set
	 */
	public void setIntegrationTimeUnit(String integrationTimeUnit) {
		this.integrationTimeUnit = integrationTimeUnit;
	}

	/**
	 * @return the calibrationMethod
	 */
	public String getCalibrationMethod() {
		return this.calibrationMethod;
	}

	/**
	 * @param calibrationMethod
	 *            the calibrationMethod to set
	 */
	public void setCalibrationMethod(String calibrationMethod) {
		this.calibrationMethod = calibrationMethod;
	}

	/**
	 * @return the calibrationDescription
	 */
	public String getCalibrationDescription() {
		return this.calibrationDescription;
	}

	/**
	 * @param calibrationDescription
	 *            the calibrationDescription to set
	 */
	public void setCalibrationDescription(String calibrationDescription) {
		this.calibrationDescription = calibrationDescription;
	}

	/**
	 * @param station
	 *            the station to set
	 */
	public void setStation(EEAStation station) {
		this.station = station;
	}

	/**
	 * @return the station
	 */
	public EEAStation getStation() {
		return this.station;
	}

	/**
	 * @param componentCaption
	 *            the componentCaption to set
	 */
	public void setComponentCaption(String componentCaption) {
		this.componentCaption = componentCaption;
	}

	/**
	 * @return the componentCaption
	 */
	public String getComponentCaption() {
		return this.componentCaption;
	}

	/**
	 * @param componentName
	 *            the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	/**
	 * @return the componentName
	 */
	public String getComponentName() {
		return this.componentName;
	}

	/**
	 * @param componentCode
	 *            the componentCode to set
	 */
	public void setComponentCode(int componentCode) {
		this.componentCode = componentCode;
	}

	/**
	 * @return the componentCode
	 */
	public int getComponentCode() {
		return this.componentCode;
	}

	/**
	 * @param stationCode
	 *            the stationCode to set
	 */
	public void setStationCode(String stationCode) {
		this.stationCode = stationCode;
	}

	/**
	 * @return the stationCode
	 */
	public String getStationCode() {
		return this.stationCode;
	}

	/**
	 * @return the component
	 */
	public EEAComponent getComponent() {
		return new EEAComponent(getComponentName(), getComponentCaption(),
				getComponentCode());
	}
}
