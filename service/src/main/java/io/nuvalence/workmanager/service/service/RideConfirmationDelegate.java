package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.ride.RideRequestService;
import io.nuvalence.workmanager.service.ride.models.SubmitReservationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component("RideConfirmationDelegate")
public class RideConfirmationDelegate implements JavaDelegate {

    private final TransactionService transactionService;
    private final RideRequestService rideService;
    private final String CONFIRMED_STATUS = "CONFIRMED";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        UUID transactionId = (UUID) execution.getVariable("transactionId");

        if (transactionId == null) {
            log.warn("RideConfirmationDelegate - transactionId not found");
            return;
        }
        Optional<Transaction> transactionOptional =
                transactionService.getTransactionById(transactionId);

        if (transactionOptional.isEmpty()) {
            log.warn("RideConfirmationDelegate - no transaction with id {} found", transactionId);
            return;
        }

        Transaction transaction = transactionOptional.get();
        transaction.setProcessInstanceId(execution.getProcessInstanceId());
        transaction.setStatus(CONFIRMED_STATUS);

        log.info(
                "RideConfirmationDelegate - Confirming Ride {} for rider {}",
                transactionId,
                transaction.getExternalId());

        // confirm the ride in partner API
        rideService.submitReservation(
                SubmitReservationRequest.builder()
                        .id(transaction.getData().getProperty("promiseTimeId", String.class))
                        .build());

        transactionService.updateTransaction(transaction);
    }
}
