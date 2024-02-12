package io.nuvalence.workmanager.service.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MTALocation {
    private String id;
    private String placeId;
    private String name;
    private String riderId;
    private MTALocationType locationType;
    private double latitude;
    private double longitude;
    private CommonAddress address;
}
