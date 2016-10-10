package weather;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.lang3.tuple.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;


public class GoogleGeocoder implements Geocoder{
    final String googleMapsURL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    final String API_KEY = "&key=AIzaSyCoOy5RVcBkKINIXlrCSkSzeeqMioyxyX4";

    public Pair<String, String> getCityStateForZIP(String zipcode) {
        try {
            URL url = getURL(zipcode);
            HttpURLConnection connection = setUpConnection((HttpURLConnection) url.openConnection());
            final String data = getJSONDataFromService(connection.getInputStream());
            return JSONParser(data);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public HttpURLConnection setUpConnection(HttpURLConnection urlConnection) throws IOException, InterruptedException {
        final int RETRIES = 5;
        int retry = 0;
        int responseCode;
        do{
            if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                return urlConnection;
            responseCode = urlConnection.getResponseCode();
            urlConnection.disconnect();
            retry++;
            Thread.sleep(100);
        }while (retry <RETRIES);

        throw new IOException(String.format("Unable to connect after %d retries. HTTP status code %d", retry, responseCode));
    }

    String getJSONDataFromService(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines().collect(Collectors.joining());
    }

    URL getURL(String zipcode) throws MalformedURLException {

        if(zipcode.matches("^\\d{5}$|^\\d{5}-\\d{4}$"))
            return new URL(googleMapsURL + zipcode + API_KEY);
        else
            throw new RuntimeException("Format does not match US ZIP Code");
    }

    Pair<String, String> JSONParser(String str) throws JSONException {

        JSONObject obj = new JSONObject(str);

        if (obj.get("status").equals("OK") == false)
            throw new RuntimeException(obj.get("status").toString());

        String city = null, state = null;

        try {
            JSONArray addressComp = obj.getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

            for (int i = 0; i < addressComp.length(); i++) {
                JSONObject jsonObj = addressComp.getJSONObject(i);
                JSONArray values = jsonObj.getJSONArray("types");
                if (values.get(0).equals("locality"))
                    city = jsonObj.get("long_name").toString();
                if (values.get(0).equals("administrative_area_level_1"))
                    state = jsonObj.get("short_name").toString();
            }
            return new ImmutablePair<>(city, state);
        }catch (JSONException ex){
            throw new RuntimeException("JSON string is in unexpected format : "+ ex.getMessage());
        }
    }

}
