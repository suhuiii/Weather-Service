package weather;

import org.apache.commons.lang3.tuple.Pair;

public interface Geocoder {
    public Pair<String, String> getCityStateForZIP(String zipcode);
}
