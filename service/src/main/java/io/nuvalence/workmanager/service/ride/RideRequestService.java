package io.nuvalence.workmanager.service.ride;

import io.nuvalence.workmanager.service.ride.models.PromiseTimeRequest;
import io.nuvalence.workmanager.service.ride.models.PromiseTimeResponse;
import io.nuvalence.workmanager.service.ride.models.ReservationDetailsRequest;
import io.nuvalence.workmanager.service.ride.models.ReservationDetailsResponse;
import io.nuvalence.workmanager.service.ride.models.SubmitReservationRequest;

import java.util.Optional;

/**
 * Service for ride requests.
 */
public interface RideRequestService {
    /**
     * Gets promise times with the provided criteria.
     *
     * @param request the criteria
     * @return PromiseTimeResponse
     */
    Optional<PromiseTimeResponse> getPromiseTimes(PromiseTimeRequest request);

    /**
     * Submits the reservation request.
     *
     * @param request the request
     */
    void submitReservation(SubmitReservationRequest request);

    /**
     * Retrieves the reservation details.
     *
     * @param request the request
     * @return ReservationDetailsResponse
     */
    Optional<ReservationDetailsResponse> getReservationDetails(ReservationDetailsRequest request);
}
