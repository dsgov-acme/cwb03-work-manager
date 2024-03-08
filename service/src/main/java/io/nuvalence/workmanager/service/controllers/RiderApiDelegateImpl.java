package io.nuvalence.workmanager.service.controllers;

import io.nuvalence.workmanager.service.domain.profile.Individual;
import io.nuvalence.workmanager.service.domain.record.MissingRecordDefinitionException;
import io.nuvalence.workmanager.service.domain.record.Record;
import io.nuvalence.workmanager.service.domain.record.RecordDefinition;
import io.nuvalence.workmanager.service.domain.transaction.MissingTransactionDefinitionException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.generated.controllers.RiderApiDelegate;
import io.nuvalence.workmanager.service.generated.models.*;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.mapper.RecordMapper;
import io.nuvalence.workmanager.service.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiderApiDelegateImpl implements RiderApiDelegate {
    private final RecordService recordService;
    private final TransactionService transactionService;
    private final TransactionDefinitionService transactionDefinitionService;
    private final RecordDefinitionService recordDefinitionService;
    private final IndividualService individualService;
    private final IndividualUserLinkService individualUserLinkService;
    private final RecordMapper mapper;

    /**
     * THIS METHOD IS TEMPORARY! Ideally, we would do this work as part of onboarding into the system.
     * @param riderInitializationRequest The rider initialization request. (required)
     * @return Nothing (success/failure)
     */
    @Override
    public ResponseEntity<RecordResponseModel> initializeRiderDetails(
            RiderInitializationRequest riderInitializationRequest) {
        Optional<Record> record =
                recordService
                        .getRecordByEmailDataField(riderInitializationRequest.getEmail())
                        .stream()
                        .findFirst();

        if (record.isEmpty()) {
            // no records found, create one for the email and user ID
            ResponseEntity<Transaction> createTransactionResponse = createRiderTransaction();
            if (createTransactionResponse.getStatusCode().is2xxSuccessful()) {
                ResponseEntity<Record> createRecordResponse =
                        createRiderRecordFromTransaction(
                                createTransactionResponse.getBody(),
                                riderInitializationRequest.getEmail(),
                                riderInitializationRequest.getUserId());

                if (createTransactionResponse.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.ok(
                            mapper.recordToRecordResponseModel(createRecordResponse.getBody()));
                }

                return ResponseEntity.status(createRecordResponse.getStatusCode().value()).build();
            } else {
                return ResponseEntity.status(createTransactionResponse.getStatusCode().value())
                        .build();
            }
        } else if (!record.get()
                .getSubjectUserId()
                .equals(riderInitializationRequest.getUserId())) {
            RecordUpdateRequest request =
                    new RecordUpdateRequest().subjectUserId(riderInitializationRequest.getUserId());
            Record updatedRecord = recordService.updateRecord(request, record.get());
            return ResponseEntity.ok(mapper.recordToRecordResponseModel(updatedRecord));
        }

        return ResponseEntity.ok(mapper.recordToRecordResponseModel(record.get()));
    }

    private ResponseEntity<Transaction> createRiderTransaction() {
        final TransactionCreationRequest request = new TransactionCreationRequest("MTARiderDummy");

        try {
            final TransactionDefinition definition =
                    transactionDefinitionService
                            .getTransactionDefinitionByKey(request.getTransactionDefinitionKey())
                            .orElseThrow(
                                    () ->
                                            new MissingTransactionDefinitionException(
                                                    request.getTransactionDefinitionKey()));

            Individual individual = individualService.createOrGetIndividualForCurrentUser();

            final Transaction transaction =
                    (request.getMetadata() == null || request.getMetadata().isEmpty())
                            ? transactionService.createTransactionWithIndividualSubject(
                                    definition, individual)
                            : transactionService.createTransactionWithIndividualSubject(
                                    definition, individual, request.getMetadata());

            individualUserLinkService.createAdminUserLinkForProfile(transaction);

            postAuditEventForTransactionCreated(transaction);

            return ResponseEntity.ok(transaction);
        } catch (MissingSchemaException e) {
            log.error(
                    String.format(
                            "transaction definition [%s] references missing schema.",
                            request.getTransactionDefinitionKey()),
                    e);
            return ResponseEntity.status(424).build();
        } catch (MissingTransactionDefinitionException e) {
            log.error(
                    String.format(
                            "ID [%s] references missing transaction definition.",
                            request.getTransactionDefinitionKey()),
                    e);
            return ResponseEntity.status(424).build();
        }
    }

    private Map<String, Object> getAddressAsMap(String externalId) {
        Map<String, Object> location = new HashMap<>();
        location.put("name", "Home");
        Map<String, Object> address = new HashMap<>();
        address.put("city", "New York");
        address.put("stateCode", "NY");
        address.put("postalCode", "10011");
        address.put("countryCode", "US");
        address.put("addressLine1", "47 W 13th St");
        location.put("address", address);
        location.put("riderId", externalId);

        return location;
    }

    private ResponseEntity<Record> createRiderRecordFromTransaction(
            Transaction transaction, String email, String subjectUserId) {
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> pca = new HashMap<>();
        pca.put("phone", "(999) 999-9999");
        pca.put("lastName", "Gleason");
        pca.put("firstName", "Louis");
        pca.put("shareTripDetails", true);

        Map<String, Object> emergencyContact = new HashMap<>();
        emergencyContact.put("phone", "(999) 999-9999");
        emergencyContact.put("lastName", "Hansen");
        emergencyContact.put("firstName", "August");
        emergencyContact.put("relationship", "Father");
        emergencyContact.put("shareTripDetails", true);

        Map<String, Object> accommodations = new HashMap<>();
        accommodations.put("wcSeats", true);
        accommodations.put("ambSeats", true);
        accommodations.put("fareType", "Regular");
        accommodations.put("wideSeats", true);
        accommodations.put("pcaRequired", true);
        accommodations.put("disabilities", List.of("Epilepsy", "Orthopedic", "Vision"));
        accommodations.put("numCompanion", 0);
        accommodations.put("travelTraining", false);
        accommodations.put(
                "mobilityDevices", List.of("Walker", "Support Cane", "Power Wheelchair"));
        accommodations.put("interpreterNeeded", false);
        accommodations.put("personalCareAttendant", pca);
        accommodations.put("serviceAnimalRequired", true);

        data.put("id", transaction.getExternalId());
        data.put("email", email);
        data.put("phone", "(999) 999-9999");
        data.put("gender", "Male");
        data.put("language", "English");
        data.put("lastName", "User");
        data.put("firstName", "Test");
        data.put("rideTypes", List.of("Turnaround", "Subscription"));
        data.put("dateOfBirth", "1990-01-01");
        data.put("homeAddress", getAddressAsMap(transaction.getExternalId()));
        data.put("accommodations", accommodations);
        data.put("paymentMethods", List.of(Map.of("type", "Cash"), Map.of("type", "OMNY Card")));
        data.put("savedLocations", List.of());
        data.put("alternateAddress", getAddressAsMap(transaction.getExternalId()));
        data.put("emergencyContact", emergencyContact);
        data.put("primaryPickupAddress", getAddressAsMap(transaction.getExternalId()));
        data.put("communicationPreferences", List.of("Phone Call", "SMS"));

        RecordCreationRequest request =
                new RecordCreationRequest()
                        .recordDefinitionKey("MTARider")
                        .transactionId(transaction.getId())
                        .data(data)
                        .status("Active")
                        .externalId(transaction.getExternalId())
                        .subjectUserId(subjectUserId);

        try {
            final RecordDefinition definition =
                    recordDefinitionService
                            .getRecordDefinitionByKey(request.getRecordDefinitionKey())
                            .orElseThrow(
                                    () ->
                                            new MissingRecordDefinitionException(
                                                    request.getRecordDefinitionKey()));

            final Record transactionRecord =
                    recordService.createRecord(definition, transaction, request);

            return ResponseEntity.ok(transactionRecord);
        } catch (MissingSchemaException e) {
            log.error(
                    String.format(
                            "transaction definition [%s] references missing schema.",
                            request.getRecordDefinitionKey()),
                    e);
            return ResponseEntity.status(424).build();
        } catch (MissingRecordDefinitionException e) {
            log.error(
                    String.format(
                            "ID [%s] references missing record definition.",
                            request.getRecordDefinitionKey()),
                    e);
            return ResponseEntity.status(424).build();
        }
    }

    private void postAuditEventForTransactionCreated(Transaction transaction) {
        try {
            transactionService.postAuditEventForTransactionCreated(transaction);

        } catch (Exception e) {
            String errorMessage =
                    String.format(
                            "An error has occurred when recording a creation audit event for a"
                                    + " transaction with user id %s for transaction with id %s.",
                            transaction.getCreatedBy(), transaction.getId());
            log.error(errorMessage, e);
        }
    }
}
