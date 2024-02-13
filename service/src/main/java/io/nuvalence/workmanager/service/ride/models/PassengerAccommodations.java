package io.nuvalence.workmanager.service.ride.models;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@ToString
@Jacksonized
public class PassengerAccommodations {
    private int ambulatorySeats;
    private int wheelchairSeats;
    private int companions;
}
