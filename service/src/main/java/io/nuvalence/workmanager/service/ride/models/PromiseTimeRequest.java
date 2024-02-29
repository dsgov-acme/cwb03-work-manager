package io.nuvalence.workmanager.service.ride.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class PromiseTimeRequest {
    private String riderId;
    private String pickupPlaceId;
    private String dropPlaceId;
    private AnchorType anchor;
    private PassengerAccommodations passengerAccommodations;
    private long requestTime;
}
