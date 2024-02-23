package io.nuvalence.workmanager.service.ride.models;

import io.nuvalence.workmanager.service.models.CommonAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MTALocation {
    private String id;
    private String placeId;
    private String name;
    private String riderId;
    private MTALocationType locationType;
    private CommonAddress address;
}
