package weather;

public interface WeatherReporter {
    public WeatherSummary getWeatherReportForCity(String zipcode);
}
