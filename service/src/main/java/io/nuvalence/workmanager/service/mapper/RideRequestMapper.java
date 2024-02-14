package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.generated.models.PromiseTimeRequest;
import io.nuvalence.workmanager.service.generated.models.ReservationDetailsRequest;
import io.nuvalence.workmanager.service.generated.models.SubmitReservationRequest;
import io.nuvalence.workmanager.service.ride.models.PromiseTimeResponse;
import io.nuvalence.workmanager.service.ride.models.ReservationDetailsResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@AllArgsConstructor
@NoArgsConstructor
public abstract class RideRequestMapper {

    @Setter protected EntityMapper entityMapper;


    public abstract io.nuvalence.workmanager.service.ride.models.ReservationDetailsRequest toEntity(ReservationDetailsRequest dto);

    public abstract io.nuvalence.workmanager.service.ride.models.SubmitReservationRequest toEntity(SubmitReservationRequest dto);

    public abstract io.nuvalence.workmanager.service.ride.models.PromiseTimeRequest toEntity(PromiseTimeRequest dto);

    public abstract io.nuvalence.workmanager.service.generated.models.ReservationDetailsResponse toDto(ReservationDetailsResponse entity);

    public abstract io.nuvalence.workmanager.service.generated.models.PromiseTimeResponse toDto(PromiseTimeResponse entity);
}
