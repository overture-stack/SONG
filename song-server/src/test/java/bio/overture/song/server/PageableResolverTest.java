package bio.overture.song.server;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.DEFAULT_LIMIT;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.DEFAULT_OFFSET;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.DEFAULT_SORT_ORDER;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.LIMIT;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.OFFSET;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.SORT;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.SORTORDER;

public class PageableResolverTest {

  @Test
  public void integerResolver_validIntegers_success() {
    val p1 = createPageable("1", null, null, null);
    assertThat(p1.getOffset()).isEqualTo(1);

    val p2 = createPageable(null, "9", null, null);
    assertThat(p2.getPageSize()).isEqualTo(9);
  }

  @Test
  public void sortResolver_validSorting_success(){
    val s1 = createPageable(null, null, "desc", "version").getSort();
    assertThat(s1.stream()).containsExactly(Sort.Order.desc("id"));

    val s2 = createPageable(null, null, "asc", "version").getSort();
    assertThat(s2.stream()).containsExactly(Sort.Order.asc("id"));

    val s3 = createPageable(null, null, "desc", "name").getSort();
    assertThat(s3.stream()).containsExactly(Sort.Order.desc("name"));

    val s4 = createPageable(null, null, "asc", "name").getSort();
    assertThat(s4.stream()).containsExactly(Sort.Order.asc("name"));

    val s5 = createPageable(null, null, "asc", "name,version").getSort();
    assertThat(s5.stream()).containsExactlyInAnyOrder(Sort.Order.asc("name"), Sort.Order.asc("id"));

    val s6 = createPageable(null, null, "asc", "version,name").getSort();
    assertThat(s6.stream()).containsExactlyInAnyOrder(Sort.Order.asc("name"), Sort.Order.asc("id"));

    val s7 = createPageable(null, null, "desc", "name,version").getSort();
    assertThat(s7.stream()).containsExactlyInAnyOrder(Sort.Order.desc("name"), Sort.Order.desc("id"));

    val s8 = createPageable(null, null, "desc", "version,name").getSort();
    assertThat(s8.stream()).containsExactlyInAnyOrder(Sort.Order.desc("name"), Sort.Order.desc("id"));

    val s9 = createPageable(null, null, "DESC", "version,name").getSort();
    assertThat(s9.stream()).containsExactlyInAnyOrder(Sort.Order.desc("name"), Sort.Order.desc("id"));

    val s10 = createPageable(null, null, "ASC", "version,name").getSort();
    assertThat(s10.stream()).containsExactlyInAnyOrder(Sort.Order.asc("name"), Sort.Order.asc("id"));
  }

  @Test
  public void sortResolver_invalidSorting_malformedParameter(){
    // Test invalid variables
    assertSongError(() ->  createPageable(null, null, null, "id,name"), MALFORMED_PARAMETER);
    assertSongError(() ->  createPageable(null, null, null, "version,name3"), MALFORMED_PARAMETER);
    assertSongError(() ->  createPageable(null, null, null, "ver-sion"), MALFORMED_PARAMETER);
    assertSongError(() ->  createPageable(null, null, null, "id"), MALFORMED_PARAMETER);

    // Test invalid sort order
    assertSongError(() ->  createPageable(null, null, "assc3", "version,name"), MALFORMED_PARAMETER);
  }

  @Test
  public void integerResolver_invalidIntegers_malformedParameter(){
    // Negative
    assertSongError(() -> createPageable("-1", null, null, null), MALFORMED_PARAMETER );
    assertSongError(() -> createPageable(null, "-1", null, null), MALFORMED_PARAMETER );
    assertSongError(() -> createPageable("-1", "-1", null, null), MALFORMED_PARAMETER );

    // String
    assertSongError(() -> createPageable("notAnInt", null, null, null), MALFORMED_PARAMETER );
    assertSongError(() -> createPageable(null, "notAnInt", null, null), MALFORMED_PARAMETER );
    assertSongError(() -> createPageable("notAnInt", "notAnInt", null, null), MALFORMED_PARAMETER );

    // decimal
    assertSongError(() -> createPageable("1.0", null, null, null), MALFORMED_PARAMETER );
    assertSongError(() -> createPageable(null, "1.0", null, null), MALFORMED_PARAMETER );
    assertSongError(() -> createPageable("1.0", "1.0", null, null), MALFORMED_PARAMETER );
  }

  @Test
  public void resolver_defaults_success(){
    val expectedDefaults = TestData.builder()
        .offset(DEFAULT_OFFSET)
        .pageSize(DEFAULT_LIMIT)
        .sortOrder(DEFAULT_SORT_ORDER)
        .sortVariables("id")
        .build();

    val pageableUsingNulls = createPageable(null, null, null, null);
    assertEqualTo(expectedDefaults , pageableUsingNulls );

    val pageableUsingEmpty = createPageable("", "", "", "");
    assertEqualTo(expectedDefaults , pageableUsingEmpty);
  }

  private static NativeWebRequest createMockNativeRequest(String offset, String limit, String sort, String sortOrder){
    val nativeWebRequest = mock(NativeWebRequest.class);
    when(nativeWebRequest.getParameter(OFFSET)).thenReturn(offset);
    when(nativeWebRequest.getParameter(LIMIT)).thenReturn(limit);
    when(nativeWebRequest.getParameter(SORT)).thenReturn(sort);
    when(nativeWebRequest.getParameter(SORTORDER)).thenReturn(sortOrder);
    return nativeWebRequest;
  }

  private static void assertEqualTo(TestData testData, Pageable pageable){
    assertThat(pageable.getPageSize()).isEqualTo(testData.getPageSize());
    assertThat(pageable.getOffset()).isEqualTo((long)testData.getOffset());
    assertThat(pageable.getSort()).isEqualTo(testData.getSort());
  }

  @SneakyThrows
  private static Pageable createPageable(String offset, String limit, String sortOrder, String sortVariables){
    val resolver = new AnalysisTypePageableResolver();
    val mockNativeRequest = createMockNativeRequest(offset, limit, sortVariables, sortOrder);
    val out = resolver.resolveArgument(null, null, mockNativeRequest, null );
    assertThat(out).isNotNull();
    assertThat(out).isInstanceOf(AnalysisTypePageableResolver.AnalysisTypePageable.class);
    return (AnalysisTypePageableResolver.AnalysisTypePageable)out;
  }

  @Value
  @Builder
  public static class TestData {
    @NonNull private final Integer pageSize;
    @NonNull private final Integer offset;
    @NonNull private final Sort.Direction sortOrder;
    @NonNull private final String sortVariables;

    public Sort getSort(){
      return new Sort(sortOrder, sortVariables);
    }
  }

}
