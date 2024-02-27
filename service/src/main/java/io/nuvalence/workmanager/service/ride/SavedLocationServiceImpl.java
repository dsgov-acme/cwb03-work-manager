package io.nuvalence.workmanager.service.ride;

import io.nuvalence.workmanager.service.domain.dynamicschema.DynamicEntity;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.models.CommonAddress;
import io.nuvalence.workmanager.service.repository.TransactionRepository;
import io.nuvalence.workmanager.service.ride.models.MTALocation;
import io.nuvalence.workmanager.service.ride.models.MTALocationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class SavedLocationServiceImpl implements SavedLocationService {
    private static final String SAVED_LOCATION_KEY = "MTALocation";
    private static final String SAVED_LOCATION_STATUS = "Completed";
    private final TransactionRepository transactionRepository;

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

    private MTALocation convertToMTALocation(Transaction transaction) {

        DynamicEntity dynamicEntity = transaction.getData();
        String id = transaction.getId().toString();
        String placeId = dynamicEntity.getProperty("placeId", String.class);
        String name = dynamicEntity.getProperty("name", String.class);
        String riderId = dynamicEntity.getProperty("riderId", String.class);
        MTALocationType locationType =
                dynamicEntity.getProperty("locationType", MTALocationType.class);

        String address1 = dynamicEntity.getProperty("address.addressLine1", String.class);
        String address2 = dynamicEntity.getProperty("address.addressLine2", String.class);
        String city = dynamicEntity.getProperty("address.city", String.class);
        String stateCode = dynamicEntity.getProperty("address.stateCode", String.class);
        String postalCode = dynamicEntity.getProperty("address.postalCode", String.class);
        String postalCodeExtension =
                dynamicEntity.getProperty("address.postalCodeExtension", String.class);
        String countryCode = dynamicEntity.getProperty("address.countryCode", String.class);

        MTALocation location = new MTALocation();
        location.setId(id);
        location.setPlaceId(placeId);
        location.setName(name);
        location.setRiderId(riderId);
        location.setLocationType(locationType);

        CommonAddress commonAddress = new CommonAddress();
        commonAddress.setAddressLine1(address1);
        commonAddress.setAddressLine2(address2);
        commonAddress.setCity(city);
        commonAddress.setStateCode(stateCode);
        commonAddress.setPostalCode(postalCode);
        commonAddress.setPostalCodeExtension(postalCodeExtension);
        commonAddress.setCountryCode(countryCode);

        // Set the address for the location
        location.setAddress(commonAddress);

        return location;
    }
}
