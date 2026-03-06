package org.folio.locations.mapper;

import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.domain.entity.ServicePointUserEntity;
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
public interface ServicePointUserMapper {

  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  ServicePointUserEntity toEntity(ServicePointsUser dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  void updateEntity(ServicePointsUser dto, @MappingTarget ServicePointUserEntity entity);

  @Mapping(target = "metadata", source = "entity", qualifiedByName = "toMetadata")
  ServicePointsUser toDto(ServicePointUserEntity entity);

  @Named("toMetadata")
  default Metadata toMetadata(ServicePointUserEntity entity) {
    if (entity.getCreatedDate() == null) {
      return null;
    }
    return new Metadata(entity.getCreatedDate(), entity.getCreatedByUserId())
      .updatedDate(entity.getUpdatedDate())
      .updatedByUserId(entity.getUpdatedByUserId());
  }
}
