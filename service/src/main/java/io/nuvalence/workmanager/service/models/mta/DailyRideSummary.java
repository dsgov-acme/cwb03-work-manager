package io.nuvalence.workmanager.service.models.mta;

import io.nuvalence.workmanager.service.models.dfcx.SysDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyRideSummary {
    private String formattedDate;
    private SysDate date;
    private List<RideSummary> rides;
}
