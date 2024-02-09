package io.nuvalence.workmanager.service.ride.models;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@ToString
@Jacksonized
public class GeocodedLocation {
    private BigDecimal latitude;
    private BigDecimal longitude;
}
