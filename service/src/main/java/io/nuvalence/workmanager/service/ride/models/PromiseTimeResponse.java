package io.nuvalence.workmanager.service.ride.models;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@ToString
@Jacksonized
public class PromiseTimeResponse {
    private AnchorType anchor;
    private String riderId;
    private PromiseTime[] promises;
}
