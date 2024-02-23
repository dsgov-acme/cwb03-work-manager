package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.record.Record;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Records.
 */
public interface RecordRepository
        extends CrudRepository<Record, UUID>, JpaSpecificationExecutor<Record> {

    @Query(value = "SELECT nextval('record_sequence')", nativeQuery = true)
    Long getNextTransactionSequenceValue();

    @Modifying
    @Query(
            "UPDATE Record r SET r.status = 'Expired' WHERE r.expires < CURRENT_TIMESTAMP AND"
                    + " r.status <> 'Expired'")
    int updateStatusForExpiredRecords();

    @Query(
            "SELECT r.status, COUNT(r) FROM Record r WHERE r.recordDefinitionKey = ?1 GROUP BY"
                    + " r.status")
    List<Object[]> getStatusCountByRecordDefinitionKey(String recordDefinitionKey);

    Record findBySubjectUserId(String subjectUserId);
}
