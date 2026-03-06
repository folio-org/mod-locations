package org.folio.locations.service.impl;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointsCollection;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.domain.entity.ServicePointStaffSlipId;
import org.folio.locations.exception.ServicePointNotFoundException;
import org.folio.locations.mapper.ServicePointMapper;
import org.folio.locations.repository.ServicePointRepository;
import org.folio.locations.service.ServicePointService;
import org.folio.locations.service.validator.ServicePointValidator;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.data.OffsetRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicePointServiceImpl implements ServicePointService {

  private static final String ALL_RECORDS_CQL = "cql.allRecords=1";
  private static final String ECS_ROUTING_FILTER = " NOT ecsRequestRouting = true";

  private final ServicePointRepository repository;
  private final ServicePointMapper mapper;
  private final FolioExecutionContext context;
  private final ServicePointValidator validator;

  @Override
  @Transactional(readOnly = true)
  public ServicePointsCollection getServicePoints(@Nullable String query, Integer limit, Integer offset,
                                                  Boolean includeRoutingServicePoints) {
    var cql = buildCqlQuery(query, includeRoutingServicePoints);
    var page = repository.findByCql(cql, OffsetRequest.of(offset, limit));
    var servicePoints = page.getContent().stream().map(mapper::toDto).toList();
    return new ServicePointsCollection(servicePoints, (int) page.getTotalElements());
  }

  @Override
  @Transactional(readOnly = true)
  public ServicePoint getById(UUID id) {
    return repository.findById(id)
      .map(mapper::toDto)
      .orElseThrow(() -> new ServicePointNotFoundException(id));
  }

  @Override
  @Transactional
  public ServicePoint create(ServicePoint dto) {
    validator.validate(dto);
    var entity = mapper.toEntity(dto);
    entity.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
    entity.setCreatedDate(OffsetDateTime.now());
    entity.setCreatedByUserId(context.getUserId());
    syncStaffSlipIds(entity);
    return mapper.toDto(repository.save(entity));
  }

  @Override
  @Transactional
  public void update(UUID id, ServicePoint dto) {
    validator.validate(dto);
    var entity = repository.findById(id)
      .orElseThrow(() -> new ServicePointNotFoundException(id));
    mapper.updateEntity(dto, entity);
    if (dto.getHoldShelfExpiryPeriod() == null) {
      entity.setHoldShelfExpiryPeriodDuration(null);
      entity.setHoldShelfExpiryPeriodIntervalId(null);
    }
    entity.setUpdatedDate(OffsetDateTime.now());
    entity.setUpdatedByUserId(context.getUserId());
    syncStaffSlipIds(entity);
    repository.save(entity);
  }

  @Override
  @Transactional
  public void deleteById(UUID id) {
    if (!repository.existsById(id)) {
      throw new ServicePointNotFoundException(id);
    }
    repository.deleteById(id);
  }

  @Override
  @Transactional
  public void deleteAll() {
    repository.deleteAll();
  }

  private void syncStaffSlipIds(ServicePointEntity entity) {
    entity.getStaffSlips().forEach(slip -> {
      if (slip.getId() == null) {
        slip.setId(new ServicePointStaffSlipId());
      }
      slip.getId().setServicePointId(entity.getId());
      slip.setServicePoint(entity);
    });
  }

  private String buildCqlQuery(@Nullable String query, boolean includeRoutingServicePoints) {
    var base = query != null ? "(" + query + ")" : ALL_RECORDS_CQL;
    return includeRoutingServicePoints ? base : base + ECS_ROUTING_FILTER;
  }
}
