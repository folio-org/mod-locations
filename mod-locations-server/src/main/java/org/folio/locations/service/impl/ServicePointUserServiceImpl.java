package org.folio.locations.service.impl;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.domain.dto.ServicePointsUsersCollection;
import org.folio.locations.exception.ServicePointUserNotFoundException;
import org.folio.locations.mapper.ServicePointUserMapper;
import org.folio.locations.repository.ServicePointUserRepository;
import org.folio.locations.service.ServicePointUserService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.data.OffsetRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicePointUserServiceImpl implements ServicePointUserService {

  private static final String ALL_RECORDS_CQL = "cql.allRecords=1";
  private static final Pattern SERVICE_POINTS_IDS_PATTERN =
    Pattern.compile("^servicePointsIds==?([0-9a-fA-F-]{36})$");

  private final ServicePointUserRepository repository;
  private final ServicePointUserMapper mapper;
  private final FolioExecutionContext context;

  @Override
  @Transactional(readOnly = true)
  public ServicePointsUsersCollection getServicePointsUsers(@Nullable String query, Integer limit, Integer offset) {
    var pageable = OffsetRequest.of(offset, limit);
    if (query != null) {
      var matcher = SERVICE_POINTS_IDS_PATTERN.matcher(query.trim());
      if (matcher.matches()) {
        var spId = UUID.fromString(matcher.group(1));
        var page = repository.findByServicePointsIdsContaining(spId, pageable);
        var users = page.getContent().stream().map(mapper::toDto).toList();
        return new ServicePointsUsersCollection(users, (int) page.getTotalElements());
      }
    }
    var cql = query != null ? "(" + query + ")" : ALL_RECORDS_CQL;
    var page = repository.findByCql(cql, pageable);
    var users = page.getContent().stream().map(mapper::toDto).toList();
    return new ServicePointsUsersCollection(users, (int) page.getTotalElements());
  }

  @Override
  @Transactional(readOnly = true)
  public ServicePointsUser getById(UUID id) {
    return repository.findById(id)
      .map(mapper::toDto)
      .orElseThrow(() -> new ServicePointUserNotFoundException(id));
  }

  @Override
  @Transactional
  public ServicePointsUser create(ServicePointsUser dto) {
    var entity = mapper.toEntity(dto);
    entity.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
    entity.setCreatedDate(OffsetDateTime.now());
    entity.setCreatedByUserId(context.getUserId());
    return mapper.toDto(repository.save(entity));
  }

  @Override
  @Transactional
  public void update(UUID id, ServicePointsUser dto) {
    var entity = repository.findById(id)
      .orElseThrow(() -> new ServicePointUserNotFoundException(id));
    mapper.updateEntity(dto, entity);
    entity.setUpdatedDate(OffsetDateTime.now());
    entity.setUpdatedByUserId(context.getUserId());
    repository.save(entity);
  }

  @Override
  @Transactional
  public void deleteById(UUID id) {
    if (!repository.existsById(id)) {
      throw new ServicePointUserNotFoundException(id);
    }
    repository.deleteById(id);
  }

  @Override
  @Transactional
  public void deleteAll() {
    repository.deleteAll();
  }
}
