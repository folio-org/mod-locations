package org.folio.locations.mapper;

import java.util.Map;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.entity.LocationEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
  componentModel = MappingConstants.ComponentModel.SPRING,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  builder = @Builder(disableBuilder = true)
)
public interface LocationMapper extends EntityMapper<Location, LocationEntity> {

  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  LocationEntity toEntity(Location dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  void updateEntity(Location dto, @MappingTarget LocationEntity entity);

  @Mapping(target = "metadata", source = "entity", qualifiedByName = "toMetadata")
  @Mapping(target = "details", source = "details", qualifiedByName = "toDetails")
  Location toDto(LocationEntity entity);

  @Named("toMetadata")
  default Metadata toMetadata(LocationEntity entity) {
    if (entity.getCreatedDate() == null) {
      return null;
    }
    return new Metadata(entity.getCreatedDate(), entity.getCreatedByUserId())
      .updatedDate(entity.getUpdatedDate())
      .updatedByUserId(entity.getUpdatedByUserId());
  }

  @Named("toDetails")
  default Map<String, Object> toDetails(Map<String, Object> details) {
    return details != null ? details : Map.of();
  }
}
