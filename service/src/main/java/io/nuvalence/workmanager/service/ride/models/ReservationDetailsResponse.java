package io.nuvalence.workmanager.service.ride.models;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@ToString
@Jacksonized
public class ReservationDetailsResponse {
    private String reservationId;
    private String routeId;
    private GeocodedLocation startLocation;
    private GeocodedLocation endLocation;
}