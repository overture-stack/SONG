package bio.overture.song.server;

import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.DEFAULT_LIMIT;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.DEFAULT_OFFSET;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.DEFAULT_SORT_ORDER;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.LIMIT;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.OFFSET;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.SORT;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.SORTORDER;
import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.context.request.NativeWebRequest;

public class PageableResolverTest {

  @Test
  public void integerResolver_validIntegers_success() {
    val p1 = createPageable("1", null, null, null);
    assertEquals(p1.getOffset(), 1);

    val p2 = createPageable(null, "9", null, null);
    assertEquals(p2.getPageSize(), 9);
  }

  @Test
  public void sortResolver_validSorting_success() {
    val s1 =
        createPageable(null, null, "desc", "version").getSort().stream().collect(toImmutableList());
    assertEquals(s1.size(), 1);
    assertTrue(s1.contains(Sort.Order.desc("id")));

    val s2 =
        createPageable(null, null, "asc", "version").getSort().stream().collect(toImmutableList());
    assertEquals(s2.size(), 1);
    assertTrue(s2.contains(Sort.Order.asc("id")));

    val s3 =
        createPageable(null, null, "desc", "name").getSort().stream().collect(toImmutableList());
    assertEquals(s3.size(), 1);
    assertTrue(s3.contains(Sort.Order.desc("name")));

    val s4 =
        createPageable(null, null, "asc", "name").getSort().stream().collect(toImmutableList());
    assertEquals(s4.size(), 1);
    assertTrue(s4.contains(Sort.Order.asc("name")));

    val s5 =
        createPageable(null, null, "asc", "name,version").getSort().stream()
            .collect(toImmutableList());
    assertEquals(s5, newArrayList(Sort.Order.asc("name"), Sort.Order.asc("id")));

    val s6 =
        createPageable(null, null, "asc", "version,name").getSort().stream()
            .collect(toImmutableList());
    assertEquals(s6, newArrayList(Sort.Order.asc("id"), Sort.Order.asc("name")));

    val s7 =
        createPageable(null, null, "desc", "name,version").getSort().stream()
            .collect(toImmutableList());
    assertEquals(s7, newArrayList(Sort.Order.desc("name"), Sort.Order.desc("id")));

    val s8 =
        createPageable(null, null, "desc", "version,name").getSort().stream()
            .collect(toImmutableList());
    assertEquals(s8, newArrayList(Sort.Order.desc("id"), Sort.Order.desc("name")));

    val s9 =
        createPageable(null, null, "DESC", "version,name").getSort().stream()
            .collect(toImmutableList());
    assertEquals(s9, newArrayList(Sort.Order.desc("id"), Sort.Order.desc("name")));

    val s10 =
        createPageable(null, null, "ASC", "version,name").getSort().stream()
            .collect(toImmutableList());
    assertEquals(s10, newArrayList(Sort.Order.asc("id"), Sort.Order.asc("name")));
  }

  @Test
  public void sortResolver_invalidSorting_malformedParameter() {
    // Test invalid variables
    assertSongError(() -> createPageable(null, null, null, "id,name"), MALFORMED_PARAMETER);
    assertSongError(() -> createPageable(null, null, null, "version,name3"), MALFORMED_PARAMETER);
    assertSongError(() -> createPageable(null, null, null, "ver-sion"), MALFORMED_PARAMETER);
    assertSongError(() -> createPageable(null, null, null, "id"), MALFORMED_PARAMETER);

    // Test invalid sort order
    assertSongError(() -> createPageable(null, null, "assc3", "version,name"), MALFORMED_PARAMETER);
  }

  @Test
  public void integerResolver_invalidIntegers_malformedParameter() {
    // Negative
    assertSongError(() -> createPageable("-1", null, null, null), MALFORMED_PARAMETER);
    assertSongError(() -> createPageable(null, "-1", null, null), MALFORMED_PARAMETER);
    assertSongError(() -> createPageable("-1", "-1", null, null), MALFORMED_PARAMETER);

    // String
    assertSongError(() -> createPageable("notAnInt", null, null, null), MALFORMED_PARAMETER);
    assertSongError(() -> createPageable(null, "notAnInt", null, null), MALFORMED_PARAMETER);
    assertSongError(() -> createPageable("notAnInt", "notAnInt", null, null), MALFORMED_PARAMETER);

    // decimal
    assertSongError(() -> createPageable("1.0", null, null, null), MALFORMED_PARAMETER);
    assertSongError(() -> createPageable(null, "1.0", null, null), MALFORMED_PARAMETER);
    assertSongError(() -> createPageable("1.0", "1.0", null, null), MALFORMED_PARAMETER);
  }

  @Test
  public void resolver_defaults_success() {
    val expectedDefaults =
        TestData.builder()
            .offset(DEFAULT_OFFSET)
            .pageSize(DEFAULT_LIMIT)
            .sortOrder(DEFAULT_SORT_ORDER)
            .sortVariables("id")
            .build();

    val pageableUsingNulls = createPageable(null, null, null, null);
    assertEqualTo(expectedDefaults, pageableUsingNulls);

    val pageableUsingEmpty = createPageable("", "", "", "");
    assertEqualTo(expectedDefaults, pageableUsingEmpty);
  }

  private static NativeWebRequest createMockNativeRequest(
      String offset, String limit, String sort, String sortOrder) {
    val nativeWebRequest = mock(NativeWebRequest.class);
    when(nativeWebRequest.getParameter(OFFSET)).thenReturn(offset);
    when(nativeWebRequest.getParameter(LIMIT)).thenReturn(limit);
    when(nativeWebRequest.getParameter(SORT)).thenReturn(sort);
    when(nativeWebRequest.getParameter(SORTORDER)).thenReturn(sortOrder);
    return nativeWebRequest;
  }

  private static void assertEqualTo(TestData testData, Pageable pageable) {
    assertEquals(pageable.getPageSize(), (int) testData.getPageSize());
    assertEquals(pageable.getOffset(), (long) testData.getOffset());
    assertEquals(pageable.getSort(), testData.getSort());
  }

  @SneakyThrows
  private static Pageable createPageable(
      String offset, String limit, String sortOrder, String sortVariables) {
    val resolver = new AnalysisTypePageableResolver();
    val mockNativeRequest = createMockNativeRequest(offset, limit, sortVariables, sortOrder);
    val out = resolver.resolveArgument(null, null, mockNativeRequest, null);
    assertNotNull(out);
    assertTrue(out instanceof AnalysisTypePageableResolver.AnalysisTypePageable);
    return (AnalysisTypePageableResolver.AnalysisTypePageable) out;
  }

  @Value
  @Builder
  public static class TestData {
    @NonNull private final Integer pageSize;
    @NonNull private final Integer offset;
    @NonNull private final Sort.Direction sortOrder;
    @NonNull private final String sortVariables;

    public Sort getSort() {
      return new Sort(sortOrder, sortVariables);
    }
  }
}
