/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.icgc.dcc.song.server.repository.search;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.icgc.dcc.common.core.util.Joiners.PATH;
import static org.icgc.dcc.song.server.repository.search.SearchQueryBuilder.createSearchQueryBuilder;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.createMultiSearchTerms;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.createSearchTerm;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.parseSearchTerm;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.parseSearchTerms;
import static org.icgc.dcc.song.server.utils.TestFiles.SEARCH_TEST_DIR;
import static org.icgc.dcc.song.server.utils.TestFiles.getJsonNodeFromClasspath;

@Slf4j
public class QueryBuildingTests {

  private static final String STUDY_ID = "ABC123";
  private static final String TEST_NAME = "testName";
  private static final String QUERY = "query";
  private static final String KEY_CHAIN1 = "key1";
  private static final String VALUE1 = ".*value1$";
  private static final String KEY_CHAIN2= "key2.key3.key4";
  private static final String VALUE2 = ".*value2\\d+";
  private static final String EXPECTED_QUERIES_PATHNAME = "expectedSearchQueries.json";
  private static Map<String, String > EXPECTED_TEST_QUERY_MAP = newHashMap();

  @Rule
  public TestName testName = new TestName();

  @BeforeClass
  @SneakyThrows
  public static void init(){
    val json = getJsonNodeFromClasspath(PATH.join(SEARCH_TEST_DIR, EXPECTED_QUERIES_PATHNAME));
    for(val testData : json){
      val testname = testData.path(TEST_NAME).textValue().replaceAll(".*/", "");
      val expectedQuery = testData.path(QUERY).textValue();
      checkNotNull(testname);
      checkNotNull(expectedQuery);
      checkState(!EXPECTED_TEST_QUERY_MAP.containsKey(testname),
          "The golden json test fixture '%s' should not have duplicate entries of '%s'",
          EXPECTED_QUERIES_PATHNAME, testname );
      EXPECTED_TEST_QUERY_MAP.put(testname, expectedQuery);
    }
  }

  @Test
  public void testSearchQueryEmptyNoInfo(){
    runTest(false, true);
  }

  @Test
  public void testSearchQueryEmptyIncludeInfo(){
    runTest(true, true);
  }

  @Test
  public void testSearchQueryBasicIncludeInfo(){
    runTest(true, false);
  }

  @Ignore
  @Test
  public void testCaseInsensitive(){
    fail("case insensitive not implemented yet");
  }

  @Test
  public void testSearchQueryBasicNoInfo(){
    runTest(false, false);
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

  private String getCurrentTestName(){
    return testName.getMethodName();
  }

  private String getExpectedTestQuery(){
    val thisTestName = getCurrentTestName();
    checkState(EXPECTED_TEST_QUERY_MAP.containsKey(thisTestName),
        "The expected query for this testname '%s' does not exist in the golden json test fixture '%s'",
        thisTestName, EXPECTED_QUERIES_PATHNAME );
    return  EXPECTED_TEST_QUERY_MAP.get(thisTestName);
  }

  private void runTest(boolean includeInfoField, boolean isEmpty){
    val query = createQuery(STUDY_ID, includeInfoField, isEmpty);
    assertThat(query).isEqualTo(getExpectedTestQuery());
  }

  private String createQuery(String studyId, boolean includeInfoField, boolean isEmpty){
    val searchQueryBuilder = createSearchQueryBuilder(studyId, includeInfoField);
    if (!isEmpty){
      searchQueryBuilder.add(KEY_CHAIN1, VALUE1);
      searchQueryBuilder.add(KEY_CHAIN2, VALUE2);
    }
    val query = searchQueryBuilder.build();
    log.debug("{}: {}",getCurrentTestName(), query);
    return query;
  }

}
