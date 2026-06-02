package org.folio.locations.service.crud;

import java.util.List;

/**
 * Common object for collections of resources.
 *
 * @param resources list of DTO resources
 * @param totalRecords total number of resources available
 * @param <D> DTO type
 */
public record ResourceCollection<D>(List<D> resources, int totalRecords) { }
