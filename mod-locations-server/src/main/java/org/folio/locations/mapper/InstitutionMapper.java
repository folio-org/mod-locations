package org.folio.locations.mapper;

import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.entity.InstitutionEntity;
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
public interface InstitutionMapper extends EntityMapper<Institution, InstitutionEntity> {

  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  InstitutionEntity toEntity(Institution dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  void updateEntity(Institution dto, @MappingTarget InstitutionEntity entity);

  @Mapping(target = "metadata", source = "entity", qualifiedByName = "toMetadata")
  Institution toDto(InstitutionEntity entity);

  @Named("toMetadata")
  default Metadata toMetadata(InstitutionEntity entity) {
    if (entity.getCreatedDate() == null) {
      return null;
    }
    return new Metadata(entity.getCreatedDate(), entity.getCreatedByUserId())
      .updatedDate(entity.getUpdatedDate())
      .updatedByUserId(entity.getUpdatedByUserId());
  }
}
