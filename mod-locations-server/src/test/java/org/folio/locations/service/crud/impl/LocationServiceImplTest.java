package org.folio.locations.service.crud.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.entity.LocationEntity;
import org.folio.locations.exception.LocationNotFoundException;
import org.folio.locations.mapper.LocationMapper;
import org.folio.locations.repository.LocationRepository;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.locations.service.validator.LocationValidator;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

  private static final UUID LOCATION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID SP_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID INST_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID CAMPUS_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID LIBRARY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final UUID USER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final String TENANT_ID = "test-tenant";

  @Mock
  private LocationRepository repository;
  @Mock
  private LocationMapper mapper;
  @Mock
  private FolioExecutionContext context;
  @Mock
  private LocationValidator validator;
  @Mock
  private DomainEventPublisher publisher;

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(repository, mapper, context, validator, publisher);
  }

  // ── getAll ────────────────────────────────────────────────────────────────────

  @Test
  void getAll_positive_allRecords() {
    var service = newService();
    var entity = new LocationEntity();
    var dto = location("Main", "MN");
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("isShadow==false", OffsetRequest.of(0, 10))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getAll(null, 10, 0, false);

    assertThat(result.getLocations()).containsExactly(dto);
    assertThat(result.getTotalRecords()).isEqualTo(1);
  }

  @Test
  void getAll_positive_withQuery() {
    var service = newService();
    var entity = new LocationEntity();
    var dto = location("Main", "MN");
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("(name==\"Main\") AND isShadow==false", OffsetRequest.of(0, 5))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getAll("name==\"Main\"", 5, 0, false);

    assertThat(result.getLocations()).containsExactly(dto);
  }

  @Test
  void getAll_positive_includeShadow() {
    var service = newService();
    var entity = new LocationEntity();
    var dto = location("Shadow", "SH");
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("(name==\"Shadow\")", OffsetRequest.of(0, 10))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getAll("name==\"Shadow\"", 10, 0, true);

    assertThat(result.getLocations()).containsExactly(dto);
  }

  // ── getById ──────────────────────────────────────────────────────────────────

  @Test
  void getById_positive_returnsDto() {
    var service = newService();
    var entity = new LocationEntity();
    var dto = location("Main", "MN");
    when(repository.findById(LOCATION_ID)).thenReturn(Optional.of(entity));
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getById(LOCATION_ID);

    assertThat(result).isSameAs(dto);
  }

  @Test
  void getById_negative_notFoundThrowsException() {
    var service = newService();
    when(repository.findById(LOCATION_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(LOCATION_ID))
      .isInstanceOf(LocationNotFoundException.class)
      .hasMessageContaining(LOCATION_ID.toString());
  }

  // ── create ───────────────────────────────────────────────────────────────────

  @Test
  void create_positive_persistsAndReturnsDto() {
    final var dto = location("Main", "MN").id(LOCATION_ID);
    var entity = new LocationEntity();
    entity.setId(LOCATION_ID);
    var savedEntity = new LocationEntity();
    var resultDto = location("Main", "MN");
    when(context.getUserId()).thenReturn(USER_ID);
    when(context.getTenantId()).thenReturn(TENANT_ID);
    when(mapper.toEntity(dto)).thenReturn(entity);
    doNothing().when(validator).validate(dto);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(resultDto);
    var service = newService();

    var result = service.create(dto);

    assertThat(result).isSameAs(resultDto);
    assertThat(entity.getCreatedByUserId()).isEqualTo(USER_ID);
    assertThat(entity.getCreatedDate()).isNotNull();
    verify(publisher).publish(any());
  }

  @Test
  void create_positive_generatesIdWhenNotProvided() {
    var dto = location("Main", "MN");
    var entity = new LocationEntity();
    var savedEntity = new LocationEntity();
    when(context.getUserId()).thenReturn(USER_ID);
    when(context.getTenantId()).thenReturn(TENANT_ID);
    when(mapper.toEntity(dto)).thenReturn(entity);
    doNothing().when(validator).validate(dto);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(location("Main", "MN"));
    var service = newService();

    service.create(dto);

    assertThat(entity.getId()).isNotNull();
    verify(publisher).publish(any());
  }

  // ── update ───────────────────────────────────────────────────────────────────

  @Test
  void update_positive_updatesExistingEntity() {
    var entity = new LocationEntity();
    final var dto = location("Updated", "UPD");
    var oldDto = location("Main", "MN");
    when(repository.findById(LOCATION_ID)).thenReturn(Optional.of(entity));
    when(context.getUserId()).thenReturn(USER_ID);
    when(context.getTenantId()).thenReturn(TENANT_ID);
    when(mapper.toDto(entity)).thenReturn(oldDto);
    doNothing().when(validator).validate(dto);
    when(repository.save(entity)).thenReturn(entity);
    var service = newService();

    service.update(LOCATION_ID, dto);

    assertThat(entity.getUpdatedByUserId()).isEqualTo(USER_ID);
    verify(mapper).updateEntity(dto, entity);
    verify(publisher).publish(any());
  }

  @Test
  void update_negative_notFoundThrowsException() {
    final var dto = location("Updated", "UPD");
    when(repository.findById(LOCATION_ID)).thenReturn(Optional.empty());
    doNothing().when(validator).validate(dto);
    var service = newService();

    assertThatThrownBy(() -> service.update(LOCATION_ID, dto))
      .isInstanceOf(LocationNotFoundException.class)
      .hasMessageContaining(LOCATION_ID.toString());
  }

  // ── deleteById ───────────────────────────────────────────────────────────────

  @Test
  void deleteById_positive_deletesExistingRecord() {
    var entity = new LocationEntity();
    var oldDto = location("Main", "MN");
    when(repository.findById(LOCATION_ID)).thenReturn(Optional.of(entity));
    when(mapper.toDto(entity)).thenReturn(oldDto);
    when(context.getTenantId()).thenReturn(TENANT_ID);
    when(context.getUserId()).thenReturn(USER_ID);
    var service = newService();

    service.deleteById(LOCATION_ID);

    verify(repository).deleteById(LOCATION_ID);
    verify(publisher).publish(any());
  }

  @Test
  void deleteById_negative_notFoundThrowsException() {
    var service = newService();
    when(repository.findById(LOCATION_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deleteById(LOCATION_ID))
      .isInstanceOf(LocationNotFoundException.class)
      .hasMessageContaining(LOCATION_ID.toString());
  }

  // ── deleteAll ────────────────────────────────────────────────────────────────

  @Test
  void deleteAll_positive_delegatesToRepository() {
    var service = newService();

    service.deleteAll();

    verify(repository).deleteAll();
  }

  // ── helpers ──────────────────────────────────────────────────────────────────

  private LocationServiceImpl newService() {
    return new LocationServiceImpl(repository, mapper, context, validator, publisher);
  }

  private static Location location(String name, String code) {
    return new Location(name, code, INST_ID, CAMPUS_ID, LIBRARY_ID, SP_ID)
      .addServicePointIdsItem(SP_ID);
  }
}
