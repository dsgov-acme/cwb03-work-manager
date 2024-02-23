package io.nuvalence.workmanager.service.ride;

import io.nuvalence.workmanager.service.ride.models.AnchorType;
import io.nuvalence.workmanager.service.ride.models.PassengerAccommodations;
import io.nuvalence.workmanager.service.ride.models.PromiseTime;
import io.nuvalence.workmanager.service.ride.models.PromiseTimeRequest;
import io.nuvalence.workmanager.service.ride.models.PromiseTimeResponse;
import io.nuvalence.workmanager.service.ride.models.ReservationDetailsAnchor;
import io.nuvalence.workmanager.service.ride.models.ReservationDetailsRequest;
import io.nuvalence.workmanager.service.ride.models.ReservationDetailsResponse;
import io.nuvalence.workmanager.service.ride.models.SubmitReservationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

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
        // TODO: if the request contains placeIds, they can be used to retrieve the promise times.
        // if the request contains UUIDs, e.g. IDs of saved locations, we should look up the placeId of the saved location first.
        return Optional.ofNullable(
                PromiseTimeResponse.builder()
                        .anchor(AnchorType.DROPOFF)
                        .promises(
                                new PromiseTime[] {
                                    PromiseTime.builder()
                                            .id("PROMISE_TIME_1")
                                            .pickupTime(
                                                    adjustDateAndGetInSecondsSinceEpoch(
                                                            getTomorrow(), 8, 45))
                                            .dropTime(
                                                    adjustDateAndGetInSecondsSinceEpoch(
                                                            getTomorrow(), 9, 45))
                                            .route("ROUTE_1")
                                            .build(),
                                    PromiseTime.builder()
                                            .id("PROMISE_TIME_2")
                                            .pickupTime(
                                                    adjustDateAndGetInSecondsSinceEpoch(
                                                            getTomorrow(), 9, 0))
                                            .dropTime(
                                                    adjustDateAndGetInSecondsSinceEpoch(
                                                            getTomorrow(), 10, 0))
                                            .route("ROUTE_2")
                                            .build(),
                                    PromiseTime.builder()
                                            .id("PROMISE_TIME_3")
                                            .pickupTime(
                                                    adjustDateAndGetInSecondsSinceEpoch(
                                                            getTomorrow(), 9, 15))
                                            .dropTime(
                                                    adjustDateAndGetInSecondsSinceEpoch(
                                                            getTomorrow(), 10, 15))
                                            .route("ROUTE_3")
                                            .build()
                                })
                        .build());
    }

    /**
     * Submits the reservation request.
     *
     * @param request the request
     * @return ReservationDetailsResponse
     */
    @Override
    public Optional<ReservationDetailsResponse> submitReservation(
            SubmitReservationRequest request) {
        return getMockReservationDetails();
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
        return getMockReservationDetails();
    }

    private Optional<ReservationDetailsResponse> getMockReservationDetails() {
        return Optional.ofNullable(
                ReservationDetailsResponse.builder()
                        .id("RESERVATION_1")
                        .anchor(AnchorType.DROPOFF)
                        .requestTime(adjustDateAndGetInSecondsSinceEpoch(getTomorrow(), 10, 0))
                        .pickup(
                                ReservationDetailsAnchor.builder()
                                        .placeId("PLACE_1")
                                        .promisedTime(
                                                adjustDateAndGetInSecondsSinceEpoch(
                                                        getTomorrow(), 9, 0))
                                        .build())
                        .dropOff(
                                ReservationDetailsAnchor.builder()
                                        .placeId("PLACE_2")
                                        .promisedTime(
                                                adjustDateAndGetInSecondsSinceEpoch(
                                                        getTomorrow(), 10, 0))
                                        .build())
                        .passengerAccommodations(
                                PassengerAccommodations.builder()
                                        .ambulatorySeats(4)
                                        .wheelchairSeats(4)
                                        .companions(0)
                                        .build())
                        .route("ROUTE_2")
                        .build());
    }

    private long adjustDateAndGetInSecondsSinceEpoch(Date date, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        return c.getTimeInMillis() / 1000L;
    }

    private Date getTomorrow() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        return DateUtils.truncate(c.getTime(), Calendar.DATE);
    }
}
