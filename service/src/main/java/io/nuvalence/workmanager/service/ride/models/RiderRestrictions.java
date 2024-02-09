package io.nuvalence.workmanager.service.ride.models;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@ToString
@Jacksonized
public class RiderRestrictions {
    private int numCompanions;
    private int numPca;
    private int numServiceAnimal;
    private boolean ambSeats;
    private boolean wcSeats;
    private boolean wideSeats;
}
