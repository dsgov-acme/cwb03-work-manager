package io.nuvalence.workmanager.service.ride;

import io.nuvalence.workmanager.service.ride.models.GeocodedLocation;
import io.nuvalence.workmanager.service.ride.models.PromiseTime;
import io.nuvalence.workmanager.service.ride.models.PromiseTimeRequest;
import io.nuvalence.workmanager.service.ride.models.PromiseTimeResponse;
import io.nuvalence.workmanager.service.ride.models.ReservationDetailsRequest;
import io.nuvalence.workmanager.service.ride.models.ReservationDetailsResponse;
import io.nuvalence.workmanager.service.ride.models.SubmitReservationRequest;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Mock service for ride requests.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MockRideRequestService implements RideRequestService {
    /**
     * Gets promise times with the provided criteria.
     *
     * @param request the criteria
     * @return PromiseTimeResponse
     */
    @Override
    public Optional<PromiseTimeResponse> getPromiseTimes(PromiseTimeRequest request) {
        return Optional.ofNullable(
                PromiseTimeResponse.builder()
                        .reservationId("RESERVATION_1")
                        .promiseTimes(
                                new PromiseTime[] {
                                    PromiseTime.builder()
                                            .id("PROMISE_TIME_1")
                                            .pickUpTimeUTC(
                                                    formatDate(adjustDate(getTomorrow(), 8, 45)))
                                            .pickUpTimeUTC(
                                                    formatDate(adjustDate(getTomorrow(), 9, 45)))
                                            .build(),
                                    PromiseTime.builder()
                                            .id("PROMISE_TIME_2")
                                            .pickUpTimeUTC(
                                                    formatDate(adjustDate(getTomorrow(), 9, 0)))
                                            .pickUpTimeUTC(
                                                    formatDate(adjustDate(getTomorrow(), 10, 0)))
                                            .build(),
                                    PromiseTime.builder()
                                            .id("PROMISE_TIME_3")
                                            .pickUpTimeUTC(
                                                    formatDate(adjustDate(getTomorrow(), 9, 15)))
                                            .pickUpTimeUTC(
                                                    formatDate(adjustDate(getTomorrow(), 10, 15)))
                                            .build()
                                })
                        .build());
    }

    /**
     * Submits the reservation request.
     *
     * @param request the request
     */
    @Override
    public void submitReservation(SubmitReservationRequest request) {
        // TODO: no work actually done
    }

    /**
     * Retrieves the reservation details.
     *
     * @param request the request
     * @return ReservationDetailsResponse
     */
    @Override
    public Optional<ReservationDetailsResponse> getReservationDetails(
            ReservationDetailsRequest request) {
        return Optional.ofNullable(
                ReservationDetailsResponse.builder()
                        .reservationId("RESERVATION_1")
                        .routeId("ROUTE_1")
                        .startLocation(
                                GeocodedLocation.builder() // times square
                                        .latitude(BigDecimal.valueOf(40.758896))
                                        .longitude(BigDecimal.valueOf(-73.985130))
                                        .build())
                        .endLocation(
                                GeocodedLocation.builder() // manhattan bridge
                                        .latitude(BigDecimal.valueOf(40.7075))
                                        .longitude(BigDecimal.valueOf(-73.9908))
                                        .build())
                        .build());
    }

    private Date adjustDate(Date date, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        return c.getTime();
    }

    private String formatDate(Date date) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.getDefault());
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    private Date getTomorrow() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        return DateUtils.truncate(c.getTime(), Calendar.DATE);
    }
}
