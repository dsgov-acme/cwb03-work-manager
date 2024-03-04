package io.nuvalence.workmanager.service.models.dfcx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysTime {
    private int hours;
    private int minutes;
    private int seconds;
    private int nanos;
}
