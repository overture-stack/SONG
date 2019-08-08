package bio.overture.song.server.controller;

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
  // parsing integer (both limit and offset)
  //  - test when "" output is default
  //  - test when null output is default
  //  - test normal value
  //  - error test decimal, MALFORMED_PARAMETER
  //  - error test string, MALFORMED_PARAMETER
  //  - error test negative int, MALFORMED_PARAMETER
  // parse variable
  //  - test when "" output is default
  //  - test when null output is default
  //  - error test outside of allowed range (not version or name) MALFORMED_PARAMETER kk
  //  - test when input version, output is id
  //  - test when input is csv, output array
  // parse direction:
  //  - test when "" output is default
  //  - test when null output is default
  //  - test lower case asc/desc
  //  - test upper case asc/desc
  //  - error test non asc or desc, MALFORMED_PARAMETER

  @Test
  public void resolver_validIntegers_success() {
    val p1 = createPageable("1", null, null, null);
    assertThat(p1.getOffset()).isEqualTo(1);

    val p2 = createPageable(null, "9", null, null);
    assertThat(p2.getPageSize()).isEqualTo(9);
  }

  @Test
  public void resolver_invalidIntegers_malformedParameter(){
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
