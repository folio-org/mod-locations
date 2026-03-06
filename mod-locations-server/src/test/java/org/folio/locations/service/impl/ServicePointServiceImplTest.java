package org.folio.locations.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.exception.ServicePointNotFoundException;
import org.folio.locations.mapper.ServicePointMapper;
import org.folio.locations.repository.ServicePointRepository;
import org.folio.locations.service.validator.ServicePointValidator;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ServicePointServiceImplTest {

  private static final UUID SERVICE_POINT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Mock
  private ServicePointRepository repository;
  @Mock
  private ServicePointMapper mapper;
  @Mock
  private FolioExecutionContext context;
  @Mock
  private ServicePointValidator validator;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(repository, mapper, context, validator);
  }

  // ── getServicePoints ─────────────────────────────────────────────────────────

  @Test
  void getServicePoints_positive_allRecordsWithoutRoutingFilter() {
    var service = newService();
    var entity = new ServicePointEntity();
    var dto = new ServicePoint();
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("cql.allRecords=1 NOT ecsRequestRouting = true", OffsetRequest.of(0, 10)))
      .thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getServicePoints(null, 10, 0, false);

    assertThat(result.getServicepoints()).containsExactly(dto);
    assertThat(result.getTotalRecords()).isEqualTo(1);
  }

  @Test
  void getServicePoints_positive_customQueryWithRoutingServicePoints() {
    var service = newService();
    var entity = new ServicePointEntity();
    var dto = new ServicePoint();
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("(name==\"test\")", OffsetRequest.of(5, 20))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getServicePoints("name==\"test\"", 20, 5, true);

    assertThat(result.getServicepoints()).containsExactly(dto);
  }

  // ── getById ──────────────────────────────────────────────────────────────────

  @Test
  void getById_positive_returnsDto() {
    var service = newService();
    var entity = new ServicePointEntity();
    var dto = new ServicePoint();
    when(repository.findById(SERVICE_POINT_ID)).thenReturn(Optional.of(entity));
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getById(SERVICE_POINT_ID);

    assertThat(result).isSameAs(dto);
  }

  @Test
  void getById_negative_notFoundThrowsException() {
    var service = newService();
    when(repository.findById(SERVICE_POINT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(SERVICE_POINT_ID))
      .isInstanceOf(ServicePointNotFoundException.class)
      .hasMessageContaining(SERVICE_POINT_ID.toString());
  }

  // ── create ───────────────────────────────────────────────────────────────────

  @Test
  void create_positive_persistsAndReturnsDto() {
    final var dto = new ServicePoint().id(SERVICE_POINT_ID);
    var entity = new ServicePointEntity();
    entity.setId(SERVICE_POINT_ID); // mapper sets id from dto in production
    entity.setStaffSlips(List.of());
    var savedEntity = new ServicePointEntity();
    var resultDto = new ServicePoint();
    when(context.getUserId()).thenReturn(USER_ID);
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(resultDto);
    var service = newService();

    var result = service.create(dto);

    assertThat(result).isSameAs(resultDto);
    assertThat(entity.getId()).isEqualTo(SERVICE_POINT_ID);
    assertThat(entity.getCreatedByUserId()).isEqualTo(USER_ID);
    assertThat(entity.getCreatedDate()).isNotNull();
    verify(validator).validate(dto);
  }

  @Test
  void create_positive_generatesIdWhenNotProvided() {
    var dto = new ServicePoint();
    var entity = new ServicePointEntity();
    entity.setStaffSlips(List.of());
    var savedEntity = new ServicePointEntity();
    when(context.getUserId()).thenReturn(USER_ID);
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(new ServicePoint());
    var service = newService();

    service.create(dto);

    assertThat(entity.getId()).isNotNull();
    verify(validator).validate(dto);
  }

  // ── update ───────────────────────────────────────────────────────────────────

  @Test
  void update_positive_updatesExistingEntity() {
    var entity = new ServicePointEntity();
    entity.setStaffSlips(List.of());
    when(repository.findById(SERVICE_POINT_ID)).thenReturn(Optional.of(entity));
    when(context.getUserId()).thenReturn(USER_ID);
    when(repository.save(entity)).thenReturn(entity);
    var service = newService();
    var dto = new ServicePoint();

    service.update(SERVICE_POINT_ID, dto);

    assertThat(entity.getUpdatedByUserId()).isEqualTo(USER_ID);
    assertThat(entity.getUpdatedDate()).isNotNull();
    verify(validator).validate(dto);
    verify(mapper).updateEntity(dto, entity);
  }

  @Test
  void update_negative_notFoundThrowsException() {
    var dto = new ServicePoint();
    doNothing().when(validator).validate(dto);
    when(repository.findById(SERVICE_POINT_ID)).thenReturn(Optional.empty());
    var service = newService();

    assertThatThrownBy(() -> service.update(SERVICE_POINT_ID, dto))
      .isInstanceOf(ServicePointNotFoundException.class)
      .hasMessageContaining(SERVICE_POINT_ID.toString());
  }

  // ── deleteById ───────────────────────────────────────────────────────────────

  @Test
  void deleteById_positive_deletesExistingRecord() {
    var service = newService();
    when(repository.existsById(SERVICE_POINT_ID)).thenReturn(true);

    service.deleteById(SERVICE_POINT_ID);

    verify(repository).deleteById(SERVICE_POINT_ID);
  }

  @Test
  void deleteById_negative_notFoundThrowsException() {
    var service = newService();
    when(repository.existsById(SERVICE_POINT_ID)).thenReturn(false);

    assertThatThrownBy(() -> service.deleteById(SERVICE_POINT_ID))
      .isInstanceOf(ServicePointNotFoundException.class)
      .hasMessageContaining(SERVICE_POINT_ID.toString());
  }

  // ── deleteAll ────────────────────────────────────────────────────────────────

  @Test
  void deleteAll_positive_delegatesToRepository() {
    var service = newService();

    service.deleteAll();

    verify(repository).deleteAll();
  }

  // ── helpers ──────────────────────────────────────────────────────────────────

  private ServicePointServiceImpl newService() {
    return new ServicePointServiceImpl(repository, mapper, context, validator);
  }
}
