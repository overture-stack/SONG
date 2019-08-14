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

package bio.overture.song.server.repository.search;

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
import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.fail;
import static org.icgc.dcc.common.core.util.Joiners.PATH;
import static bio.overture.song.server.utils.TestFiles.SEARCH_TEST_DIR;
import static bio.overture.song.server.repository.search.SearchQueryBuilder.createSearchQueryBuilder;
import static bio.overture.song.server.repository.search.SearchTerm.createMultiSearchTerms;
import static bio.overture.song.server.repository.search.SearchTerm.createSearchTerm;
import static bio.overture.song.server.repository.search.SearchTerm.parseSearchTerm;
import static bio.overture.song.server.repository.search.SearchTerm.parseSearchTerms;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;

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
    assertEquals(st.getLeafKey(),"c");
    assertEquals(st.getValue(),"k");
  }

  @Test
  public void testMultiSearchTerms(){
    val stList = createMultiSearchTerms("a.x.y", Lists.newArrayList("b", "c", "d"));
    assertThat(stList).hasSize(3);
    val st0 = stList.get(0);
    val st1 = stList.get(1);
    val st2 = stList.get(2);
    assertEquals(st0.getKey(),"a.x.y");
    assertEquals(st1.getKey(),"a.x.y");
    assertEquals(st2.getKey(),"a.x.y");

    assertEquals(st0.getValue(),"b");
    assertEquals(st1.getValue(),"c");
    assertEquals(st2.getValue(),"d");
  }


  @Test
  public void testSearchTermStringParsing(){
    val stOneSeparator= parseSearchTerm("a=b");
    assertEquals(stOneSeparator.getKey(),"a");
    assertEquals(stOneSeparator.getValue(),"b");

    val stManySeparators = parseSearchTerm("a=b=c");
    assertEquals(stManySeparators.getKey(),"a");
    assertEquals(stManySeparators.getValue(),"b=c");

    val stList = parseSearchTerms("a=b", "c.d.e=f", "h.i=r=t");
    assertThat(stList).hasSize(3);

    val st0 = stList.get(0);
    assertEquals(st0.getKey(),"a");
    assertEquals(st0.getValue(),"b");

    val st1 = stList.get(1);
    assertEquals(st1.getKey(),"c.d.e");
    assertEquals(st1.getValue(),"f");

    val st2 = stList.get(2);
    assertEquals(st2.getKey(),"h.i");
    assertEquals(st2.getValue(),"r=t");
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
    assertEquals(query,getExpectedTestQuery());
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
