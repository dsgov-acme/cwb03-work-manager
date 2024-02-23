package io.nuvalence.workmanager.service.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import io.nuvalence.workmanager.service.models.CommonAddress;
import io.nuvalence.workmanager.service.ride.models.MTALocation;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import jakarta.transaction.Transactional;

@Component
@Transactional
@AllArgsConstructor
@RequiredArgsConstructor
@Slf4j
public class GeocodingService {
    private static final String ADDRESS_FORMAT = "%s %s, %s %s %s";

    // @Value("${google.geocoding.auth}")
    private String geocodeAPIKey = "key";

    private GeoApiContext context;

    @PostConstruct
    void postConstruct() {
        context = new GeoApiContext.Builder().apiKey(geocodeAPIKey).build();
    }

    @PreDestroy
    void onDestroy() {
        context.shutdown();
    }

    public MTALocation geocodeLocation(CommonAddress address) {
        try {
            String addressStr = extractAddress(address);
            GeocodingResult[] results = GeocodingApi.geocode(context, addressStr).await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            String result = gson.toJson(results[0]);

            // remove this after smoke testing
            log.debug("Geocoded address {} to result {}", address, result);

            return MTALocation.builder()
//                    .latitude(results[0].geometry.location.lat)
//                    .longitude(results[0].geometry.location.lng)
                    .address(address)
                    .build();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractAddress(CommonAddress address) {
        return String.format(
                ADDRESS_FORMAT,
                (address.getAddressLine1()
                        + Optional.ofNullable(address.getAddressLine2())
                                .map(a2 -> " " + a2)
                                .orElse("")),
                address.getCity(),
                address.getStateCode(),
                (address.getPostalCode()
                        + Optional.ofNullable(address.getPostalCodeExtension())
                                .map(a2 -> " " + a2)
                                .orElse("")),
                address.getCountryCode());
    }
}
