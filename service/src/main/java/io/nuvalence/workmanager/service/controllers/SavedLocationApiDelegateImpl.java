package io.nuvalence.workmanager.service.controllers;

import io.nuvalence.workmanager.service.generated.controllers.SavedLocationsApiDelegate;
import io.nuvalence.workmanager.service.generated.models.MTALocation;
import io.nuvalence.workmanager.service.mapper.SavedLocationMapper;
import io.nuvalence.workmanager.service.ride.SavedLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedLocationApiDelegateImpl implements SavedLocationsApiDelegate {

    private final SavedLocationService savedLocationService;
    private final SavedLocationMapper savedLocationMapper;

    @Override
    public ResponseEntity<List<MTALocation>> getSavedLocations(String riderId, String userId) {
        if (riderId == null && userId == null) {
            log.error("One of riderId or userId are required.");
            return ResponseEntity.badRequest().build();
        }

        var locations =
                userId != null
                        ? savedLocationService.getSavedLocationsByUserId(userId)
                        : savedLocationService.getSavedLocationsByRiderId(riderId);

        return Optional.of(locations).orElseGet(Collections::emptyList).stream()
                .map(savedLocationMapper::toDto)
                .sorted(Comparator.comparing(MTALocation::getName))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ResponseEntity::ok));
    }
}
