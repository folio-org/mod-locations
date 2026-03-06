package org.folio.locations.mapper;

import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.entity.CampusEntity;
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
public interface CampusMapper extends EntityMapper<Campus, CampusEntity> {

  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  CampusEntity toEntity(Campus dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  void updateEntity(Campus dto, @MappingTarget CampusEntity entity);

  @Mapping(target = "metadata", source = "entity", qualifiedByName = "toMetadata")
  Campus toDto(CampusEntity entity);

  @Named("toMetadata")
  default Metadata toMetadata(CampusEntity entity) {
    if (entity.getCreatedDate() == null) {
      return null;
    }
    return new Metadata(entity.getCreatedDate(), entity.getCreatedByUserId())
      .updatedDate(entity.getUpdatedDate())
      .updatedByUserId(entity.getUpdatedByUserId());
  }
}
