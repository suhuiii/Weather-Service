package weather;

import java.util.*;
import java.util.function.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

public class WeatherService {
    private Geocoder geocoder;
    private WeatherReporter weatherReporter;

    public enum SERVICE_STATUS{ERROR, NO_ERROR};


    public Map<SERVICE_STATUS, List<City>> getCitiesFromZipCodes(Set<String> zipCodes){

        if(geocoder == null)
            throw new NullPointerException("Geocoding Service not set...");

        Function<City, SERVICE_STATUS> errorOrNot = city -> city.getError() == null ? SERVICE_STATUS.NO_ERROR : SERVICE_STATUS.ERROR;

        Map<SERVICE_STATUS, List<City>> result = 
          zipCodes.stream()
                  .map(zip -> getCityStateNames(zip))
                  .collect(groupingBy(errorOrNot, toList()));

        Arrays.stream(SERVICE_STATUS.values())
              .forEach(status -> result.putIfAbsent(status, new ArrayList<>()));

        return result;
    }

    private City getCityStateNames(String zip){
        try {
            return new City(zip, geocoder.getCityStateForZIP(zip));
        }catch(Exception ex){
            return new City(zip, String.format("Geocoding Error for ZIP %s : %s", zip, ex.getMessage()));
        }
    }

    public List<City> sortCities(List<City> unsortedcities){

        return unsortedcities.stream()
                             .sorted(comparing(City::getCityName)
                               .thenComparing(City::getStateName)
                               .thenComparing(City::getZipCode))
                             .collect(toList());
    }

    public void setGeocoder(Geocoder geocodingService){
        geocoder = geocodingService;
    }

    public void setWeatherReporter(WeatherReporter weatherReporterService){
        weatherReporter = weatherReporterService;
    }

    public Map<SERVICE_STATUS,List<City>> getWeatherForCities(List<City> cities) {
        if(weatherReporter == null)
            throw new NullPointerException("WeatherReporting Service not set...");

        Function<City, SERVICE_STATUS> errorOrNot = city -> city.getError() == null ? SERVICE_STATUS.NO_ERROR : SERVICE_STATUS.ERROR;

        Map<SERVICE_STATUS, List<City>> result = 
          cities.stream()
                .map(city -> getWeatherForCity(city))
                .collect(groupingBy(errorOrNot, toList()));

        if(result.get(SERVICE_STATUS.NO_ERROR) == null)
            result.put(SERVICE_STATUS.NO_ERROR, new ArrayList<>());
        if(result.get(SERVICE_STATUS.ERROR) == null)
            result.put(SERVICE_STATUS.ERROR, new ArrayList<>());

        return result;
    }

    private City getWeatherForCity(City city){
        try {
            city.setWeatherSummary(weatherReporter.getWeatherReportForCity(city.getZipCode()));
            return city;
        }catch(Exception ex){
            return new City(city.getZipCode(), String.format("WeatherReporting Error for ZIP %s : %s", city.getZipCode(), ex.getMessage()));
        }
    }
    
    public List<City> getWarmestCities(List<City> cities){
        int highestTemp = cities.stream()
                                .mapToInt(city->city.getWeatherSummary().getHighestTemperature())
                                .max()
                                .getAsInt();

        return cities.stream()
                     .filter(city -> city.getWeatherSummary().getHighestTemperature() == highestTemp)
                     .collect(toList());

    }

    public List<City> getCoolestCities(List<City> cities){
        int lowestTemp = cities.stream()
                               .mapToInt(city->city.getWeatherSummary().getLowestTemperature())
                               .min()
                               .getAsInt();

        return cities.stream()
                     .filter(city -> city.getWeatherSummary().getLowestTemperature() == lowestTemp)
                     .collect(toList());
    }

}
