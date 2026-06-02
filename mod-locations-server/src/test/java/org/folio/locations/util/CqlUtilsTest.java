package org.folio.locations.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.z3950.zing.cql.CQLRelation;
import org.z3950.zing.cql.CQLTermNode;

@UnitTest
class CqlUtilsTest {

  private static final String ALL_RECORDS_CQL = "cql.allRecords = 1";

  // --- parseOrAllRecords ---

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void parseOrAllRecords_nullOrBlank_returnsAllRecordsNode(String query) {
    var result = CqlUtils.parseOrAllRecords(query);

    assertThat(result.toCQL()).isEqualTo(ALL_RECORDS_CQL);
  }

  @Test
  void parseOrAllRecords_validQuery_returnsNormalizedNode() {
    var result = CqlUtils.parseOrAllRecords("name=\"test\"");

    assertThat(result.toCQL()).isEqualTo("name = test");
  }

  @Test
  void parseOrAllRecords_invalidCql_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> CqlUtils.parseOrAllRecords("(unclosed"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Invalid CQL query: (unclosed")
      .hasCauseInstanceOf(org.z3950.zing.cql.CQLParseException.class);
  }

  // --- normalize ---

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void normalize_nullOrBlank_returnsAllRecordsString(String query) {
    assertThat(CqlUtils.normalize(query)).isEqualTo(ALL_RECORDS_CQL);
  }

  @Test
  void normalize_validQuery_returnsCqlString() {
    assertThat(CqlUtils.normalize("name==\"test\"")).isEqualTo("name == test");
  }

  // --- appendNotFilter (String overload) ---

  @Test
  void appendNotFilter_nullQuery_returnsAllRecordsNotFilter() {
    var result = CqlUtils.appendNotFilter(null, "ecsRequestRouting", "true");

    assertThat(result).isEqualTo("(cql.allRecords = 1) not (ecsRequestRouting = true)");
  }

  @Test
  void appendNotFilter_validQuery_appendsNotFilter() {
    var result = CqlUtils.appendNotFilter("name=\"test\"", "ecsRequestRouting", "true");

    assertThat(result).isEqualTo("(name = test) not (ecsRequestRouting = true)");
  }

  @Test
  void appendNotFilter_queryWithSortby_preservesSortClause() {
    var result = CqlUtils.appendNotFilter("name=\"test\" sortby id", "ecsRequestRouting", "true");

    assertThat(result).isEqualTo("(name = test) not (ecsRequestRouting = true) sortby id");
  }

  @Test
  void appendNotFilter_invalidQuery_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> CqlUtils.appendNotFilter("(unclosed", "index", "term"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Invalid CQL query");
  }

  // --- appendNotFilter (CQLNode overload) ---

  @Test
  void appendNotFilter_withFilterNode_appendsNotFilter() {
    var filterNode = new CQLTermNode("status", new CQLRelation("="), "inactive");

    var result = CqlUtils.appendNotFilter("name=\"test\"", filterNode);

    assertThat(result).isEqualTo("(name = test) not (status = inactive)");
  }

  @Test
  void appendNotFilter_withFilterNodeAndSortby_preservesSortClause() {
    var filterNode = new CQLTermNode("deleted", new CQLRelation("="), "true");

    var result = CqlUtils.appendNotFilter("name=\"test\" sortby name/ascending", filterNode);

    assertThat(result).isEqualTo("(name = test) not (deleted = true) sortby name/ascending");
  }

  // --- appendAndFilter (String overload) ---

  @Test
  void appendAndFilter_nullQuery_returnsAllRecordsAndFilter() {
    var result = CqlUtils.appendAndFilter(null, "status", "active");

    assertThat(result).isEqualTo("(cql.allRecords = 1) and (status = active)");
  }

  @Test
  void appendAndFilter_validQuery_appendsAndFilter() {
    var result = CqlUtils.appendAndFilter("name=\"test\"", "status", "active");

    assertThat(result).isEqualTo("(name = test) and (status = active)");
  }

  @Test
  void appendAndFilter_queryWithSortby_preservesSortClause() {
    var result = CqlUtils.appendAndFilter("name=\"test\" sortby name/ascending", "status", "active");

    assertThat(result).isEqualTo("(name = test) and (status = active) sortby name/ascending");
  }

  @Test
  void appendAndFilter_invalidQuery_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> CqlUtils.appendAndFilter("(unclosed", "index", "term"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Invalid CQL query");
  }

  // --- appendAndFilter (CQLNode overload) ---

  @Test
  void appendAndFilter_withFilterNode_appendsAndFilter() {
    var filterNode = new CQLTermNode("type", new CQLRelation("="), "library");

    var result = CqlUtils.appendAndFilter("name=\"test\"", filterNode);

    assertThat(result).isEqualTo("(name = test) and (type = library)");
  }

  @Test
  void appendAndFilter_withFilterNodeAndSortby_preservesSortClause() {
    var filterNode = new CQLTermNode("type", new CQLRelation("="), "library");

    var result = CqlUtils.appendAndFilter("name=\"test\" sortby id", filterNode);

    assertThat(result).isEqualTo("(name = test) and (type = library) sortby id");
  }
}
