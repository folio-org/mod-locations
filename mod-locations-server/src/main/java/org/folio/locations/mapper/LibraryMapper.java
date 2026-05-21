package org.folio.locations.mapper;

import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.entity.LibraryEntity;
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
public interface LibraryMapper extends EntityMapper<Library, LibraryEntity> {

  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  LibraryEntity toEntity(Library dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  void updateEntity(Library dto, @MappingTarget LibraryEntity entity);

  @Mapping(target = "metadata", source = "entity", qualifiedByName = "toMetadata")
  Library toDto(LibraryEntity entity);

  @Named("toMetadata")
  default Metadata toMetadata(LibraryEntity entity) {
    if (entity.getCreatedDate() == null) {
      return null;
    }
    return new Metadata()
      .createdDate(entity.getCreatedDate())
      .createdByUserId(entity.getCreatedByUserId())
      .updatedDate(entity.getUpdatedDate())
      .updatedByUserId(entity.getUpdatedByUserId());
  }
}
