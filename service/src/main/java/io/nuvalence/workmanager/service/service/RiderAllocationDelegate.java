package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.record.Record;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component("RiderAllocationDelegate")
public class RiderAllocationDelegate implements JavaDelegate {

    private final TransactionService transactionService;
    private final RecordService recordService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        UUID transactionId = (UUID) execution.getVariable("transactionId");

        if (transactionId == null) {
            log.warn("RiderAllocationDelegate - transactionId not found");
            return;
        }
        Optional<Transaction> transactionOptional =
                transactionService.getTransactionById(transactionId);

        if (transactionOptional.isEmpty()) {
            log.warn("RiderAllocationDelegate - no transaction with id {} found", transactionId);
            return;
        }

        Transaction transaction = transactionOptional.get();
        transaction.setProcessInstanceId(execution.getProcessInstanceId());

        String riderId = null;

        try {
            riderId = transaction.getData().getProperty("rider.id", String.class);
        } catch (IllegalArgumentException e) {
            log.info("Transaction does not contain riderId info.  Continuing.");
        }

        if (riderId == null) {
            // attempt to look up the riderId by the subjectUserId in the transaction if it was not
            // set
            riderId =
                    Optional.ofNullable(transaction.getSubjectUserId())
                            .map(recordService::getRecordBySubjectUserId)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(Record::getExternalId)
                            .orElse(null);
        }

        String riderUserId =
                Optional.ofNullable(riderId)
                        .map(recordService::getRecordByExternalId)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(Record::getSubjectUserId)
                        .orElse(null);

        log.info(
                "RiderAllocationDelegate - Assigning riderId {} and subjectUserId {} to transaction"
                        + " {}",
                riderId,
                riderUserId,
                transactionId);

        transaction.setExternalId(riderId);
        if (riderUserId != null) {
            try {
                transaction.setSubjectProfileId(UUID.fromString(riderUserId));
            } catch (IllegalArgumentException e) {
                log.error(
                        "Unable to assign subjectUserId of {} to transaction {}",
                        riderId,
                        transactionId,
                        e);
            }
        }

        transactionService.updateTransaction(transaction);
    }
}
