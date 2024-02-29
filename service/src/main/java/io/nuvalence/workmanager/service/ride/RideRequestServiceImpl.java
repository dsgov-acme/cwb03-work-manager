package io.nuvalence.workmanager.service.ride;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import io.nuvalence.workmanager.service.config.exceptions.ApiException;
import io.nuvalence.workmanager.service.ride.models.MTALocation;
import io.nuvalence.workmanager.service.ride.models.PromiseTimeRequest;
import io.nuvalence.workmanager.service.ride.models.PromiseTimeResponse;
import io.nuvalence.workmanager.service.ride.models.ReservationDetailsRequest;
import io.nuvalence.workmanager.service.ride.models.ReservationDetailsResponse;
import io.nuvalence.workmanager.service.ride.models.SubmitReservationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Primary
@Component
@Slf4j
public class RideRequestServiceImpl implements RideRequestService {
    private final String baseUrl;
    private final String authToken;
    private final RestTemplate httpClient;
    private final SavedLocationService savedLocationService;

    public RideRequestServiceImpl(
            @Qualifier("noAuthHttpClient") final RestTemplate httpClient,
            @Value("${rideRequest.baseUrl}") final String baseUrl,
            @Value("${rideRequest.authToken}") final String authToken,
            final SavedLocationService savedLocationService) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.authToken = authToken;
        this.savedLocationService = savedLocationService;
    }

    /**
     * Gets promise times with the provided criteria.
     *
     * @param request the criteria
     * @return PromiseTimeResponse
     */
    @Override
    public Optional<PromiseTimeResponse> getPromiseTimes(PromiseTimeRequest request) {
        log.info("Retrieving promise times for request: {}", request);

        prepareRequest(request);

        final String url = String.format("%s/promise-times", baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, authToken);

        HttpEntity<PromiseTimeRequest> payload = new HttpEntity<>(request, headers);

        ResponseEntity<PromiseTimeResponse> response;
        try {
            response =
                    httpClient.exchange(url, HttpMethod.POST, payload, PromiseTimeResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("Failed to retrieve promise times for request {}", request, e);
            if (NOT_FOUND == e.getStatusCode()) {
                return Optional.empty();
            }
            throw e;
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ApiException(
                    "Failed to retrieve promise times for request.  Status code: "
                            + response.getStatusCode());
        }

        log.info("Successfully promise times for request: {}", request);
        return Optional.ofNullable(response.getBody());
    }

    /**
     * Submits the reservation request.
     *
     * @param request the request
     * @return ReservationDetailsResponse
     */
    @Override
    public Optional<ReservationDetailsResponse> submitReservation(
            SubmitReservationRequest request) {
        log.info("Submitting reservation for request with ID: {}", request.getId());

        final String url = String.format("%s/reservations", baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, authToken);

        HttpEntity<SubmitReservationRequest> payload = new HttpEntity<>(request, headers);
        ResponseEntity<ReservationDetailsResponse> response;
        try {
            response =
                    httpClient.exchange(
                            url, HttpMethod.PUT, payload, ReservationDetailsResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("Failed to retrieve promise times for request {}", request, e);
            if (NOT_FOUND == e.getStatusCode()) {
                return Optional.empty();
            }
            throw e;
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ApiException(
                    "Failed to retrieve promise times for request.  Status code: "
                            + response.getStatusCode());
        }

        log.info("Successfully submitted reservation for request with ID: {}", request.getId());
        return Optional.ofNullable(response.getBody());
    }

    /**
     * Retrieves the reservation details.
     *
     * @param request the request
     * @return ReservationDetailsResponse
     */
    @Override
    public Optional<ReservationDetailsResponse> getReservationDetails(
            ReservationDetailsRequest request) {
        log.info(
                "Retrieving reservation details for request with promiseTimeId: {}",
                request.getReservationId());

        final String url =
                String.format("%s/reservations?id=%s", baseUrl, request.getReservationId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, authToken);

        HttpEntity<String> payload = new HttpEntity<>(headers);
        ResponseEntity<ReservationDetailsResponse> response;
        try {
            response =
                    httpClient.exchange(
                            url, HttpMethod.GET, payload, ReservationDetailsResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("Failed to retrieve promise times for request {}", request, e);
            if (NOT_FOUND == e.getStatusCode()) {
                return Optional.empty();
            }
            throw e;
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ApiException(
                    "Failed to retrieve promise times for request.  Status code: "
                            + response.getStatusCode());
        }

        log.info(
                "Successfully retrieved reservation details for request with promiseTimeId: {}",
                request.getReservationId());
        return Optional.ofNullable(response.getBody());
    }

    /**
     * PlaceIds are sometimes sent as the IDs of saved locations (in UUID format).
     * If this happens, convert it to the corresponding placeId that is recognized by the external API.
     */
    private void prepareRequest(PromiseTimeRequest request) {
        try {
            UUID savedLocationId = UUID.fromString(request.getDropPlaceId());
            request.setDropPlaceId(lookupPlaceIdFromSavedLocation(savedLocationId));
        } catch (IllegalArgumentException e) {
            // id is not in UUID format - continue
        } catch (Exception e) {
            log.error("Unexpected error when preparing request to retrieve promise times", e);
        }

        try {
            UUID savedLocationId = UUID.fromString(request.getPickupPlaceId());
            request.setPickupPlaceId(lookupPlaceIdFromSavedLocation(savedLocationId));
        } catch (IllegalArgumentException e) {
            // id is not in UUID format - continue
        } catch (Exception e) {
            log.error("Unexpected error when preparing request to retrieve promise times", e);
        }
    }

    private String lookupPlaceIdFromSavedLocation(UUID savedLocationId) {
        return savedLocationService
                .getSavedLocationsById(savedLocationId)
                .map(MTALocation::getPlaceId)
                .orElseThrow(
                        () ->
                                new ApiException(
                                        "Failed to lookup saved location by ID: "
                                                + savedLocationId));
    }
}
