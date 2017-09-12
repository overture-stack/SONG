package org.icgc.dcc.song.server.repository.search;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.icgc.dcc.song.server.repository.search.SearchQueryBuilder.createSearchQueryBuilder;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.createMultiSearchTerms;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.createSearchTerm;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.parseSearchTerm;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.parseSearchTerms;

@Slf4j
public class QueryBuildingTests {

  private static final String BEGINNING_PORTION = "SELECT analysis.id AS analysis_id ";
  private static final String INCLUDED_INFO_PORTION = ", info.info AS info " ;
  private static final String MIDDLE_PORTION =
      "FROM analysis INNER JOIN info ON analysis.id = info.id WHERE info.id_type = 'Analysis'";
  private static final String WITH_CONDITIONS = " AND info.info->>'key1' ~ '.*value1$' AND info.info->'key2'->'key3'->>'key4' ~ '" + ".*value2\\d+';";
  private static final String WITHOUT_CONDITIONS = ";";

  @Test
  public void testSearchQueryEmptyNoInfo(){
    val query = createQuery(false, true);
    assertThat(query).isEqualTo(BEGINNING_PORTION+MIDDLE_PORTION+WITHOUT_CONDITIONS);
  }

  @Test
  public void testSearchQueryEmptyIncludeInfo(){
    val query = createQuery(true, true);
    assertThat(query).isEqualTo(BEGINNING_PORTION+INCLUDED_INFO_PORTION+MIDDLE_PORTION+WITHOUT_CONDITIONS);
  }

  @Test
  public void testSearchQueryBasicIncludeInfo(){
    val query = createQuery(true, false);
    assertThat(query).isEqualTo(BEGINNING_PORTION+INCLUDED_INFO_PORTION+MIDDLE_PORTION+WITH_CONDITIONS);
  }

  @Ignore
  @Test
  public void testCaseInsensitive(){
    fail("case insensitive not implemented yet");
  }

  @Test
  public void testSearchQueryBasicNoInfo(){
    val query = createQuery(false, false);
    assertThat(query).isEqualTo(BEGINNING_PORTION+MIDDLE_PORTION+WITH_CONDITIONS);
  }

  @Test
  public void testSearchTermKeyParsing(){
    val st = createSearchTerm("a.b.c", "k");
    assertThat(st.getNonLeafKeys()).isSubsetOf("a","b");
    assertThat(st.getLeafKey()).isEqualTo("c");
    assertThat(st.getValue()).isEqualTo("k");
  }

  @Test
  public void testMultiSearchTerms(){
    val stList = createMultiSearchTerms("a.x.y", Lists.newArrayList("b", "c", "d"));
    assertThat(stList).hasSize(3);
    val st0 = stList.get(0);
    val st1 = stList.get(1);
    val st2 = stList.get(2);
    assertThat(st0.getKey()).isEqualTo("a.x.y");
    assertThat(st1.getKey()).isEqualTo("a.x.y");
    assertThat(st2.getKey()).isEqualTo("a.x.y");

    assertThat(st0.getValue()).isEqualTo("b");
    assertThat(st1.getValue()).isEqualTo("c");
    assertThat(st2.getValue()).isEqualTo("d");
  }


  @Test
  public void testSearchTermStringParsing(){
    val stOneSeparator= parseSearchTerm("a=b");
    assertThat(stOneSeparator.getKey()).isEqualTo("a");
    assertThat(stOneSeparator.getValue()).isEqualTo("b");

    val stManySeparators = parseSearchTerm("a=b=c");
    assertThat(stManySeparators.getKey()).isEqualTo("a");
    assertThat(stManySeparators.getValue()).isEqualTo("b=c");

    val stList = parseSearchTerms("a=b", "c.d.e=f", "h.i=r=t");
    assertThat(stList).hasSize(3);

    val st0 = stList.get(0);
    assertThat(st0.getKey()).isEqualTo("a");
    assertThat(st0.getValue()).isEqualTo("b");

    val st1 = stList.get(1);
    assertThat(st1.getKey()).isEqualTo("c.d.e");
    assertThat(st1.getValue()).isEqualTo("f");

    val st2 = stList.get(2);
    assertThat(st2.getKey()).isEqualTo("h.i");
    assertThat(st2.getValue()).isEqualTo("r=t");
  }

  private static String createQuery(boolean includeInfoField, boolean isEmpty){
    val searchQueryBuilder = createSearchQueryBuilder(includeInfoField);
    if (!isEmpty){
      searchQueryBuilder.add("key1", ".*value1$");
      searchQueryBuilder.add("key2.key3.key4", ".*value2\\d+");
    }
    val query = searchQueryBuilder.build();
    log.debug(query);
    return query;
  }



}
