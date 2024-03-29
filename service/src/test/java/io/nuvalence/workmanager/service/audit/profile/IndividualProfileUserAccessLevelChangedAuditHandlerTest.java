package io.nuvalence.workmanager.service.audit.profile;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuvalence.workmanager.service.config.SpringConfig;
import io.nuvalence.workmanager.service.domain.profile.Individual;
import io.nuvalence.workmanager.service.domain.profile.IndividualUserLink;
import io.nuvalence.workmanager.service.domain.profile.ProfileAccessLevel;
import io.nuvalence.workmanager.service.models.auditevents.AuditActivityType;
import io.nuvalence.workmanager.service.models.auditevents.AuditEventBusinessObject;
import io.nuvalence.workmanager.service.service.AuditEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class IndividualProfileUserAccessLevelChangedAuditHandlerTest {
    @Mock private AuditEventService auditEventService;
    @InjectMocks private IndividualProfileUserAccessLevelChangedAuditHandler auditHandler;

    @Test
    void test_publishAuditEvent_WithChanges() throws JsonProcessingException {
        UUID ownerUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ProfileAccessLevel accessLevelOld = ProfileAccessLevel.READER;
        Individual individual =
                Individual.builder().id(UUID.randomUUID()).ownerUserId(ownerUserId).build();

        IndividualUserLink individualUserLink =
                IndividualUserLink.builder()
                        .id(UUID.randomUUID())
                        .profile(individual)
                        .userId(userId)
                        .accessLevel(accessLevelOld)
                        .build();

        auditHandler.handlePreUpdateState(individualUserLink);

        ProfileAccessLevel profileAccessLevelNew = ProfileAccessLevel.WRITER;
        individualUserLink.setAccessLevel(profileAccessLevelNew);
        auditHandler.handlePostUpdateState(individualUserLink);

        String originatorId = "originatorId";
        auditHandler.publishAuditEvent(originatorId);

        Map<String, String> before = new HashMap<>();
        before.put("accessLevel", accessLevelOld.toString());

        Map<String, String> after = new HashMap<>();
        after.put("accessLevel", profileAccessLevelNew.toString());

        Map<String, Object> eventData =
                Map.of(
                        "ownerUserId", ownerUserId,
                        "userId", userId);
        String eventDataJson = SpringConfig.getMapper().writeValueAsString(eventData);

        verify(auditEventService)
                .sendStateChangeEvent(
                        originatorId,
                        originatorId,
                        String.format(
                                "Profile user access level changed to [%s] for individual profile"
                                        + " user %s owned by %s. Previously it was [%s]",
                                profileAccessLevelNew, userId, ownerUserId, accessLevelOld),
                        userId,
                        AuditEventBusinessObject.INDIVIDUAL,
                        before,
                        after,
                        eventDataJson,
                        AuditActivityType.INDIVIDUAL_PROFILE_USER_ACCESS_LEVEL_CHANGED.getValue());
    }

    @Test
    void test_publishAuditEvent_WithNoChanges() {
        IndividualUserLink individualUserLink =
                IndividualUserLink.builder()
                        .profile(Individual.builder().id(UUID.randomUUID()).build())
                        .userId(UUID.randomUUID())
                        .accessLevel(ProfileAccessLevel.READER)
                        .build();
        auditHandler.handlePreUpdateState(individualUserLink);
        auditHandler.handlePostUpdateState(individualUserLink);

        String originatorId = "originatorId";
        auditHandler.publishAuditEvent(originatorId);

        verifyNoInteractions(auditEventService);
    }
}
