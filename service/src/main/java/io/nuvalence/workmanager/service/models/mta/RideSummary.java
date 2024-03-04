package io.nuvalence.workmanager.service.models.mta;

import io.nuvalence.workmanager.service.models.dfcx.SysDate;
import io.nuvalence.workmanager.service.models.dfcx.SysTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideSummary {
    private String id;
    private Date pickup;
    private SysDate pickupDate;
    private SysTime pickupTime;
    private SysTime pickupTimeMax;
    private SysTime actualPickupTime;
    private int delayMinutes;
    private int minutesAway;
    private Date dropoff;
    private SysDate dropoffDate;
    private SysTime dropoffTime;
    private SysTime dropoffTimeMax;
    private SysTime actualDropoffTime;
    private String pickupLocation;
    private String dropoffLocation;
    private String status;
    private String driverName;
    private String driverVehicle;
}
