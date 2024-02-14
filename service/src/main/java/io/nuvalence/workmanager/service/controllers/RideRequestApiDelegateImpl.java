package io.nuvalence.workmanager.service.controllers;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.workmanager.service.generated.controllers.RideRequestApiDelegate;
import io.nuvalence.workmanager.service.generated.models.PromiseTimeRequest;
import io.nuvalence.workmanager.service.generated.models.PromiseTimeResponse;
import io.nuvalence.workmanager.service.generated.models.ReservationDetailsRequest;
import io.nuvalence.workmanager.service.generated.models.ReservationDetailsResponse;
import io.nuvalence.workmanager.service.generated.models.SubmitReservationRequest;
import io.nuvalence.workmanager.service.mapper.RideRequestMapper;
import io.nuvalence.workmanager.service.ride.RideRequestService;
import io.nuvalence.workmanager.service.ride.models.PromiseTime;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideRequestApiDelegateImpl implements RideRequestApiDelegate {

    private final AuthorizationHandler authorizationHandler;
    private final RideRequestService rideRequestService;
    private final RideRequestMapper rideRequestMapper;

    @Override
    public ResponseEntity<PromiseTimeResponse> getPromiseTimes(PromiseTimeRequest promiseTimeRequest) {
//        if (!authorizationHandler.isAllowed("view", PromiseTime.class)) {
//            throw new ForbiddenException();
//        }
        return Optional.ofNullable(promiseTimeRequest)
                .map(rideRequestMapper::toEntity)
                .map(rideRequestService::getPromiseTimes)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(rideRequestMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<ReservationDetailsResponse> submitReservation(SubmitReservationRequest submitReservationRequest) {
        // TODO: revisit permissions
        if (!authorizationHandler.isAllowed("update", PromiseTime.class)) {
            throw new ForbiddenException();
        }
        return Optional.ofNullable(submitReservationRequest)
                .map(rideRequestMapper::toEntity)
                .map(rideRequestService::submitReservation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(rideRequestMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @Override
    public ResponseEntity<ReservationDetailsResponse> getReservationDetails(ReservationDetailsRequest reservationDetailsRequest) {
        if (!authorizationHandler.isAllowed("view", PromiseTime.class)) {
            throw new ForbiddenException();
        }

        return Optional.ofNullable(reservationDetailsRequest)
                .map(rideRequestMapper::toEntity)
                .map(rideRequestService::getReservationDetails)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(rideRequestMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
