package weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

public class OpenWeatherMapReporter implements WeatherReporter {
    final String openWeatherMapURL = "http://api.openweathermap.org/data/2.5/forecast/daily?zip=";
    final String API_KEY = "&appid=bb4eea7ffb7cc057e1cb84243eaa5c1f";
    final String units = "&units=Imperial";

    public WeatherSummary getWeatherReportForCity(String zipcode) {
        try {
            URL url = getURL(zipcode);
            HttpURLConnection connection = setUpConnection((HttpURLConnection) url.openConnection());
            final String data = getJSONDataFromService(connection.getInputStream());
            connection.disconnect();

            return JSONParser(data);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    HttpURLConnection setUpConnection(HttpURLConnection urlConnection) throws IOException, InterruptedException {
        final int RETRIES = 5;
        int retry = 0;
        int responseCode;
        do{
            if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                return urlConnection;
            responseCode = urlConnection.getResponseCode();
            urlConnection.disconnect();
            retry++;
            Thread.sleep(200);
        }while (retry <RETRIES);

        throw new IOException(String.format("Unable to connect after %d retries. HTTP status code %d", retry, responseCode));
    }

    String getJSONDataFromService(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines().collect(Collectors.joining());
    }

    URL getURL(String zipcode) throws MalformedURLException {
        return new URL(openWeatherMapURL + zipcode + API_KEY + units);
    }

    WeatherSummary JSONParser(String str){
        try {
            JSONArray obj = new JSONObject(str).getJSONArray("list");
            JSONObject firstResult = obj.getJSONObject(0);

            JSONObject weather = firstResult.getJSONArray("weather").getJSONObject(0);
            JSONObject temp = firstResult.getJSONObject("temp");

            String conditions = weather.get("main").toString();
            Integer highTemp = Double.valueOf(temp.get("max").toString()).intValue();
            Integer lowTemp = Double.valueOf(temp.get("min").toString()).intValue();

            return new WeatherSummary(conditions, highTemp, lowTemp);

        }catch (JSONException ex) {
            throw new RuntimeException("JSON string is in unexpected format : "+ ex.getMessage());
        }
    }
}
