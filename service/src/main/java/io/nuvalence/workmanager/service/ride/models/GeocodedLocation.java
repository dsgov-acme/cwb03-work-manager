package io.nuvalence.workmanager.service.ride.models;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
@Jacksonized
public class GeocodedLocation {
    private BigDecimal latitude;
    private BigDecimal longitude;
}
