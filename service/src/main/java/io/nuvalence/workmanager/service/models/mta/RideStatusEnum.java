package io.nuvalence.workmanager.service.models.mta;

import lombok.Getter;

public enum RideStatusEnum {
    SCHEDULED("SCHEDULED"),
    DELAYED("DELAYED"),
    IN_PROGRESS("IN_PROGRESS"),
    IN_PROGRESS_DELAYED("IN_PROGRESS_DELAYED"),
    COMPLETED("COMPLETED");

    @Getter private String value;

    RideStatusEnum(String value) {
        this.value = value;
    }
}
