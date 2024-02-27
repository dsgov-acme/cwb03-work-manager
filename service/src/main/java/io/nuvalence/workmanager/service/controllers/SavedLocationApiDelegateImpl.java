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
        if (userId != null) {
            return Optional.of(userId)
                    .map(savedLocationService::getSavedLocationsByUserId)
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(savedLocationMapper::toDto)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), ResponseEntity::ok));
        }

        // query by rider id
        return Optional.of(riderId)
                .map(savedLocationService::getSavedLocationsByRiderId)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(savedLocationMapper::toDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ResponseEntity::ok));
    }
}
