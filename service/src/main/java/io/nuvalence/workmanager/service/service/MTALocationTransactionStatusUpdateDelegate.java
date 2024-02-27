package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.dynamicschema.DynamicEntity;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.models.CommonAddress;
import io.nuvalence.workmanager.service.ride.models.MTALocation;
import io.nuvalence.workmanager.service.utils.camunda.CamundaPropertiesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component("MTALocationTransactionStatusUpdateDelegate")
public class MTALocationTransactionStatusUpdateDelegate implements JavaDelegate {

    private final TransactionService transactionService;
    private final GeocodingService geocodingService;
    private final String DEFAULT_STATUS = "Draft";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        UUID transactionId = (UUID) execution.getVariable("transactionId");

        if (transactionId == null) {
            log.warn("MTALocationTransactionStatusUpdateDelegate - transactionId not found");
            return;
        }

        String status = DEFAULT_STATUS;
        Optional<String> statusOptional =
                CamundaPropertiesUtils.getExtensionProperty("status", execution);
        if (statusOptional.isPresent()) {
            status = statusOptional.get();
        } else {
            log.warn(
                    "MTALocationTransactionStatusUpdateDelegate - status not found, the default"
                            + " status will be set: {}",
                    DEFAULT_STATUS);
        }
        updateTransactionStatusAndLocation(transactionId, status, execution);
    }

    private void updateTransactionStatusAndLocation(
            UUID transactionId, String status, DelegateExecution execution) {
        Optional<Transaction> transactionOptional =
                transactionService.getTransactionById(transactionId);
        if (transactionOptional.isPresent()) {
            Transaction transaction = transactionOptional.get();
            transaction.setStatus(status);
            transaction.setProcessInstanceId(execution.getProcessInstanceId());
            updateTransactionLocation(transaction.getData());

            transactionService.updateTransaction(transaction);
        }
    }

    private void updateTransactionLocation(DynamicEntity transactionEntity) {
        CommonAddress address = (CommonAddress) transactionEntity.get("address");

        MTALocation location = geocodingService.geocodeLocation(address);

        //        transactionEntity.set("latitude", location.getLatitude());
        //        transactionEntity.set("longitude", location.getLongitude());
    }
}
