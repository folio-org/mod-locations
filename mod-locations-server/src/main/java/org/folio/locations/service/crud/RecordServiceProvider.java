package org.folio.locations.service.crud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class RecordServiceProvider {

  private final CampusService campusService;
  private final LibraryService libraryService;
  private final LocationService locationService;
  private final InstitutionService institutionService;
  private final ServicePointService servicePointService;
  private final ServicePointUserService servicePointUserService;
}
