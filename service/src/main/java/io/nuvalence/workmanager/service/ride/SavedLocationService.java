package io.nuvalence.workmanager.service.ride;

import io.nuvalence.workmanager.service.ride.models.MTALocation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedLocationService {

    List<MTALocation> getSavedLocationsByRiderId(String riderId);

    List<MTALocation> getSavedLocationsByUserId(String userId);

    Optional<MTALocation> getSavedLocationsById(UUID savedLocationId);
}
