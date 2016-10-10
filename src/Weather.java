import weather.City;
import weather.GoogleGeocoder;
import weather.OpenWeatherMapReporter;
import weather.WeatherService;
import weather.WeatherService.SERVICE_STATUS;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Weather {
    private static WeatherService weatherService;

    private static void initializeWeatherService() {
        weatherService = new WeatherService();
        weatherService.setGeocoder(new GoogleGeocoder());
        weatherService.setWeatherReporter(new OpenWeatherMapReporter());
    }

    private static Set<String> extractZipCodesFromFile(String fileName) {
        System.out.println("\nReading list of zipcodes from " + fileName);
        try {
            return Files.lines(Paths.get(fileName))
                        .filter(line -> !line.isEmpty())
                        .map(String::trim)
                        .collect(Collectors.toSet());
        } catch (IOException ex) {
            System.out.println(String.format("Error reading %s", fileName));
            return new HashSet<String>();
        }
    }

    private static String promptForFileName() {
        String fileName = "";

        System.out.println("Enter filename (default file will be read if empty): ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            fileName = reader.readLine();
            if(fileName != null)
                if(!fileName.isEmpty())
                    return fileName;
        } catch (IOException e) {
            System.out.println("Error reading input from user. Default file will be read instead");
        }

        return "zipCodes.txt";
    }

    private static void printWarmestAndCoolestCity(List<City> cities) {
        List<City> hottestCity = weatherService.getWarmestCities(cities);
        List<City> coldestCity = weatherService.getCoolestCities(cities);

        String spacer = "\t\t";

        System.out.println("\nHottest City(s):");
        hottestCity.stream()
                   .forEach(city -> System.out.println(city.getCityName() + spacer + city.getStateName() + spacer + city.getZipCode()));

        System.out.println("\nColdest City(s):");
        coldestCity.stream()
                   .forEach(city -> System.out.println(city.getCityName() + spacer + city.getStateName() + spacer + city.getZipCode()));
    }

    private static void printWeatherData(List<City> cities){
        String spacer = "\t\t";

        System.out.println("City" + spacer + "State"  + spacer + "Zip Code" + "\t" + "Min" + spacer + "Max" + "\t" + "Condition");
        cities.stream()
                .forEach(city -> System.out.println(city.toString(spacer)));
    }

    private static void printZIPsWithWeatherErrors(List<City> cities) {
        cities.stream().forEach(city -> System.out.println(city.toString(" ")));
    }

    private static void printZIPsWithGeocodingErrors(List<City> cities) {
        cities.stream().forEach(city -> System.out.println(city.toString(" ")));
    }

    public static void main(String[] args){
        initializeWeatherService();

        System.out.println("\nWelcome! This program will read a list of ZIP codes from a file and find the weather for each ZIP");

        String fileName = promptForFileName();
        Set<String> zipCodes = extractZipCodesFromFile(fileName);

        if(!zipCodes.isEmpty()) {
            Map<SERVICE_STATUS, List<City>> cities = weatherService.getCitiesFromZipCodes(zipCodes);
            Map<SERVICE_STATUS, List<City>> citiesWithWeather = weatherService.getWeatherForCities(cities.get(SERVICE_STATUS.NO_ERROR));

            printWeatherData(citiesWithWeather.get(SERVICE_STATUS.NO_ERROR));
            printWarmestAndCoolestCity(citiesWithWeather.get(SERVICE_STATUS.NO_ERROR));

            if(cities.get(SERVICE_STATUS.ERROR).size() != 0 || citiesWithWeather.get(SERVICE_STATUS.ERROR).size() != 0 ){
                System.out.println("\nThe following errors were reported : ");
                printZIPsWithGeocodingErrors(cities.get(SERVICE_STATUS.ERROR));
                printZIPsWithWeatherErrors(citiesWithWeather.get(SERVICE_STATUS.ERROR));
            }
        }
        else
            System.out.println("Empty file read...");
    }
}
