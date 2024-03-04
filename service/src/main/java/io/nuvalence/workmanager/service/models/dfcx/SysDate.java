package io.nuvalence.workmanager.service.models.dfcx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysDate {
    private int day;
    private int month;
    private int year;
}
