package org.folio.locations.util;

import java.io.IOException;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import org.z3950.zing.cql.CQLAndNode;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLNotNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;
import org.z3950.zing.cql.CQLRelation;
import org.z3950.zing.cql.CQLSortNode;
import org.z3950.zing.cql.CQLTermNode;
import org.z3950.zing.cql.ModifierSet;

@UtilityClass
public class CqlUtils {

  private static final CQLNode ALL_RECORDS_NODE =
    new CQLTermNode("cql.allRecords", new CQLRelation("="), "1");

  public static String normalize(@Nullable String query) {
    return parseOrAllRecords(query).toCQL();
  }

  /**
   * Parses a CQL query string. If the query is {@code null} or blank, returns a {@code cql.allRecords = 1} node.
   */
  public static CQLNode parseOrAllRecords(@Nullable String query) {
    if (query == null || query.isBlank()) {
      return ALL_RECORDS_NODE;
    }
    try {
      return new CQLParser(CQLParser.V1POINT1SORT).parse(query);
    } catch (CQLParseException | IOException e) {
      throw new IllegalArgumentException("Invalid CQL query: " + query, e);
    }
  }

  /**
   * Parses the query, ANDs it with the given filter node, preserves any {@code sortby} clause,
   * and returns the resulting CQL string. Parse errors are wrapped in {@link IllegalArgumentException}.
   */
  public static String appendAndFilter(@Nullable String query, CQLNode filterNode) {
    return combineWithSort(parseOrAllRecords(query), filterNode, false);
  }

  public static String appendAndFilter(@Nullable String query, String index, String term) {
    var filterNode = new CQLTermNode(index, new CQLRelation("="), term);
    return appendAndFilter(query, filterNode);
  }

  /**
   * Parses the query, applies {@code NOT} with the given filter node, preserves any {@code sortby} clause,
   * and returns the resulting CQL string. Parse errors are wrapped in {@link IllegalArgumentException}.
   */
  public static String appendNotFilter(@Nullable String query, CQLNode filterNode) {
    return combineWithSort(parseOrAllRecords(query), filterNode, true);
  }

  public static String appendNotFilter(@Nullable String query, String index, String term) {
    var filterNode = new CQLTermNode(index, new CQLRelation("="), term);
    return appendNotFilter(query, filterNode);
  }

  private static String combineWithSort(CQLNode base, CQLNode filter, boolean negate) {
    var sortNode = base instanceof CQLSortNode sn ? sn : null;
    var mainNode = sortNode != null ? sortNode.getSubtree() : base;

    var combined = negate
                   ? new CQLNotNode(mainNode, filter, new ModifierSet("not"))
                   : new CQLAndNode(mainNode, filter, new ModifierSet("and"));

    if (sortNode != null) {
      var withSort = new CQLSortNode(combined);
      sortNode.getSortIndexes().forEach(withSort::addSortIndex);
      return withSort.toCQL();
    }
    return combined.toCQL();
  }
}
