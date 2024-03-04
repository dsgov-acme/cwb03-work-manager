package io.nuvalence.workmanager.service.controllers;

import static java.util.stream.Collectors.groupingBy;

import io.nuvalence.workmanager.service.domain.record.Record;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.generated.controllers.DfcxApiDelegate;
import io.nuvalence.workmanager.service.generated.models.DialogflowSessionInfo;
import io.nuvalence.workmanager.service.generated.models.DialogflowWebhookRequest;
import io.nuvalence.workmanager.service.generated.models.DialogflowWebhookResponse;
import io.nuvalence.workmanager.service.mapper.DialogflowEntityMapper;
import io.nuvalence.workmanager.service.mapper.RideMapper;
import io.nuvalence.workmanager.service.models.RecordFilters;
import io.nuvalence.workmanager.service.models.SearchTransactionsFilters;
import io.nuvalence.workmanager.service.models.mta.DailyRideSummary;
import io.nuvalence.workmanager.service.models.mta.RideSummary;
import io.nuvalence.workmanager.service.service.RecordService;
import io.nuvalence.workmanager.service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DfcxApiDelegateImpl implements DfcxApiDelegate {
    private static final String RIDER_RECORD_DEFINITION_KEY = "MTARider";

    private final RecordService recordService;
    private final TransactionService transactionService;
    private final RideMapper rideMapper;
    private final DialogflowEntityMapper dialogflowEntityMapper;

    @Override
    public ResponseEntity<DialogflowWebhookResponse> action(
            DialogflowWebhookRequest webhookRequest) {
        String tag = webhookRequest.getFulfillmentInfo().getTag();

        return switch (tag) {
            case "rider-lookup" -> riderLookup(webhookRequest);
            case "load-scheduled-rides" -> getRiderScheduledRides(webhookRequest);
            default -> ResponseEntity.status(404).build();
        };
    }

    private ResponseEntity<DialogflowWebhookResponse> riderLookup(
            DialogflowWebhookRequest webhookRequest) {
        final Map<String, Object> parametersIn = getRequestParameters(webhookRequest);
        final Map<String, Object> parametersOut = new HashMap<>();

        boolean riderFound = false;

        if (parametersIn.containsKey("$flow.rider-id")) {
            String riderId = parametersIn.get("$flow.rider-id").toString();
            final RecordFilters filters =
                    new RecordFilters(
                            RIDER_RECORD_DEFINITION_KEY,
                            List.of(),
                            riderId,
                            true,
                            "externalId",
                            "ASC",
                            0,
                            10);

            final Optional<Record> foundRider =
                    recordService.getRecordsByFilters(filters).stream().findFirst();
            if (foundRider.isPresent()) {
                riderFound = true;
                parametersOut.put("user-id", foundRider.get().getSubjectUserId());
                parametersOut.put("rider-name", foundRider.get().getData().get("fullName"));
                parametersOut.put("rider-id", foundRider.get().getData().get("id"));
            }
        }

        parametersOut.put("$request.rider-found", riderFound);

        DialogflowWebhookResponse response =
                new DialogflowWebhookResponse()
                        .sessionInfo(new DialogflowSessionInfo().parameters(parametersOut));

        return ResponseEntity.status(riderFound ? 200 : 404).body(response);
    }

    private ResponseEntity<DialogflowWebhookResponse> getRiderScheduledRides(
            DialogflowWebhookRequest webhookRequest) {
        final Map<String, Object> parametersIn = getRequestParameters(webhookRequest);
        final Map<String, Object> parametersOut = new HashMap<>();
        final String riderUserId = parametersIn.get("rider-user-id").toString();

        if (!StringUtils.isEmpty(riderUserId)) {
            SearchTransactionsFilters filters =
                    SearchTransactionsFilters.builder()
                            .transactionDefinitionKeys(List.of("MTAReservation"))
                            .status(List.of("CONFIRMED"))
                            .subjectUserId(riderUserId)
                            .sortBy("createdTimestamp")
                            .sortOrder("ASC")
                            .pageNumber(0)
                            .pageSize(99)
                            .build();

            Page<Transaction> pagedTransactions =
                    transactionService.getFilteredTransactions(filters);
            if (!pagedTransactions.isEmpty()) {
                List<DailyRideSummary> dailySummaries =
                        pagedTransactions.stream()
                                .map(t -> rideMapper.mapEntityToRideSummary(t.getData()))
                                .collect(groupingBy(r -> toSortableDate(r.getPickup())))
                                .entrySet()
                                .stream()
                                .map(this::toDailyRideSummary)
                                .toList();
                parametersOut.put("$flow.scheduled-rides", dailySummaries);
            } else {
                parametersOut.put("$flow.scheduled-rides", List.of());
            }
        } else {
            parametersOut.put("$flow.scheduled-rides", List.of());
        }

        DialogflowWebhookResponse response =
                new DialogflowWebhookResponse()
                        .sessionInfo(new DialogflowSessionInfo().parameters(parametersOut));

        return ResponseEntity.status(200).body(response);
    }

    private DailyRideSummary toDailyRideSummary(Map.Entry<String, List<RideSummary>> rides) {
        try {
            return DailyRideSummary.builder()
                    .formattedDate(rides.getKey())
                    .date(dialogflowEntityMapper.mapStringToSysDate(rides.getKey()))
                    .rides(rides.getValue())
                    .build();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String toSortableDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ROOT);
        return sdf.format(date);
    }

    private Map<String, Object> getRequestParameters(DialogflowWebhookRequest webhookRequest) {
        return webhookRequest != null
                        && webhookRequest.getSessionInfo() != null
                        && webhookRequest.getSessionInfo().getParameters() != null
                ? webhookRequest.getSessionInfo().getParameters()
                : new HashMap<>();
    }
}
