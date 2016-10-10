package weather;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class WeatherSummary {
    final private String weatherConditions;
    final private int highestTemperature;
    final private int lowestTemperature;

    public WeatherSummary(String conditions, int highTemp, int lowTemp) {
        weatherConditions = conditions;
        highestTemperature = highTemp;
        lowestTemperature = lowTemp;
    }

    public int getLowestTemperature() {
        return lowestTemperature;
    }

    public int getHighestTemperature() {
        return highestTemperature;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String toString(String delimiter){
        return lowestTemperature + delimiter + highestTemperature + delimiter + weatherConditions;
    }
}
