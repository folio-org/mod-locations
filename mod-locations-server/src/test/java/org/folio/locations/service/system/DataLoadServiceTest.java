package org.folio.locations.service.system;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.service.crud.CampusService;
import org.folio.locations.service.crud.InstitutionService;
import org.folio.locations.service.crud.LibraryService;
import org.folio.locations.service.crud.LocationService;
import org.folio.locations.service.crud.ServicePointService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DataLoadServiceTest {

  private static final String SERVICE_POINTS_PATTERN = "classpath:reference-data/service-points/*.json";
  private static final String INSTITUTIONS_PATTERN = "classpath:sample-data/institutions/*.json";
  private static final String CAMPUSES_PATTERN = "classpath:sample-data/campuses/*.json";
  private static final String LIBRARIES_PATTERN = "classpath:sample-data/libraries/*.json";
  private static final String LOCATIONS_PATTERN = "classpath:sample-data/locations/*.json";

  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private ResourcePatternResolver resourcePatternResolver;
  @Mock
  private InstitutionService institutionService;
  @Mock
  private CampusService campusService;
  @Mock
  private LibraryService libraryService;
  @Mock
  private LocationService locationService;
  @Mock
  private ServicePointService servicePointService;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(objectMapper, resourcePatternResolver,
      institutionService, campusService, libraryService, locationService, servicePointService);
  }

  private DataLoadService newService() {
    return new DataLoadService(objectMapper, resourcePatternResolver,
      institutionService, campusService, libraryService, locationService, servicePointService);
  }

  // ── loadReferenceData ─────────────────────────────────────────────────────────

  @Test
  void loadReferenceData_positive_servicePointLoaded() throws Exception {
    var resource = mock(Resource.class);
    var inputStream = mock(InputStream.class);
    var servicePoint = new ServicePoint();

    when(resourcePatternResolver.getResources(SERVICE_POINTS_PATTERN))
      .thenReturn(new Resource[]{resource});
    when(resource.getInputStream()).thenReturn(inputStream);
    when(objectMapper.readValue(inputStream, ServicePoint.class)).thenReturn(servicePoint);

    newService().loadReferenceData();

    verify(servicePointService).create(servicePoint);
  }

  @Test
  void loadReferenceData_negative_resourceScanFails_returnsWithoutCallingService() throws Exception {
    when(resourcePatternResolver.getResources(SERVICE_POINTS_PATTERN))
      .thenThrow(new IOException("scan failed"));

    newService().loadReferenceData();

    verifyNoInteractions(servicePointService);
  }

  @Test
  void loadReferenceData_negative_deserializationFails_continuesWithRemainingResources() throws Exception {
    var failingResource = mock(Resource.class);
    var successResource = mock(Resource.class);
    var failingStream = mock(InputStream.class);
    var successStream = mock(InputStream.class);
    var servicePoint = new ServicePoint();

    when(resourcePatternResolver.getResources(SERVICE_POINTS_PATTERN))
      .thenReturn(new Resource[]{failingResource, successResource});
    when(failingResource.getInputStream()).thenReturn(failingStream);
    when(objectMapper.readValue(failingStream, ServicePoint.class))
      .thenThrow(new IOException("parse error"));
    when(successResource.getInputStream()).thenReturn(successStream);
    when(objectMapper.readValue(successStream, ServicePoint.class)).thenReturn(servicePoint);

    newService().loadReferenceData();

    verify(servicePointService).create(servicePoint);
  }

  @Test
  void loadReferenceData_negative_creatorThrows_continuesWithRemainingResources() throws Exception {
    var failingResource = mock(Resource.class);
    var successResource = mock(Resource.class);
    var failingStream = mock(InputStream.class);
    var successStream = mock(InputStream.class);
    var failingPoint = new ServicePoint().name("failing");
    var successPoint = new ServicePoint().name("success");

    when(resourcePatternResolver.getResources(SERVICE_POINTS_PATTERN))
      .thenReturn(new Resource[]{failingResource, successResource});
    when(failingResource.getInputStream()).thenReturn(failingStream);
    when(objectMapper.readValue(failingStream, ServicePoint.class)).thenReturn(failingPoint);
    when(successResource.getInputStream()).thenReturn(successStream);
    when(objectMapper.readValue(successStream, ServicePoint.class)).thenReturn(successPoint);
    doThrow(new RuntimeException("constraint violation")).when(servicePointService).create(failingPoint);

    newService().loadReferenceData();

    verify(servicePointService).create(successPoint);
  }

  // ── loadSampleData ────────────────────────────────────────────────────────────

  @Test
  void loadSampleData_positive_loadsAllEntitiesInDependencyOrder() throws Exception {
    var institution = new Institution();
    var campus = new Campus();
    var library = new Library();
    var location = new Location();
    var instResource = stubEntity(Institution.class, institution);
    var campResource = stubEntity(Campus.class, campus);
    var libResource = stubEntity(Library.class, library);
    var locResource = stubEntity(Location.class, location);
    when(resourcePatternResolver.getResources(INSTITUTIONS_PATTERN)).thenReturn(new Resource[]{instResource});
    when(resourcePatternResolver.getResources(CAMPUSES_PATTERN)).thenReturn(new Resource[]{campResource});
    when(resourcePatternResolver.getResources(LIBRARIES_PATTERN)).thenReturn(new Resource[]{libResource});
    when(resourcePatternResolver.getResources(LOCATIONS_PATTERN)).thenReturn(new Resource[]{locResource});

    newService().loadSampleData();

    var inOrder = inOrder(institutionService, campusService, libraryService, locationService);
    inOrder.verify(institutionService).create(institution);
    inOrder.verify(campusService).create(campus);
    inOrder.verify(libraryService).create(library);
    inOrder.verify(locationService).create(location);
  }

  @Test
  void loadSampleData_negative_institutionsScanFails_otherEntitiesStillLoaded() throws Exception {
    var campus = new Campus();
    var library = new Library();
    var location = new Location();
    var campResource = stubEntity(Campus.class, campus);
    var libResource = stubEntity(Library.class, library);
    var locResource = stubEntity(Location.class, location);
    when(resourcePatternResolver.getResources(INSTITUTIONS_PATTERN))
      .thenThrow(new IOException("scan failed"));
    when(resourcePatternResolver.getResources(CAMPUSES_PATTERN)).thenReturn(new Resource[]{campResource});
    when(resourcePatternResolver.getResources(LIBRARIES_PATTERN)).thenReturn(new Resource[]{libResource});
    when(resourcePatternResolver.getResources(LOCATIONS_PATTERN)).thenReturn(new Resource[]{locResource});

    newService().loadSampleData();

    verify(campusService).create(campus);
    verify(libraryService).create(library);
    verify(locationService).create(location);
  }

  private <T> Resource stubEntity(Class<T> type, T entity) throws IOException {
    var resource = mock(Resource.class);
    var stream = mock(InputStream.class);
    when(resource.getInputStream()).thenReturn(stream);
    when(objectMapper.readValue(stream, type)).thenReturn(entity);
    return resource;
  }
}
