package weather;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class WeatherReporterTest {
    private OpenWeatherMapReporter weatherReporter;
    String SAMPLE_JSON_INPUT = "{\"city\":{\"id\":4696376,\"name\":\"Harris County\",\"coord\":{\"lon\":-95.400223,\"lat\":29.833561},\"country\":\"US\",\"population\":0},"
            + "\"cod\":\"200\",\"message\":0.0268,\"cnt\":7,\"list\":[{\"dt\":1457373600,\"temp\":{\"day\":73.63,\"min\":69.51,\"max\":74.52,\"night\":69.51,\"eve\":72.34,\"morn\":73.63},\"pressure\":1027.41,\"humidity\":64,"
            + "\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"speed\":8.11,\"deg\":165,\"clouds\":92}]}";

    @Before
    public void init() {
        weatherReporter = new OpenWeatherMapReporter();
    }

    @Test
    public void getValuesFromJSONExtractsCityAndState() throws JSONException {
        assertEquals(new WeatherSummary("Rain", 74, 69), weatherReporter.JSONParser(SAMPLE_JSON_INPUT));
    }

    @Test
    public void unexpectedFormatJSONThrowsException(){
        String sampleJSONInput = "{\"results\":}";
        try {
            weatherReporter.JSONParser(sampleJSONInput);
            fail("Expected exception for wrong JSON String");
        }catch (RuntimeException ex)
        {
            assertTrue(true);
        }
    }

    @Test
    public void malformedURLThrowsException(){
        weatherReporter = new OpenWeatherMapReporter(){
            @Override
            URL getURL(String zipcode) throws MalformedURLException {
                return new URL("htt://google.com");
            }
        };

        try{
            weatherReporter.getWeatherReportForCity("77083").getHighestTemperature();
            fail("Expected Exception for Malformed URL");
        }catch (RuntimeException ex){
            assertTrue(true);
        }
    }

    @Test
    public void ErrorOpeningInputStreamThrowsException() throws IOException {
        weatherReporter = new OpenWeatherMapReporter(){
            @Override
            URL getURL(String zipcode) throws MalformedURLException {
                return new URL("http://127.0.0.1");
            }
        };

        try{
            weatherReporter.getWeatherReportForCity("77083");
            fail("Expected Exception");
        }catch (RuntimeException ex){
            assertTrue(true);
        }
    }

    @Test
    public void getJSONDataFromStreamReturnsCorrectString() throws IOException {
        InputStream stream = new ByteArrayInputStream(SAMPLE_JSON_INPUT.getBytes(StandardCharsets.UTF_8));
        assertEquals(SAMPLE_JSON_INPUT, weatherReporter.getJSONDataFromService(stream));
    }

    @Test
    public void getWeatherReportReporter() throws IOException {
        String zip = "abcde";

        OpenWeatherMapReporter weatherReporter = spy(new OpenWeatherMapReporter());
        doReturn(SAMPLE_JSON_INPUT).when(weatherReporter).getJSONDataFromService(any());

        assertEquals(new WeatherSummary("Rain", 74, 69),weatherReporter.getWeatherReportForCity(zip));
    }

    @Test
    public void HTTPResponseOKReturnsSameConnection() throws IOException, InterruptedException {
        HttpURLConnection mockconnection = mock(HttpURLConnection.class);
        when(mockconnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        assertEquals(mockconnection, weatherReporter.setUpConnection(mockconnection));
    }

    @Test
    public void HTTPResponseNOTOKRetries() throws IOException, InterruptedException {
        HttpURLConnection mockconnection = mock(HttpURLConnection.class);
        when(mockconnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);

        try{
            weatherReporter.setUpConnection(mockconnection);
            fail("Expected exception for failed connection");
        }catch (IOException ex ){
            assertTrue(true);
            assertEquals("Unable to connect after 5 retries. HTTP status code 400", ex.getMessage());
        }
    }

}
