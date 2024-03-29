package io.nuvalence.workmanager.service.service;

import io.micrometer.common.util.StringUtils;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.record.Record;
import io.nuvalence.workmanager.service.domain.record.RecordDefinition;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.generated.models.RecordCreationRequest;
import io.nuvalence.workmanager.service.generated.models.RecordUpdateRequest;
import io.nuvalence.workmanager.service.mapper.EntityMapper;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.models.RecordFilters;
import io.nuvalence.workmanager.service.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;

/**
 * Service for managing records.
 */
@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RecordService {
    private final RecordRepository repository;
    private final RecordFactory factory;
    private final TransactionService transactionService;
    private final EntityMapper entityMapper;

    /**
     * Create a new Record for a given record definition.
     *
     * @param definition Type of record to create
     * @param transaction The transaction to create the record from
     * @param request The data to populate the record with initially.
     *
     * @return The newly created record
     * @throws MissingSchemaException if the record definition references a schema that does not exist
     */
    public Record createRecord(
            RecordDefinition definition, Transaction transaction, RecordCreationRequest request)
            throws MissingSchemaException {
        return repository.save(factory.createRecord(definition, transaction, request));
    }

    public Optional<Record> getRecordById(final UUID id) {
        return repository.findById(id);
    }

    public Optional<Record> getRecordBySubjectUserId(final String subjectUserId) {
        return Optional.ofNullable(repository.findBySubjectUserId(subjectUserId));
    }

    public Optional<Record> getRecordByExternalId(final String externalId) {
        return Optional.ofNullable(repository.findByExternalId(externalId));
    }

    public Page<Record> getRecordsByFilters(RecordFilters filter) {

        return repository.findAll(filter.getRecordSpecification(), filter.getPageRequest());
    }

    public List<Record> getRecordByEmailDataField(String email) {
        return repository.getRecordByEmail(email);
    }

    /**
     * Update a record.
     *
     * @param updateRequest the update request
     * @param existingRecord the existing record
     * @return the record
     */
    public Record updateRecord(RecordUpdateRequest updateRequest, Record existingRecord) {

        if (!StringUtils.isBlank(updateRequest.getStatus())) {
            existingRecord.setStatus(updateRequest.getStatus());
        }

        if (!StringUtils.isBlank(String.valueOf(updateRequest.getExpires()))) {
            existingRecord.setExpires(updateRequest.getExpires());
        }

        if (!StringUtils.isBlank(updateRequest.getSubjectUserId())) {
            existingRecord.setSubjectUserId(updateRequest.getSubjectUserId());
        }

        if (updateRequest.getData() != null) {
            final Map<String, Object> mergedMap =
                    transactionService.unifyAttributeMaps(
                            updateRequest.getData(),
                            entityMapper.convertAttributesToGenericMap(existingRecord.getData()));

            final Schema schema = existingRecord.getData().getSchema();
            try {
                existingRecord.setData(entityMapper.convertGenericMapToEntity(schema, mergedMap));
            } catch (MissingSchemaException e) {
                log.error(
                        String.format(
                                "Record [%s] references missing schema.", existingRecord.getId()));
            }
        }

        return repository.save(existingRecord);
    }

    /**
     * Check if the record has admin data changes.
     *
     * @param originalRecord the original record
     * @param status the status
     * @param expires the expires
     * @return true if the record has admin data changes
     */
    public boolean hasAdminDataChanges(Record originalRecord, String status, String expires) {

        if (status != null && !status.equals(originalRecord.getStatus())) {
            return true;
        }

        return expires != null && !expires.equals(originalRecord.getExpires().toString());
    }

    public int expireRecords() {
        return repository.updateStatusForExpiredRecords();
    }
}
