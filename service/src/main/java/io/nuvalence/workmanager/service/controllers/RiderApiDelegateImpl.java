package io.nuvalence.workmanager.service.controllers;

import io.nuvalence.workmanager.service.domain.record.Record;
import io.nuvalence.workmanager.service.generated.controllers.RiderApiDelegate;
import io.nuvalence.workmanager.service.generated.models.DialogflowSessionInfo;
import io.nuvalence.workmanager.service.generated.models.DialogflowWebhookRequest;
import io.nuvalence.workmanager.service.generated.models.DialogflowWebhookResponse;
import io.nuvalence.workmanager.service.models.RecordFilters;
import io.nuvalence.workmanager.service.service.RecordService;
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
    private static final String RIDER_RECORD_DEFINITION_KEY = "MTARider";

    private final RecordService recordService;

    @Override
    public ResponseEntity<DialogflowWebhookResponse> validateRiderWebhook(DialogflowWebhookRequest webhookRequest) {
        final Map<String, Object> parametersIn = webhookRequest != null
                && webhookRequest.getSessionInfo() != null && webhookRequest.getSessionInfo().getParameters() != null
                ? webhookRequest.getSessionInfo().getParameters()
                : new HashMap<>();
        final Map<String, Object> parametersOut = new HashMap<>();

        boolean riderFound = false;

        if (parametersIn.containsKey("$flow.rider-id")) {
            String riderId = parametersIn.get("$flow.rider-id").toString();
            final RecordFilters filters = new RecordFilters(
                    RIDER_RECORD_DEFINITION_KEY,
                    List.of(),
                    riderId,
                    true,
                    "externalId", "ASC", 0, 10);

            final Optional<Record> foundRider = recordService.getRecordsByFilters(filters).stream().findFirst();
            if (foundRider.isPresent()) {
                riderFound = true;
                parametersOut.put("$request.rider-name", foundRider.get().getData().get("fullName"));
            }
        }

        parametersOut.put("$request.rider-found", riderFound);

        DialogflowWebhookResponse response = new DialogflowWebhookResponse()
                .sessionInfo(new DialogflowSessionInfo().parameters(parametersOut));

        return ResponseEntity.status(riderFound ? 200 : 404).body(response);
    }
}
