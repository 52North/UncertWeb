
package de.ifgi.airbase.feeder.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.ifgi.airbase.feeder.data.EEAConfiguration.EEAComponent;

/**
 * @author Christian Autermann
 */
public class EEAStation {

    private static class Pair<U, V> {
        U one;
        V two;

        Pair(U u, V v) {
            this.one = u;
            this.two = v;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( (this.one == null) ? 0 : this.one.hashCode());
            result = prime * result + ( (this.two == null) ? 0 : this.two.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            return this.hashCode() == obj.hashCode();
        }
    }

    private String europeanCode;
    private String localCode;
    private String countryIsoCode;
    private String countryName;
    private String name;
    private String startDate;
    private String endDate;
    private String type;
    private String ozoneClassification;
    private String typeOfArea;
    private String subcatRuralBack;
    private String streetType;
    private String longitudeDeg;
    private String latitudeDeg;
    private String altitude;
    private String city;
    private String lauLevel1Code;
    private String lauLevel2Code;
    private String lauLevel2Name;
    private String EMEPStation;

    private HashMap<Pair<Integer, Integer>, EEAConfiguration> configurations = new HashMap<Pair<Integer, Integer>, EEAConfiguration>();

    private Set<Integer> componentIds = new HashSet<Integer>();

    /**
     * @return the component codes
     */
    public Set<Integer> getComponentCodes() {
        return this.componentIds;
    }

    /**
     * @param config
     *        the {@link EEAConfiguration} to add
     */
    public void addConfiguration(EEAConfiguration config) {
        this.componentIds.add(Integer.valueOf(config.getComponentCode()));
        this.configurations.put(new Pair<Integer, Integer>(Integer.valueOf(config.getComponentCode()),
                                                           Integer.valueOf(config.getMeasurementEuropeanGroupCode())),
                                config);
    }

    /**
     * @param component
     *        the {@link EEAComponent}
     * @param groupCode
     *        the group code
     * @return the {@link EEAConfiguration}
     */
    public EEAConfiguration getConfigurationsByComponentAndMeasurment(int component, int groupCode) {
        Pair<Integer, Integer> pair = new Pair<Integer, Integer>(Integer.valueOf(component), Integer.valueOf(groupCode));
        EEAConfiguration config = this.configurations.get(pair);
        return config;
    }

    /**
     * @return the {@link EEAConfiguration}s
     */
    public Collection<EEAConfiguration> getConfigurations() {
        return this.configurations.values();
    }

    /**
     * @return the europeanCode
     */
    public String getEuropeanCode() {
        return this.europeanCode;
    }

    /**
     * @param europeanCode
     *        the europeanCode to set
     */
    public void setEuropeanCode(String europeanCode) {
        this.europeanCode = europeanCode;
    }

    /**
     * @return the localCode
     */
    public String getLocalCode() {
        return this.localCode;
    }

    /**
     * @param localCode
     *        the localCode to set
     */
    public void setLocalCode(String localCode) {
        this.localCode = localCode;
    }

    /**
     * @return the countryIsoCode
     */
    public String getCountryIsoCode() {
        return this.countryIsoCode;
    }

    /**
     * @param countryIsoCode
     *        the countryIsoCode to set
     */
    public void setCountryIsoCode(String countryIsoCode) {
        this.countryIsoCode = countryIsoCode;
    }

    /**
     * @return the countryName
     */
    public String getCountryName() {
        return this.countryName;
    }

    /**
     * @param countryName
     *        the countryName to set
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *        the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the startDate
     */
    public String getStartDate() {
        return this.startDate;
    }

    /**
     * @param startDate
     *        the startDate to set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public String getEndDate() {
        return this.endDate;
    }

    /**
     * @param endDate
     *        the endDate to set
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * @param type
     *        the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the ozoneClassification
     */
    public String getOzoneClassification() {
        return this.ozoneClassification;
    }

    /**
     * @param ozoneClassification
     *        the ozoneClassification to set
     */
    public void setOzoneClassification(String ozoneClassification) {
        this.ozoneClassification = ozoneClassification;
    }

    /**
     * @return the typeOfArea
     */
    public String getTypeOfArea() {
        return this.typeOfArea;
    }

    /**
     * @param typeOfArea
     *        the typeOfArea to set
     */
    public void setTypeOfArea(String typeOfArea) {
        this.typeOfArea = typeOfArea;
    }

    /**
     * @return the subcatRuralBack
     */
    public String getSubcatRuralBack() {
        return this.subcatRuralBack;
    }

    /**
     * @param subcatRuralBack
     *        the subcatRuralBack to set
     */
    public void setSubcatRuralBack(String subcatRuralBack) {
        this.subcatRuralBack = subcatRuralBack;
    }

    /**
     * @return the streetType
     */
    public String getStreetType() {
        return this.streetType;
    }

    /**
     * @param streetType
     *        the streetType to set
     */
    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }

    /**
     * @return the longitudeDeg
     */
    public String getLongitude() {
        return this.longitudeDeg;
    }

    /**
     * @param longitudeDeg
     *        the longitudeDeg to set
     */
    public void setLongitudeDeg(String longitudeDeg) {
        this.longitudeDeg = longitudeDeg;
    }

    /**
     * @return the latitudeDeg
     */
    public String getLatitude() {
        return this.latitudeDeg;
    }

    /**
     * @param latitudeDeg
     *        the latitudeDeg to set
     */
    public void setLatitudeDeg(String latitudeDeg) {
        this.latitudeDeg = latitudeDeg;
    }

    /**
     * @return the altitude
     */
    public String getAltitude() {
        return this.altitude;
    }

    /**
     * @param altitude
     *        the altitude to set
     */
    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return this.city;
    }

    /**
     * @param city
     *        the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the lauLevel1Code
     */
    public String getLauLevel1Code() {
        return this.lauLevel1Code;
    }

    /**
     * @param lauLevel1Code
     *        the lauLevel1Code to set
     */
    public void setLauLevel1Code(String lauLevel1Code) {
        this.lauLevel1Code = lauLevel1Code;
    }

    /**
     * @return the lauLevel2Code
     */
    public String getLauLevel2Code() {
        return this.lauLevel2Code;
    }

    /**
     * @param lauLevel2Code
     *        the lauLevel2Code to set
     */
    public void setLauLevel2Code(String lauLevel2Code) {
        this.lauLevel2Code = lauLevel2Code;
    }

    /**
     * @return the lauLevel2Name
     */
    public String getLauLevel2Name() {
        return this.lauLevel2Name;
    }

    /**
     * @param lauLevel2Name
     *        the lauLevel2Name to set
     */
    public void setLauLevel2Name(String lauLevel2Name) {
        this.lauLevel2Name = lauLevel2Name;
    }

    /**
     * @return the eMEPStation
     */
    public String getEMEPStation() {
        return this.EMEPStation;
    }

    /**
     * @param eMEPStation
     *        the eMEPStation to set
     */
    public void setEMEPStation(String eMEPStation) {
        this.EMEPStation = eMEPStation;
    }

}
