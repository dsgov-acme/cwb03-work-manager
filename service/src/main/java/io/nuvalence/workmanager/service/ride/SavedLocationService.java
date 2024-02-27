package io.nuvalence.workmanager.service.ride;

import io.nuvalence.workmanager.service.ride.models.MTALocation;

import java.util.List;

public interface SavedLocationService {

    List<MTALocation> getSavedLocationsByRiderId(String riderId);

    List<MTALocation> getSavedLocationsByUserId(String userId);
}
