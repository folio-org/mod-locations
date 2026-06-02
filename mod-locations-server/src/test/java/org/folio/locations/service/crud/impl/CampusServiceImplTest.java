package org.folio.locations.service.crud.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.domain.event.DomainEvent;
import org.folio.locations.domain.event.DomainEventType;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.exception.CampusNotFoundException;
import org.folio.locations.mapper.CampusMapper;
import org.folio.locations.repository.CampusRepository;
import org.folio.locations.service.crud.ShadowFilterContext;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@UnitTest
@ExtendWith(MockitoExtension.class)
class CampusServiceImplTest {

  private static final UUID CAMPUS_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID INSTITUTION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final String TENANT_ID = "test-tenant";

  @Mock
  private CampusRepository repository;
  @Mock
  private CampusMapper mapper;
  @Mock
  private FolioExecutionContext context;
  @Mock
  private DomainEventPublisher publisher;

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(repository, mapper, context, publisher);
  }

  // ── getAll ────────────────────────────────────────────────────────────────────

  @Test
  void getAll_positive_allRecords() {
    var service = newService();
    var entity = new CampusEntity();
    var dto = new Campus("City Campus", "CC", INSTITUTION_ID);
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("(cql.allRecords = 1) and (isShadow = false)", OffsetRequest.of(0, 10))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getAll(new ShadowFilterContext(null, 10, 0, false));

    assertThat(result.resources()).containsExactly(dto);
    assertThat(result.totalRecords()).isEqualTo(1);
  }

  @Test
  void getAll_positive_withQuery() {
    var service = newService();
    var entity = new CampusEntity();
    var dto = new Campus("City Campus", "CC", INSTITUTION_ID);
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql(
      "(institutionId == " + INSTITUTION_ID + ") and (isShadow = false)", OffsetRequest.of(0, 5))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getAll(new ShadowFilterContext("institutionId==\"" + INSTITUTION_ID + "\"", 5, 0, false));

    assertThat(result.resources()).containsExactly(dto);
  }

  // ── getById ──────────────────────────────────────────────────────────────────

  @Test
  void getById_positive_returnsDto() {
    var service = newService();
    var entity = new CampusEntity();
    var dto = new Campus("City Campus", "CC", INSTITUTION_ID);
    when(repository.findById(CAMPUS_ID)).thenReturn(Optional.of(entity));
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getById(CAMPUS_ID);

    assertThat(result).isSameAs(dto);
  }

  @Test
  void getById_negative_notFoundThrowsException() {
    var service = newService();
    when(repository.findById(CAMPUS_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(CAMPUS_ID))
      .isInstanceOf(CampusNotFoundException.class)
      .hasMessageContaining(CAMPUS_ID.toString());
  }

  // ── create ───────────────────────────────────────────────────────────────────

  @Test
  void create_positive_persistsAndReturnsDto() {
    var dto = new Campus("City Campus", "CC", INSTITUTION_ID).id(CAMPUS_ID);
    var entity = new CampusEntity();
    entity.setId(CAMPUS_ID);
    var savedEntity = new CampusEntity();
    final var resultDto = new Campus("City Campus", "CC", INSTITUTION_ID);
    setupContextMocks();
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(resultDto);

    var result = newService().create(dto);

    assertThat(result).isSameAs(resultDto);
    assertThat(entity.getId()).isEqualTo(CAMPUS_ID);
    assertThat(entity.getCreatedByUserId()).isEqualTo(USER_ID);
    assertThat(entity.getCreatedDate()).isNotNull();
    verify(publisher).publish(any());
  }

  @Test
  void create_positive_generatesIdWhenNotProvided() {
    var dto = new Campus("City Campus", "CC", INSTITUTION_ID);
    var entity = new CampusEntity();
    var savedEntity = new CampusEntity();
    setupContextMocks();
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(new Campus("City Campus", "CC", INSTITUTION_ID));

    newService().create(dto);

    assertThat(entity.getId()).isNotNull();
    verify(publisher).publish(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void create_positive_publishesCreateEvent() {
    final var dto = new Campus("City Campus", "CC", INSTITUTION_ID).id(CAMPUS_ID);
    var savedEntity = new CampusEntity();
    savedEntity.setId(CAMPUS_ID);
    final var resultDto = new Campus("City Campus", "CC", INSTITUTION_ID);
    setupContextMocks();
    when(mapper.toEntity(dto)).thenReturn(new CampusEntity());
    when(repository.save(any())).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(resultDto);

    newService().create(dto);

    var captor = (ArgumentCaptor<DomainEvent<Campus>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(DomainEvent.class);
    verify(publisher).publish(captor.capture());
    var event = captor.getValue();
    assertThat(event.getType()).isEqualTo(DomainEventType.CREATE);
    assertThat(event.getResourceType()).isEqualTo(ResourceType.CAMPUS);
    assertThat(event.getResourceId()).isEqualTo(CAMPUS_ID);
    assertThat(event.getTenant()).isEqualTo(TENANT_ID);
    assertThat(event.getUserId()).isEqualTo(USER_ID);
    assertThat(event.getNewResource()).isSameAs(resultDto);
    assertThat(event.getOldResource()).isNull();
    assertThat(event.getEventId()).isNotNull();
  }

  // ── update ───────────────────────────────────────────────────────────────────

  @Test
  void update_positive_updatesExistingEntity() {
    var entity = new CampusEntity();
    var oldDto = new Campus("Old", "OLD", INSTITUTION_ID);
    when(repository.findById(CAMPUS_ID)).thenReturn(Optional.of(entity));
    setupContextMocks();
    when(mapper.toDto(entity)).thenReturn(oldDto);
    when(repository.save(entity)).thenReturn(entity);
    final var dto = new Campus("Updated", "UPD", INSTITUTION_ID);

    newService().update(CAMPUS_ID, dto);

    assertThat(entity.getUpdatedByUserId()).isEqualTo(USER_ID);
    verify(mapper).updateEntity(dto, entity);
    verify(publisher).publish(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void update_positive_publishesUpdateEvent() {
    var entity = new CampusEntity();
    entity.setId(CAMPUS_ID);
    var oldDto = new Campus("Old Campus", "OLD", INSTITUTION_ID);
    var newDto = new Campus("Updated Campus", "UPD", INSTITUTION_ID);
    when(repository.findById(CAMPUS_ID)).thenReturn(Optional.of(entity));
    setupContextMocks();
    when(mapper.toDto(entity)).thenReturn(oldDto).thenReturn(newDto);
    when(repository.save(entity)).thenReturn(entity);
    final var dto = new Campus("Updated Campus", "UPD", INSTITUTION_ID);

    newService().update(CAMPUS_ID, dto);

    var captor = (ArgumentCaptor<DomainEvent<Campus>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(DomainEvent.class);
    verify(publisher).publish(captor.capture());
    verify(mapper).updateEntity(dto, entity);
    var event = captor.getValue();
    assertThat(event.getType()).isEqualTo(DomainEventType.UPDATE);
    assertThat(event.getResourceType()).isEqualTo(ResourceType.CAMPUS);
    assertThat(event.getResourceId()).isEqualTo(CAMPUS_ID);
    assertThat(event.getTenant()).isEqualTo(TENANT_ID);
    assertThat(event.getUserId()).isEqualTo(USER_ID);
    assertThat(event.getOldResource()).isSameAs(oldDto);
    assertThat(event.getNewResource()).isSameAs(newDto);
  }

  @Test
  void update_negative_notFoundThrowsException() {
    when(repository.findById(CAMPUS_ID)).thenReturn(Optional.empty());
    final var dto = new Campus("Updated", "UPD", INSTITUTION_ID);

    var campusService = newService();
    assertThatThrownBy(() -> campusService.update(CAMPUS_ID, dto))
      .isInstanceOf(CampusNotFoundException.class)
      .hasMessageContaining(CAMPUS_ID.toString());
  }

  // ── deleteById ───────────────────────────────────────────────────────────────

  @Test
  void deleteById_positive_deletesExistingRecord() {
    var entity = new CampusEntity();
    var oldDto = new Campus("City Campus", "CC", INSTITUTION_ID);
    when(repository.findById(CAMPUS_ID)).thenReturn(Optional.of(entity));
    when(mapper.toDto(entity)).thenReturn(oldDto);
    setupContextMocks();

    newService().deleteById(CAMPUS_ID);

    verify(repository).deleteById(CAMPUS_ID);
    verify(publisher).publish(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void deleteById_positive_publishesDeleteEvent() {
    var entity = new CampusEntity();
    entity.setId(CAMPUS_ID);
    var oldDto = new Campus("City Campus", "CC", INSTITUTION_ID);
    when(repository.findById(CAMPUS_ID)).thenReturn(Optional.of(entity));
    when(mapper.toDto(entity)).thenReturn(oldDto);
    setupContextMocks();

    newService().deleteById(CAMPUS_ID);

    var captor = (ArgumentCaptor<DomainEvent<Campus>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(DomainEvent.class);
    verify(repository).deleteById(CAMPUS_ID);
    verify(publisher).publish(captor.capture());
    var event = captor.getValue();
    assertThat(event.getType()).isEqualTo(DomainEventType.DELETE);
    assertThat(event.getResourceType()).isEqualTo(ResourceType.CAMPUS);
    assertThat(event.getResourceId()).isEqualTo(CAMPUS_ID);
    assertThat(event.getTenant()).isEqualTo(TENANT_ID);
    assertThat(event.getUserId()).isEqualTo(USER_ID);
    assertThat(event.getOldResource()).isSameAs(oldDto);
    assertThat(event.getNewResource()).isNull();
    assertThat(event.getEventTs()).isNotNull();
  }

  @Test
  void deleteById_negative_notFoundThrowsException() {
    when(repository.findById(CAMPUS_ID)).thenReturn(Optional.empty());

    var campusService = newService();
    assertThatThrownBy(() -> campusService.deleteById(CAMPUS_ID))
      .isInstanceOf(CampusNotFoundException.class)
      .hasMessageContaining(CAMPUS_ID.toString());
  }

  // ── deleteAll ────────────────────────────────────────────────────────────────

  @Test
  void deleteAll_positive_delegatesToRepository() {
    when(repository.findAll()).thenReturn(List.of());

    newService().deleteAll();

    verify(repository).deleteAll(List.of());
  }

  // ── helpers ──────────────────────────────────────────────────────────────────

  private CampusServiceImpl newService() {
    return new CampusServiceImpl(repository, mapper, context, publisher);
  }

  private void setupContextMocks() {
    when(context.getUserId()).thenReturn(USER_ID);
    when(context.getTenantId()).thenReturn(TENANT_ID);
  }
}
