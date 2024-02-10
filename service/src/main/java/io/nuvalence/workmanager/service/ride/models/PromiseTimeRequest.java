package io.nuvalence.workmanager.service.ride.models;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@ToString
@Jacksonized
public class PromiseTimeRequest {
    private String riderId;
    private GeocodedLocation pickLocation;
    private GeocodedLocation dropLocation;
    private RiderRestrictions riderRestrictions;
}