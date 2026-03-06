package org.folio.locations.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.exception.CampusNotFoundException;
import org.folio.locations.mapper.CampusMapper;
import org.folio.locations.repository.CampusRepository;
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
class CampusServiceImplTest {

  private static final UUID CAMPUS_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID INSTITUTION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Mock
  private CampusRepository repository;
  @Mock
  private CampusMapper mapper;
  @Mock
  private FolioExecutionContext context;

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(repository, mapper, context);
  }

  // ── getAll ────────────────────────────────────────────────────────────────────

  @Test
  void getAll_positive_allRecords() {
    var service = newService();
    var entity = new CampusEntity();
    var dto = new Campus("City Campus", "CC", INSTITUTION_ID);
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("cql.allRecords=1", OffsetRequest.of(0, 10))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getAll(null, 10, 0);

    assertThat(result.getLoccamps()).containsExactly(dto);
    assertThat(result.getTotalRecords()).isEqualTo(1);
  }

  @Test
  void getAll_positive_withQuery() {
    var service = newService();
    var entity = new CampusEntity();
    var dto = new Campus("City Campus", "CC", INSTITUTION_ID);
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql(
      "(institutionId==\"" + INSTITUTION_ID + "\")", OffsetRequest.of(0, 5))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getAll("institutionId==\"" + INSTITUTION_ID + "\"", 5, 0);

    assertThat(result.getLoccamps()).containsExactly(dto);
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
    var resultDto = new Campus("City Campus", "CC", INSTITUTION_ID);
    var userId = UUID.randomUUID();
    when(context.getUserId()).thenReturn(userId);
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(resultDto);
    var service = newService();

    var result = service.create(dto);

    assertThat(result).isSameAs(resultDto);
    assertThat(entity.getId()).isEqualTo(CAMPUS_ID);
    assertThat(entity.getCreatedByUserId()).isEqualTo(userId);
    assertThat(entity.getCreatedDate()).isNotNull();
  }

  @Test
  void create_positive_generatesIdWhenNotProvided() {
    var dto = new Campus("City Campus", "CC", INSTITUTION_ID);
    var entity = new CampusEntity();
    var savedEntity = new CampusEntity();
    var userId = UUID.randomUUID();
    when(context.getUserId()).thenReturn(userId);
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(new Campus("City Campus", "CC", INSTITUTION_ID));
    var service = newService();

    service.create(dto);

    assertThat(entity.getId()).isNotNull();
  }

  // ── update ───────────────────────────────────────────────────────────────────

  @Test
  void update_positive_updatesExistingEntity() {
    var entity = new CampusEntity();
    var userId = UUID.randomUUID();
    when(repository.findById(CAMPUS_ID)).thenReturn(Optional.of(entity));
    when(context.getUserId()).thenReturn(userId);
    when(repository.save(entity)).thenReturn(entity);
    var service = newService();
    var dto = new Campus("Updated", "UPD", INSTITUTION_ID);

    service.update(CAMPUS_ID, dto);

    assertThat(entity.getUpdatedByUserId()).isEqualTo(userId);
    assertThat(entity.getUpdatedDate()).isNotNull();
    verify(mapper).updateEntity(dto, entity);
  }

  @Test
  void update_negative_notFoundThrowsException() {
    when(repository.findById(CAMPUS_ID)).thenReturn(Optional.empty());
    var service = newService();
    var dto = new Campus("Updated", "UPD", INSTITUTION_ID);

    assertThatThrownBy(() -> service.update(CAMPUS_ID, dto))
      .isInstanceOf(CampusNotFoundException.class)
      .hasMessageContaining(CAMPUS_ID.toString());
  }

  // ── deleteById ───────────────────────────────────────────────────────────────

  @Test
  void deleteById_positive_deletesExistingRecord() {
    var service = newService();
    when(repository.existsById(CAMPUS_ID)).thenReturn(true);

    service.deleteById(CAMPUS_ID);

    verify(repository).deleteById(CAMPUS_ID);
  }

  @Test
  void deleteById_negative_notFoundThrowsException() {
    var service = newService();
    when(repository.existsById(CAMPUS_ID)).thenReturn(false);

    assertThatThrownBy(() -> service.deleteById(CAMPUS_ID))
      .isInstanceOf(CampusNotFoundException.class)
      .hasMessageContaining(CAMPUS_ID.toString());
  }

  // ── deleteAll ────────────────────────────────────────────────────────────────

  @Test
  void deleteAll_positive_delegatesToRepository() {
    var service = newService();

    service.deleteAll();

    verify(repository).deleteAll();
  }

  // ── helpers ──────────────────────────────────────────────────────────────────

  private CampusServiceImpl newService() {
    return new CampusServiceImpl(repository, mapper, context);
  }
}
