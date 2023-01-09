package bio.overture.song.core.utils;

import static bio.overture.song.core.utils.JsonUtils.mapper;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;

import bio.overture.song.core.model.PageDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public class Deserialization {
  private static final ObjectMapper MAPPER = mapper();

  @SneakyThrows
  public static <T> PageDTO<T> deserializePage(
      @NonNull String body, @NonNull Class<T> contentType) {
    val reader = MAPPER.readerFor(new TypeReference<PageDTO<T>>() {});
    val erasedPage = reader.<PageDTO<T>>readValue(body);
    val contents =
        erasedPage.getResultSet().stream()
            .map(x -> MAPPER.convertValue(x, contentType))
            .collect(toUnmodifiableList());
    val pageDTO = new PageDTO<T>();
    pageDTO.setOffset(erasedPage.getOffset());
    pageDTO.setLimit(erasedPage.getLimit());
    pageDTO.setCount(erasedPage.getCount());
    pageDTO.setResultSet(contents);
    return pageDTO;
  }

  @SneakyThrows
  public static <T> List<T> deserializeList(@NonNull String body, @NonNull Class<T> contentType) {
    val reader = MAPPER.readerFor(new TypeReference<List<T>>() {});
    val erasedList = reader.<List<T>>readValue(body);
    return erasedList.stream()
        .map(x -> MAPPER.convertValue(x, contentType))
        .collect(toUnmodifiableList());
  }
}
