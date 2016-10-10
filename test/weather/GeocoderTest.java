package weather;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

public class GeocoderTest {

    private GoogleGeocoder geocoder;
    public static final String SAMPLE_JSON_INPUT = "{\"results\":[{\"address_components\":[{\"long_name\":\"Saint Paul\",\"short_name\":\"St Paul\",\"types\":[\"locality\",\"political\"]},"
            + "{\"long_name\":\"Minnesota\",\"short_name\":\"MN\",\"types\":[\"administrative_area_level_1\",\"political\"]},"
            + "{\"long_name\":\"United States\",\"short_name\":\"US\",\"types\":[\"country\",\"political\"]}]}],\"status\":\"OK\"}";

    @Before
    public void init() {
        geocoder = new GoogleGeocoder();
    }

    @Test
    public void getValuesFromJSONExtractsCityAndState() throws JSONException {
        assertEquals(new ImmutablePair<>("Saint Paul", "MN"), geocoder.JSONParser(SAMPLE_JSON_INPUT));
    }

    @Test
    public void statusNotOKThrowsException() throws JSONException {
        String sampleJSONInput = "{\"results\" : [],\"status\" : \"ZERO_RESULTS\"}";

        try{
            geocoder.JSONParser(sampleJSONInput);
            fail("Expected exception for ZERO_RESULTS");
        }catch (RuntimeException ex)
        {
            assertEquals("ZERO_RESULTS", ex.getMessage());
        }
    }

    @Test
    public void getcityStateWithGeoCoder() throws IOException {
        String zip = "12345";

        GoogleGeocoder geocoder = spy(new GoogleGeocoder());
        doReturn(SAMPLE_JSON_INPUT).when(geocoder).getJSONDataFromService(any());

        assertEquals(new ImmutablePair<>("Saint Paul", "MN"), geocoder.getCityStateForZIP(zip));
    }

    @Test
    public void malformedURLThrowsException(){
        geocoder = new GoogleGeocoder(){
            @Override
            URL getURL(String zipcode) throws MalformedURLException {
                return new URL("htp://google.com");
            }
        };
        try{
            geocoder.getCityStateForZIP("77005");
            fail("Expected Exception for Malformed URL");
        }catch (RuntimeException ex){
            assertTrue(true);
        }
    }

    @Test
    public void ErrorOpeningInputStreamThrowsException() throws IOException {
        geocoder = new GoogleGeocoder(){
            @Override
            URL getURL(String zipcode) throws MalformedURLException {
                return new URL("ftp://geocoder");
            }
        };

        try{
            geocoder.getCityStateForZIP("77005");
            fail("Expected Exception");
        }catch (RuntimeException ex){
            assertTrue(true);
        }
    }

    @Test
    public void unexpectedFormatJSONThrowsException() throws JSONException {
        String sampleJSONInput = "{\"results\":[],\"status\":\"OK\"}";
        try {
            geocoder.JSONParser(sampleJSONInput);
            fail("Expected exception for wrong JSON String");
        }catch (RuntimeException ex)
        {
            assertTrue(true);
        }
    }

    @Test
    public void getJSONDataFromStreamReturnsCorrectString() throws IOException {
        InputStream stream = new ByteArrayInputStream(SAMPLE_JSON_INPUT.getBytes(StandardCharsets.UTF_8));
        assertEquals(SAMPLE_JSON_INPUT, geocoder.getJSONDataFromService(stream));
    }

    @Test
    public void HTTPResponseNOTOKRetries() throws IOException, InterruptedException {
        HttpURLConnection mockconnection = mock(HttpURLConnection.class);
        when(mockconnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);

        try{
            geocoder.setUpConnection(mockconnection);
            fail("Expected exception for failed connection");
        }catch (IOException ex ){
            assertTrue(true);
            assertEquals("Unable to connect after 5 retries. HTTP status code 400", ex.getMessage());
        }
    }

}
