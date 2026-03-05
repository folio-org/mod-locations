package org.folio.locations.mapper;

import org.folio.locations.domain.dto.HoldShelfExpiryPeriod;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointStaffSlip;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.domain.entity.ServicePointStaffSlipEntity;
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
public interface ServicePointMapper {

  @Mapping(target = "holdShelfExpiryPeriodDuration", source = "holdShelfExpiryPeriod.duration")
  @Mapping(target = "holdShelfExpiryPeriodIntervalId", source = "holdShelfExpiryPeriod.intervalId",
           qualifiedByName = "intervalIdEnumToString")
  @Mapping(target = "holdShelfClosedLibraryDateManagement", source = "holdShelfClosedLibraryDateManagement",
           qualifiedByName = "closedLibraryEnumToString")
  @Mapping(target = "defaultCheckInActionForUseAtLocation", source = "defaultCheckInActionForUseAtLocation",
           qualifiedByName = "checkInActionEnumToString")
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  ServicePointEntity toEntity(ServicePoint dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "holdShelfExpiryPeriodDuration", source = "holdShelfExpiryPeriod.duration")
  @Mapping(target = "holdShelfExpiryPeriodIntervalId", source = "holdShelfExpiryPeriod.intervalId",
           qualifiedByName = "intervalIdEnumToString")
  @Mapping(target = "holdShelfClosedLibraryDateManagement", source = "holdShelfClosedLibraryDateManagement",
           qualifiedByName = "closedLibraryEnumToString")
  @Mapping(target = "defaultCheckInActionForUseAtLocation", source = "defaultCheckInActionForUseAtLocation",
           qualifiedByName = "checkInActionEnumToString")
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "createdByUserId", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "updatedByUserId", ignore = true)
  void updateEntity(ServicePoint dto, @MappingTarget ServicePointEntity entity);

  @Mapping(target = "holdShelfExpiryPeriod", source = "entity", qualifiedByName = "toHoldShelfExpiryPeriod")
  @Mapping(target = "holdShelfClosedLibraryDateManagement", source = "holdShelfClosedLibraryDateManagement",
           qualifiedByName = "stringToClosedLibraryEnum")
  @Mapping(target = "defaultCheckInActionForUseAtLocation", source = "defaultCheckInActionForUseAtLocation",
           qualifiedByName = "stringToCheckInActionEnum")
  @Mapping(target = "metadata", source = "entity", qualifiedByName = "toMetadata")
  ServicePoint toDto(ServicePointEntity entity);

  @Mapping(target = "id", source = "id.staffSlipId")
  ServicePointStaffSlip toStaffSlipDto(ServicePointStaffSlipEntity entity);

  @Mapping(target = "id.staffSlipId", source = "id")
  @Mapping(target = "id.servicePointId", ignore = true)
  @Mapping(target = "servicePoint", ignore = true)
  ServicePointStaffSlipEntity toStaffSlipEntity(ServicePointStaffSlip dto);

  @Named("intervalIdEnumToString")
  default String intervalIdEnumToString(HoldShelfExpiryPeriod.IntervalIdEnum intervalId) {
    return intervalId != null ? intervalId.getValue() : null;
  }

  @Named("closedLibraryEnumToString")
  default String closedLibraryEnumToString(ServicePoint.HoldShelfClosedLibraryDateManagementEnum value) {
    return value != null ? value.getValue() : null;
  }

  @Named("checkInActionEnumToString")
  default String checkInActionEnumToString(ServicePoint.DefaultCheckInActionForUseAtLocationEnum value) {
    return value != null ? value.getValue() : null;
  }

  @Named("toHoldShelfExpiryPeriod")
  default HoldShelfExpiryPeriod toHoldShelfExpiryPeriod(ServicePointEntity entity) {
    if (entity.getHoldShelfExpiryPeriodDuration() == null || entity.getHoldShelfExpiryPeriodIntervalId() == null) {
      return null;
    }
    return new HoldShelfExpiryPeriod(
      entity.getHoldShelfExpiryPeriodDuration(),
      HoldShelfExpiryPeriod.IntervalIdEnum.fromValue(entity.getHoldShelfExpiryPeriodIntervalId())
    );
  }

  @Named("stringToClosedLibraryEnum")
  default ServicePoint.HoldShelfClosedLibraryDateManagementEnum stringToClosedLibraryEnum(String value) {
    return value != null ? ServicePoint.HoldShelfClosedLibraryDateManagementEnum.fromValue(value) : null;
  }

  @Named("stringToCheckInActionEnum")
  default ServicePoint.DefaultCheckInActionForUseAtLocationEnum stringToCheckInActionEnum(String value) {
    return value != null ? ServicePoint.DefaultCheckInActionForUseAtLocationEnum.fromValue(value) : null;
  }

  @Named("toMetadata")
  default Metadata toMetadata(ServicePointEntity entity) {
    if (entity.getCreatedDate() == null) {
      return null;
    }
    return new Metadata(entity.getCreatedDate(), entity.getCreatedByUserId())
      .updatedDate(entity.getUpdatedDate())
      .updatedByUserId(entity.getUpdatedByUserId());
  }
}
