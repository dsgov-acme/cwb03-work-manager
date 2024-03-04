package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.dynamicschema.DynamicEntity;
import io.nuvalence.workmanager.service.models.mta.RideSummary;
import io.nuvalence.workmanager.service.ride.models.MTALocation;
import io.nuvalence.workmanager.service.ride.models.MTALocationType;
import lombok.Setter;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

/**
 * Mapper for MTA ride entities.
 */
@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class RideMapper {
    @Autowired @Setter private DialogflowEntityMapper dialogflowEntityMapper;

    public RideSummary mapEntityToRideSummary(DynamicEntity entity) {
        MTALocation pickLocation = entity.getProperty("pickLocation", MTALocation.class);
        MTALocation dropLocation = entity.getProperty("dropLocation", MTALocation.class);
        long pickupTime = entity.getProperty("promiseTime.pickupTime", long.class);
        long dropoffTime = entity.getProperty("dropTime", long.class);

        RideSummary ride = RideSummary.builder()
                .dropoff(new Date(pickupTime * 1000))
                .pickup(new Date(dropoffTime * 1000))
                .pickupLocation(getAddressLabel(pickLocation))
                .dropoffLocation(getAddressLabel(dropLocation))
                .status("scheduled")
                .driverName("John Smith")
                .driverVehicle("Mazda 3")
                .build();
        populateDialogflowPropertiesOnRideSummary(ride);
        return ride;
    }

    private void populateDialogflowPropertiesOnRideSummary(RideSummary ride) {
        ride.setPickupDate(dialogflowEntityMapper.mapDateToSysDate(ride.getPickup()));
        ride.setPickupTime(dialogflowEntityMapper.mapDateToSysTime(ride.getPickup()));
        ride.setPickupTimeMax(dialogflowEntityMapper.mapDateToSysTime(
                addMinutesToDate(ride.getPickup(), 30)
        ));
        ride.setDropoffDate(dialogflowEntityMapper.mapDateToSysDate(ride.getDropoff()));
        ride.setDropoffTime(dialogflowEntityMapper.mapDateToSysTime(ride.getDropoff()));
        ride.setDropoffTimeMax(dialogflowEntityMapper.mapDateToSysTime(
                addMinutesToDate(ride.getDropoff(), 30)
        ));
    }

    private Date addMinutesToDate(Date date, int minutesToAdd) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        calendar.add(Calendar.MINUTE, minutesToAdd);
        return calendar.getTime();
    }

    private String getAddressLabel(MTALocation location) {
        if (location.getLocationType() == MTALocationType.SAVED_LOCATION) {
            return location.getName();
        }

        return String.format("%s %s",
                location.getAddress().getAddressLine1(),
                Optional.ofNullable(location.getAddress().getAddressLine2()).orElse("")
        ).trim();
    }
}
