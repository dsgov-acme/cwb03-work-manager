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
        Optional<Transaction> transactionOptional = transactionService.getTransactionById(transactionId);

        if (transactionOptional.isEmpty()) {
            log.warn("RiderAllocationDelegate - no transaction with id {} found", transactionId);
            return;
        }

        Transaction transaction = transactionOptional.get();
        transaction.setProcessInstanceId(execution.getProcessInstanceId());

        String riderId = Optional.ofNullable(transaction.getSubjectUserId())
                .map(recordService::getRecordBySubjectUserId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Record::getExternalId)
                .orElse(null);

        log.info("RiderAllocationDelegate - Assigning riderId {} to transaction {}", riderId, transactionId);

        transaction.setExternalId(riderId);

        transactionService.updateTransaction(transaction);
    }
}
