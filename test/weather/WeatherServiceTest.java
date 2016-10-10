package weather;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import java.util.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import weather.WeatherService.SERVICE_STATUS;

public class WeatherServiceTest {

    private WeatherService weatherService;

    @Before
    public void init(){
        weatherService = new WeatherService();
    }

    @Test
    public void canary(){
        assertTrue(true);
    }

    @Test
    public void getCityStateWithAZip() {
        Geocoder mockGeocoder = mock(Geocoder.class);
        when(mockGeocoder.getCityStateForZIP(anyString())).thenReturn(new ImmutablePair<>("Houston", "TX"));

        weatherService.setGeocoder(mockGeocoder);

        List<City> actual = weatherService.getCitiesFromZipCodes(new HashSet<>(asList("77204"))).get(SERVICE_STATUS.NO_ERROR);
        assertEquals(asList(new City("77204", "Houston", "TX")), actual);
    }

    @Test
    public void getCityStatesWithTwoZips(){

        Geocoder mockGeocoder = mock(Geocoder.class);
        when(mockGeocoder.getCityStateForZIP("77204")).thenReturn(new ImmutablePair<>("Houston", "TX"));
        when(mockGeocoder.getCityStateForZIP("75201")).thenReturn(new ImmutablePair<>("Dallas", "TX"));

        weatherService.setGeocoder(mockGeocoder);
        HashSet<String> zips =  new HashSet<>(asList("75201", "77204"));

        List<City> actual = weatherService.getCitiesFromZipCodes(zips).get(SERVICE_STATUS.NO_ERROR);
        List<City> expected = asList(new City("77204", "Houston", "TX"), new City("75201", "Dallas", "TX"));
        assertEquals(expected, actual);
    }

    @Test
    public void emptyListOfZipCodesShouldReturnEmptyLists(){
        Geocoder mockGeocoder = mock(Geocoder.class);
        weatherService.setGeocoder(mockGeocoder);
        HashMap<SERVICE_STATUS, List<City>> expected = new HashMap<>();
        expected.put(SERVICE_STATUS.ERROR, new ArrayList<>());
        expected.put(SERVICE_STATUS.NO_ERROR, new ArrayList<>());

        assertEquals(expected, weatherService.getCitiesFromZipCodes(new HashSet<>()));
    }

    @Test
    public void differentZipsSameCityAreOK(){
        Geocoder mockGeocoder = mock(Geocoder.class);
        when(mockGeocoder.getCityStateForZIP("77204")).thenReturn(new ImmutablePair<>("Houston", "TX"));
        when(mockGeocoder.getCityStateForZIP("77205")).thenReturn(new ImmutablePair<>("Houston", "TX"));
        when(mockGeocoder.getCityStateForZIP("77206")).thenReturn(new ImmutablePair<>("Houston", "TX"));

        weatherService.setGeocoder(mockGeocoder);
        HashSet<String> zips =  new HashSet<>(asList("77204", "77205", "77206"));

        List<City> actual = weatherService.getCitiesFromZipCodes(zips).get(SERVICE_STATUS.NO_ERROR);
        List<City> expected = asList(new City("77204", "Houston", "TX"), new City("77205", "Houston", "TX"), new City("77206", "Houston", "TX"));
        assertEquals(expected, actual);
    }

    @Test
    public void citiesAreSortedAlphabetically(){
        City city1 = new City("81611", "Aspen", "CO");
        City city2 = new City("75201", "Dallas", "TX");
        City city3 = new City("77204", "Houston", "TX");

        List<City> actual = weatherService.sortCities(asList(city2, city3, city1));
        List<City> expected = asList(city1, city2, city3);
        assertEquals(expected, actual);
    }

    @Test
    public void cityWithSameNameSortedByState(){
        City city1 = new City("99694", "Houston", "AK");
        City city2 = new City("55943", "Houston", "MN");
        City city3 = new City("77204", "Houston", "TX");

        List<City> actual = weatherService.sortCities(asList(city2, city3, city1));
        List<City> expected = asList(city1, city2, city3);
        assertEquals(expected, actual);
    }

    @Test
    public void cityWithSameNameAndStateSortedByZips(){
        City city1 = new City("77204", "Houston", "TX");
        City city2 = new City("77216", "Houston", "TX");
        City city3 = new City("10081", "New York", "NY");
        City city4 = new City("10265", "New York", "NY");

        List<City> actual = weatherService.sortCities(asList(city4, city1, city3, city2));
        List<City> expected = asList(city1, city2, city3, city4);
        assertEquals(expected, actual);
    }

    @Test
    public void getWeatherWithACity(){
        WeatherSummary summary = new WeatherSummary("Partly Cloudy", 80, 72);

        WeatherReporter mockWeather = mock(WeatherReporter.class);
        when(mockWeather.getWeatherReportForCity("77204")).thenReturn(summary);

        weatherService.setWeatherReporter(mockWeather);
        List<City> inputCity = asList(new City("77204", "Houston", "TX"));
        List<City> actual = weatherService.getWeatherForCities(inputCity).get(SERVICE_STATUS.NO_ERROR);

        inputCity.get(0).setWeatherSummary(summary);
        assertEquals(inputCity, actual);
    }

    @Test
    public void getWeatherWithMultipleCities(){
        WeatherSummary summary = new WeatherSummary("Partly Cloudy", 80, 72);

        WeatherReporter mockWeather = mock(WeatherReporter.class);
        when(mockWeather.getWeatherReportForCity(anyString())).thenReturn(summary);

        List<City> inputCities = asList(new City("77204", "Houston", "TX"), new City("10081", "New York", "NY"), new City("81611", "Aspen", "CO"));

        weatherService.setWeatherReporter(mockWeather);
        List<City> actual = weatherService.getWeatherForCities(inputCities).get(SERVICE_STATUS.NO_ERROR);

        for(City city : inputCities){
            city.setWeatherSummary(summary);
        }

        assertEquals(inputCities, actual);
    }

    @Test
    public void getCityStateWhenGeocodingServiceUndefined(){
        try{
            weatherService.getCitiesFromZipCodes(new HashSet<>(asList("77204"))).isEmpty();
            fail("Null Pointer Exception expected...");
        }catch(NullPointerException ex){
            assertTrue(true);
            //:)
        }
    }

    @Test
    public void getCityStateWhenGeocodingServiceFailed(){
        Geocoder mockGeocoder = mock(Geocoder.class);
        when(mockGeocoder.getCityStateForZIP(anyString())).thenThrow(new RuntimeException("INVALID_REQUEST"));

        weatherService.setGeocoder(mockGeocoder);
        List<City> actual = weatherService.getCitiesFromZipCodes(new HashSet<>(asList("77204"))).get(SERVICE_STATUS.NO_ERROR);
        assertTrue( actual.isEmpty());
    }

    @Test
    public void errorIsSavedWhenGeocodingFails(){
        Geocoder mockGeocoder = mock(Geocoder.class);
        when(mockGeocoder.getCityStateForZIP("abcde")).thenThrow(new RuntimeException("NO RESULTS"));

        weatherService.setGeocoder(mockGeocoder);

        assertEquals(asList(new City("abcde", "Geocoding Error for ZIP abcde : NO RESULTS")), weatherService.getCitiesFromZipCodes(new HashSet<>(asList("abcde"))).get(SERVICE_STATUS.ERROR));
    }

    @Test
    public void zipWithErrorWhenGeoCodingDoNotAffectOtherRequests(){
        Geocoder mockGeocoder = mock(Geocoder.class);
        when(mockGeocoder.getCityStateForZIP("5561")).thenThrow(new RuntimeException("NO RESULTS"));
        when(mockGeocoder.getCityStateForZIP("77216")).thenReturn(new ImmutablePair<>("Houston", "TX"));
        when(mockGeocoder.getCityStateForZIP("77204")).thenReturn(new ImmutablePair<>("Houston", "TX"));

        weatherService.setGeocoder(mockGeocoder);

        Map<SERVICE_STATUS, List<City>> result = weatherService.getCitiesFromZipCodes(new HashSet<>(asList("5561", "77216", "77204")));
        List<City> expected = asList(new City("77204", "Houston", "TX"), new City("77216", "Houston", "TX"));
        assertEquals(expected, result.get(SERVICE_STATUS.NO_ERROR));

        assertEquals(asList(new City("5561", "Geocoding Error for ZIP 5561 : NO RESULTS")), result.get(SERVICE_STATUS.ERROR));
    }

    @Test
    public void getWeatherWhenWeatherReporterUndefined(){
        try {
            weatherService.getWeatherForCities(asList(new City("77204", "Houston", "TX")));
            fail("Null Pointer Exception expected...");
        }catch(NullPointerException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void getWeatherWhenWeatherReporterFailed(){
        WeatherReporter mockWeather = mock(WeatherReporter.class);
        when(mockWeather.getWeatherReportForCity(anyString())).thenThrow(new RuntimeException("QUERY NOT FOUND"));

        weatherService.setWeatherReporter(mockWeather);
        List<City> inputCity = asList(new City("77204", "Houston", "TX"), new City("10081", "New York", "NY"));
        List<City> actual = weatherService.getWeatherForCities(inputCity).get(SERVICE_STATUS.NO_ERROR);

        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void zipWithErrorWhenGettingWeatherIsSaved(){
        WeatherReporter mockWeather = mock(WeatherReporter.class);
        when(mockWeather.getWeatherReportForCity(anyString())).thenThrow(new RuntimeException("QUERY NOT FOUND"));

        weatherService.setWeatherReporter(mockWeather);

        Map<SERVICE_STATUS, List<City>> result = weatherService.getWeatherForCities(asList(new City("77005", "Houston", "TX")));
        assertEquals(asList(new City("77005", "WeatherReporting Error for ZIP 77005 : QUERY NOT FOUND")), result.get(SERVICE_STATUS.ERROR));
    }

    @Test
    public void zipWithErrorDoNotAffectOtherWeatherRequests(){
        WeatherSummary summary = new WeatherSummary("Partly Cloudy", 80, 72);
        WeatherReporter mockWeather = mock(WeatherReporter.class);
        when(mockWeather.getWeatherReportForCity("5555")).thenThrow(new RuntimeException("QUERY NOT FOUND"));
        when(mockWeather.getWeatherReportForCity("77204")).thenReturn(summary);
        when(mockWeather.getWeatherReportForCity("10081")).thenReturn(summary);

        City city1 = new City("77204", "Houston", "TX");
        City city2 = new City("10081", "New York", "NY");

        weatherService.setWeatherReporter(mockWeather);
        Map<SERVICE_STATUS, List<City>> result = weatherService.getWeatherForCities(asList(city1, new City("5555", "", ""), city2));

        city1.setWeatherSummary(summary);
        city2.setWeatherSummary(summary);

        assertEquals(asList(city1, city2), result.get(SERVICE_STATUS.NO_ERROR));
        assertEquals(asList(new City("5555", "WeatherReporting Error for ZIP 5555 : QUERY NOT FOUND")), result.get(SERVICE_STATUS.ERROR));
    }

    @Test
    public void getCityWithTheHighestTemperature(){
        WeatherSummary summary = new WeatherSummary("Partly Cloudy", 80, 65);
        WeatherSummary summary2 = new WeatherSummary("Sunny", 90, 72);

        City city1 = new City("77204", "Houston", "TX", summary);
        City city2 = new City("10081", "New York", "NY", summary2);

        List<City> actual = weatherService.getWarmestCities(asList(city1, city2));

        assertEquals(asList(city2), weatherService.getWarmestCities(actual));
    }

    @Test
    public void getCitiesWithTheHighestTemperature(){
        WeatherSummary summary = new WeatherSummary("Partly Cloudy", 80, 65);
        WeatherSummary summary2 = new WeatherSummary("Sunny", 90, 72);
        WeatherSummary summary3 = new WeatherSummary("Sunny", 90, 82);

        City city1 = new City("77204", "Houston", "TX", summary);
        City city2 = new City("10081", "New York", "NY", summary2);
        City city3 = new City("32801", "Orlando", "FL", summary3);

        List<City> actual = weatherService.getWarmestCities(asList(city1, city2, city3));

        assertEquals(asList(city2, city3), actual);
    }

    @Test
    public void getCityWithTheLowestTemperature(){
        WeatherSummary summary = new WeatherSummary("Partly Cloudy", 80, 65);
        WeatherSummary summary2 = new WeatherSummary("Sunny", 90, 72);

        City city1 = new City("77204", "Houston", "TX", summary);
        City city2 = new City("10081", "New York", "NY", summary2);

        List<City> actual = weatherService.getCoolestCities(asList(city1, city2));

        assertEquals(asList(city1), actual);
    }

    @Test
    public void getCitiesWithTheLowestTemperature(){
        WeatherSummary summary = new WeatherSummary("Partly Cloudy", 80, 65);
        WeatherSummary summary2 = new WeatherSummary("Sunny", 90, 65);
        WeatherSummary summary3 = new WeatherSummary("Sunny", 75, 72);

        City city1 = new City("77204", "Houston", "TX", summary);
        City city2 = new City("13201", "Syracuse", "NY", summary2);
        City city3 = new City("32801", "Orlando", "FL", summary3);

        List<City> actual = weatherService.getCoolestCities(asList(city1, city2, city3));

        assertEquals(asList(city1, city2), actual);
    }

    @Test
    public void testCitiesToString(){
        WeatherSummary summary = new WeatherSummary("Partly Cloudy", 80, 65);
        City city1 = new City("77204", "Houston", "TX", summary);
        String spacer = " ";

        assertEquals("Houston TX 77204 65 80 Partly Cloudy", city1.toString(spacer));
    }

    @Test
    public void testErrorCitiesToString(){
        City city = new City("77204", "has an error");
        String spacer = " ";

        assertEquals("has an error", city.toString(spacer));
    }
}
