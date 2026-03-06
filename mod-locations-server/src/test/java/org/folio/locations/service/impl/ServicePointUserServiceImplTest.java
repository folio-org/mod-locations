package org.folio.locations.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.domain.entity.ServicePointUserEntity;
import org.folio.locations.exception.ServicePointUserNotFoundException;
import org.folio.locations.mapper.ServicePointUserMapper;
import org.folio.locations.repository.ServicePointUserRepository;
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
class ServicePointUserServiceImplTest {

  private static final UUID USER_RECORD_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID FOLIO_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Mock
  private ServicePointUserRepository repository;
  @Mock
  private ServicePointUserMapper mapper;
  @Mock
  private FolioExecutionContext context;

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(repository, mapper, context);
  }

  // ── getServicePointsUsers ─────────────────────────────────────────────────────

  @Test
  void getServicePointsUsers_positive_allRecords() {
    var service = newService();
    var entity = new ServicePointUserEntity();
    var dto = new ServicePointsUser(FOLIO_USER_ID);
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("cql.allRecords=1", OffsetRequest.of(0, 10))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getServicePointsUsers(null, 10, 0);

    assertThat(result.getServicePointsUsers()).containsExactly(dto);
    assertThat(result.getTotalRecords()).isEqualTo(1);
  }

  @Test
  void getServicePointsUsers_positive_customQuery() {
    var service = newService();
    var entity = new ServicePointUserEntity();
    var dto = new ServicePointsUser(FOLIO_USER_ID);
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("(userId==\"" + FOLIO_USER_ID + "\")", OffsetRequest.of(0, 5))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getServicePointsUsers("userId==\"" + FOLIO_USER_ID + "\"", 5, 0);

    assertThat(result.getServicePointsUsers()).containsExactly(dto);
  }

  // ── getById ──────────────────────────────────────────────────────────────────

  @Test
  void getById_positive_returnsDto() {
    var service = newService();
    var entity = new ServicePointUserEntity();
    var dto = new ServicePointsUser(FOLIO_USER_ID);
    when(repository.findById(USER_RECORD_ID)).thenReturn(Optional.of(entity));
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getById(USER_RECORD_ID);

    assertThat(result).isSameAs(dto);
  }

  @Test
  void getById_negative_notFoundThrowsException() {
    var service = newService();
    when(repository.findById(USER_RECORD_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(USER_RECORD_ID))
      .isInstanceOf(ServicePointUserNotFoundException.class)
      .hasMessageContaining(USER_RECORD_ID.toString());
  }

  // ── create ───────────────────────────────────────────────────────────────────

  @Test
  void create_positive_persistsAndReturnsDto() {
    var dto = new ServicePointsUser(FOLIO_USER_ID).id(USER_RECORD_ID);
    var entity = new ServicePointUserEntity();
    var savedEntity = new ServicePointUserEntity();
    var resultDto = new ServicePointsUser(FOLIO_USER_ID);
    when(context.getUserId()).thenReturn(FOLIO_USER_ID);
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(resultDto);
    var service = newService();

    var result = service.create(dto);

    assertThat(result).isSameAs(resultDto);
    assertThat(entity.getId()).isEqualTo(USER_RECORD_ID);
    assertThat(entity.getCreatedByUserId()).isEqualTo(FOLIO_USER_ID);
    assertThat(entity.getCreatedDate()).isNotNull();
  }

  @Test
  void create_positive_generatesIdWhenNotProvided() {
    var dto = new ServicePointsUser(FOLIO_USER_ID);
    var entity = new ServicePointUserEntity();
    var savedEntity = new ServicePointUserEntity();
    when(context.getUserId()).thenReturn(FOLIO_USER_ID);
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(new ServicePointsUser(FOLIO_USER_ID));
    var service = newService();

    service.create(dto);

    assertThat(entity.getId()).isNotNull();
  }

  // ── update ───────────────────────────────────────────────────────────────────

  @Test
  void update_positive_updatesExistingEntity() {
    var entity = new ServicePointUserEntity();
    when(repository.findById(USER_RECORD_ID)).thenReturn(Optional.of(entity));
    when(context.getUserId()).thenReturn(FOLIO_USER_ID);
    when(repository.save(entity)).thenReturn(entity);
    var service = newService();
    var dto = new ServicePointsUser(FOLIO_USER_ID);

    service.update(USER_RECORD_ID, dto);

    assertThat(entity.getUpdatedByUserId()).isEqualTo(FOLIO_USER_ID);
    assertThat(entity.getUpdatedDate()).isNotNull();
    verify(mapper).updateEntity(dto, entity);
  }

  @Test
  void update_negative_notFoundThrowsException() {
    when(repository.findById(USER_RECORD_ID)).thenReturn(Optional.empty());
    var service = newService();
    var dto = new ServicePointsUser(FOLIO_USER_ID);

    assertThatThrownBy(() -> service.update(USER_RECORD_ID, dto))
      .isInstanceOf(ServicePointUserNotFoundException.class)
      .hasMessageContaining(USER_RECORD_ID.toString());
  }

  // ── deleteById ───────────────────────────────────────────────────────────────

  @Test
  void deleteById_positive_deletesExistingRecord() {
    var service = newService();
    when(repository.existsById(USER_RECORD_ID)).thenReturn(true);

    service.deleteById(USER_RECORD_ID);

    verify(repository).deleteById(USER_RECORD_ID);
  }

  @Test
  void deleteById_negative_notFoundThrowsException() {
    var service = newService();
    when(repository.existsById(USER_RECORD_ID)).thenReturn(false);

    assertThatThrownBy(() -> service.deleteById(USER_RECORD_ID))
      .isInstanceOf(ServicePointUserNotFoundException.class)
      .hasMessageContaining(USER_RECORD_ID.toString());
  }

  // ── deleteAll ────────────────────────────────────────────────────────────────

  @Test
  void deleteAll_positive_delegatesToRepository() {
    var service = newService();

    service.deleteAll();

    verify(repository).deleteAll();
  }

  // ── helpers ──────────────────────────────────────────────────────────────────

  private ServicePointUserServiceImpl newService() {
    return new ServicePointUserServiceImpl(repository, mapper, context);
  }
}
