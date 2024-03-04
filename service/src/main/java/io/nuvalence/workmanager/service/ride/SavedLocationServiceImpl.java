package io.nuvalence.workmanager.service.ride;

import io.nuvalence.workmanager.service.domain.dynamicschema.DynamicEntity;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.mapper.RideMapper;
import io.nuvalence.workmanager.service.repository.TransactionRepository;
import io.nuvalence.workmanager.service.ride.models.MTALocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class SavedLocationServiceImpl implements SavedLocationService {
    private static final String SAVED_LOCATION_KEY = "MTALocation";
    private static final String SAVED_LOCATION_STATUS = "Completed";
    private final TransactionRepository transactionRepository;
    private final RideMapper rideMapper;

    @Override
    public List<MTALocation> getSavedLocationsByRiderId(String riderId) {
        List<Transaction> transactions =
                transactionRepository.findByTransactionDefinitionKeyAndExternalIdAndStatus(
                        SAVED_LOCATION_KEY, riderId, SAVED_LOCATION_STATUS);
        return transactions.stream().map(this::convertToMTALocation).collect(Collectors.toList());
    }

    @Override
    public List<MTALocation> getSavedLocationsByUserId(String userId) {
        List<Transaction> transactions =
                transactionRepository.findByTransactionDefinitionKeyAndSubjectUserIdAndStatus(
                        SAVED_LOCATION_KEY, userId, SAVED_LOCATION_STATUS);
        return transactions.stream().map(this::convertToMTALocation).collect(Collectors.toList());
    }

    @Override
    public Optional<MTALocation> getSavedLocationsById(UUID savedLocationId) {
        return transactionRepository.findById(savedLocationId).map(this::convertToMTALocation);
    }

    private MTALocation convertToMTALocation(Transaction transaction) {

        DynamicEntity dynamicEntity = transaction.getData();
        String id = transaction.getId().toString();
        MTALocation location = rideMapper.mapEntityToMTALocation(dynamicEntity);
        location.setId(id);

        return location;
    }
}
