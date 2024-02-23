package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.generated.models.MTALocation;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@AllArgsConstructor
@NoArgsConstructor
public abstract class SavedLocationMapper {

    @Setter protected EntityMapper entityMapper;

    public abstract io.nuvalence.workmanager.service.ride.models.MTALocation toEntity(MTALocation dto);

    public abstract MTALocation toDto(io.nuvalence.workmanager.service.ride.models.MTALocation dto);

}
