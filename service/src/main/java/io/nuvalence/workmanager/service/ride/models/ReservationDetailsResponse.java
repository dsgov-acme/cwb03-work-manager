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
    private String id;
    private AnchorType anchor;
    private long requestTime;
    private ReservationDetailsAnchor pickup;
    private ReservationDetailsAnchor dropOff;
    private PassengerAccommodations passengerAccommodations;
    private String route;
}
