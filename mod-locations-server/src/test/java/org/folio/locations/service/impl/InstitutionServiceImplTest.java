package org.folio.locations.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.exception.InstitutionNotFoundException;
import org.folio.locations.mapper.InstitutionMapper;
import org.folio.locations.repository.InstitutionRepository;
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
class InstitutionServiceImplTest {

  private static final UUID INSTITUTION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock
  private InstitutionRepository repository;
  @Mock
  private InstitutionMapper mapper;
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
    var entity = new InstitutionEntity();
    var dto = new Institution("Main", "MAIN");
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("cql.allRecords=1", OffsetRequest.of(0, 10))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getAll(null, 10, 0);

    assertThat(result.getLocinsts()).containsExactly(dto);
    assertThat(result.getTotalRecords()).isEqualTo(1);
  }

  @Test
  void getAll_positive_withQuery() {
    var service = newService();
    var entity = new InstitutionEntity();
    var dto = new Institution("Main", "MAIN");
    var page = new PageImpl<>(List.of(entity));
    when(repository.findByCql("(name==\"Main\")", OffsetRequest.of(0, 5))).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getAll("name==\"Main\"", 5, 0);

    assertThat(result.getLocinsts()).containsExactly(dto);
  }

  // ── getById ──────────────────────────────────────────────────────────────────

  @Test
  void getById_positive_returnsDto() {
    var service = newService();
    var entity = new InstitutionEntity();
    var dto = new Institution("Main", "MAIN");
    when(repository.findById(INSTITUTION_ID)).thenReturn(Optional.of(entity));
    when(mapper.toDto(entity)).thenReturn(dto);

    var result = service.getById(INSTITUTION_ID);

    assertThat(result).isSameAs(dto);
  }

  @Test
  void getById_negative_notFoundThrowsException() {
    var service = newService();
    when(repository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(INSTITUTION_ID))
      .isInstanceOf(InstitutionNotFoundException.class)
      .hasMessageContaining(INSTITUTION_ID.toString());
  }

  // ── create ───────────────────────────────────────────────────────────────────

  @Test
  void create_positive_persistsAndReturnsDto() {
    var dto = new Institution("Main", "MAIN").id(INSTITUTION_ID);
    var entity = new InstitutionEntity();
    entity.setId(INSTITUTION_ID);
    var savedEntity = new InstitutionEntity();
    var resultDto = new Institution("Main", "MAIN");
    var userId = UUID.randomUUID();
    when(context.getUserId()).thenReturn(userId);
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(resultDto);
    var service = newService();

    var result = service.create(dto);

    assertThat(result).isSameAs(resultDto);
    assertThat(entity.getId()).isEqualTo(INSTITUTION_ID);
    assertThat(entity.getCreatedByUserId()).isEqualTo(userId);
    assertThat(entity.getCreatedDate()).isNotNull();
  }

  @Test
  void create_positive_generatesIdWhenNotProvided() {
    var dto = new Institution("Main", "MAIN");
    var entity = new InstitutionEntity();
    var savedEntity = new InstitutionEntity();
    var userId = UUID.randomUUID();
    when(context.getUserId()).thenReturn(userId);
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(new Institution("Main", "MAIN"));
    var service = newService();

    service.create(dto);

    assertThat(entity.getId()).isNotNull();
  }

  // ── update ───────────────────────────────────────────────────────────────────

  @Test
  void update_positive_updatesExistingEntity() {
    var entity = new InstitutionEntity();
    var userId = UUID.randomUUID();
    when(repository.findById(INSTITUTION_ID)).thenReturn(Optional.of(entity));
    when(context.getUserId()).thenReturn(userId);
    when(repository.save(entity)).thenReturn(entity);
    var service = newService();
    var dto = new Institution("Updated", "UPD");

    service.update(INSTITUTION_ID, dto);

    assertThat(entity.getUpdatedByUserId()).isEqualTo(userId);
    assertThat(entity.getUpdatedDate()).isNotNull();
    verify(mapper).updateEntity(dto, entity);
  }

  @Test
  void update_negative_notFoundThrowsException() {
    when(repository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());
    var service = newService();
    var dto = new Institution("Updated", "UPD");

    assertThatThrownBy(() -> service.update(INSTITUTION_ID, dto))
      .isInstanceOf(InstitutionNotFoundException.class)
      .hasMessageContaining(INSTITUTION_ID.toString());
  }

  // ── deleteById ───────────────────────────────────────────────────────────────

  @Test
  void deleteById_positive_deletesExistingRecord() {
    var service = newService();
    when(repository.existsById(INSTITUTION_ID)).thenReturn(true);

    service.deleteById(INSTITUTION_ID);

    verify(repository).deleteById(INSTITUTION_ID);
  }

  @Test
  void deleteById_negative_notFoundThrowsException() {
    var service = newService();
    when(repository.existsById(INSTITUTION_ID)).thenReturn(false);

    assertThatThrownBy(() -> service.deleteById(INSTITUTION_ID))
      .isInstanceOf(InstitutionNotFoundException.class)
      .hasMessageContaining(INSTITUTION_ID.toString());
  }

  // ── deleteAll ────────────────────────────────────────────────────────────────

  @Test
  void deleteAll_positive_delegatesToRepository() {
    var service = newService();

    service.deleteAll();

    verify(repository).deleteAll();
  }

  // ── helpers ──────────────────────────────────────────────────────────────────

  private InstitutionServiceImpl newService() {
    return new InstitutionServiceImpl(repository, mapper, context);
  }
}
