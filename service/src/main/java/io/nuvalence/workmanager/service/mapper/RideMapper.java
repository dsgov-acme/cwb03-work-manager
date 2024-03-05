package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.dynamicschema.DynamicEntity;
import io.nuvalence.workmanager.service.models.CommonAddress;
import io.nuvalence.workmanager.service.models.mta.RideStatusEnum;
import io.nuvalence.workmanager.service.models.mta.RideSummary;
import io.nuvalence.workmanager.service.ride.models.MTALocation;
import io.nuvalence.workmanager.service.ride.models.MTALocationType;
import lombok.Setter;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.*;

/**
 * Mapper for MTA ride entities.
 */
@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class RideMapper {
    @Autowired @Setter private DialogflowEntityMapper dialogflowEntityMapper;

    public MTALocation mapEntityToMTALocation(DynamicEntity entity) {
        String placeId = entity.getProperty("placeId", String.class);
        String name = entity.getProperty("name", String.class);
        String riderId = entity.getProperty("riderId", String.class);
        MTALocationType locationType =
                entity.get("locationType") != null
                        ? MTALocationType.valueOf(entity.get("locationType").toString())
                        : null;

        String address1 = entity.getProperty("address.addressLine1", String.class);
        String address2 = entity.getProperty("address.addressLine2", String.class);
        String city = entity.getProperty("address.city", String.class);
        String stateCode = entity.getProperty("address.stateCode", String.class);
        String postalCode = entity.getProperty("address.postalCode", String.class);
        String postalCodeExtension =
                entity.getProperty("address.postalCodeExtension", String.class);
        String countryCode = entity.getProperty("address.countryCode", String.class);

        MTALocation location = new MTALocation();
        location.setPlaceId(placeId);
        location.setName(name);
        location.setRiderId(riderId);
        location.setLocationType(locationType);

        CommonAddress commonAddress = new CommonAddress();
        commonAddress.setAddressLine1(address1);
        commonAddress.setAddressLine2(address2);
        commonAddress.setCity(city);
        commonAddress.setStateCode(stateCode);
        commonAddress.setPostalCode(postalCode);
        commonAddress.setPostalCodeExtension(postalCodeExtension);
        commonAddress.setCountryCode(countryCode);

        // Set the address for the location
        location.setAddress(commonAddress);

        return location;
    }

    public RideSummary mapEntityToRideSummary(DynamicEntity entity) {
        MTALocation pickLocation =
                mapEntityToMTALocation((DynamicEntity) entity.get("pickLocation"));
        MTALocation dropLocation =
                mapEntityToMTALocation((DynamicEntity) entity.get("dropLocation"));
        long pickupTime = entity.getProperty("promiseTime.pickupTime", Integer.class);
        long dropoffTime = entity.getProperty("promiseTime.dropTime", Integer.class);
        RideStatusEnum status = RideStatusEnum.SCHEDULED;

        Date pickupDate = getUtcDateFromSecondsSinceEpoch(pickupTime);
        Date dropoffDate = getUtcDateFromSecondsSinceEpoch(dropoffTime);

        Instant now = Instant.now();
        if (now.isAfter(dropoffDate.toInstant())) {
            status = RideStatusEnum.COMPLETED;
        } else if (now.isAfter(pickupDate.toInstant()) && now.isBefore(dropoffDate.toInstant())) {
            status = RideStatusEnum.IN_PROGRESS;
        }

        RideSummary ride =
                RideSummary.builder()
                        .pickup(pickupDate)
                        .dropoff(dropoffDate)
                        .pickupLocation(getAddressLabel(pickLocation))
                        .dropoffLocation(getAddressLabel(dropLocation))
                        .status(status)
                        .driverName("John Smith")
                        .driverVehicle("Mazda 3")
                        .build();
        populateDialogflowPropertiesOnRideSummary(ride);
        return ride;
    }

    private Date getUtcDateFromSecondsSinceEpoch(long secondsSinceEpoch) {
        Date date = new Date(secondsSinceEpoch * 1000);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        return calendar.getTime();
    }

    private void populateDialogflowPropertiesOnRideSummary(RideSummary ride) {
        ride.setPickupDate(dialogflowEntityMapper.mapDateToSysDate(ride.getPickup(), false));
        ride.setPickupTime(dialogflowEntityMapper.mapDateToSysTime(ride.getPickup()));
        ride.setPickupTimeMax(
                dialogflowEntityMapper.mapDateToSysTime(addMinutesToDate(ride.getPickup(), 30)));
        ride.setDropoffDate(dialogflowEntityMapper.mapDateToSysDate(ride.getDropoff(), false));
        ride.setDropoffTime(dialogflowEntityMapper.mapDateToSysTime(ride.getDropoff()));
        ride.setDropoffTimeMax(
                dialogflowEntityMapper.mapDateToSysTime(addMinutesToDate(ride.getDropoff(), 30)));
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

        return String.format(
                        "%s %s",
                        location.getAddress().getAddressLine1(),
                        Optional.ofNullable(location.getAddress().getAddressLine2()).orElse(""))
                .trim();
    }
}
