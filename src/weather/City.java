package weather;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.tuple.Pair;

public class City {

    final private String zipCode;
    final private String cityName;
    final private String stateName;

    final private String error;

    private WeatherSummary weatherSummary;


    public City(String zip, String city, String state, WeatherSummary summary) {
        zipCode = zip;
        cityName = city;
        stateName = state;
        weatherSummary = summary;

        error = null;
    }

    public City(String zip, String city, String state){
        zipCode = zip;
        cityName = city;
        stateName = state;
        error = null;
    }

    public City(String zip, Pair<String, String> CityStateNames){
        zipCode = zip;
        cityName = CityStateNames.getLeft();
        stateName = CityStateNames.getRight();
        error = null;
    }

    public City(String zip, String err){
        zipCode = zip;
        cityName = "";
        stateName = "";
        error = err;
    }

    public String getCityName() {
        return cityName;
    }

    public String getStateName() {
        return stateName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setWeatherSummary(WeatherSummary summary) {
        weatherSummary = summary;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public WeatherSummary getWeatherSummary() {
        return weatherSummary;
    }

    public String getError(){
        return error;
    }

    public String toString(String delimiter){
        if (error == null) {
            return cityName + delimiter + stateName + delimiter + zipCode + delimiter + weatherSummary.toString(delimiter);
        }
        return error;
    }

}
