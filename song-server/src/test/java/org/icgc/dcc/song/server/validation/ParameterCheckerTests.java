package org.icgc.dcc.song.server.validation;

import com.google.common.collect.Sets;
import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerErrors;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.legacy.LegacyDto;
import org.icgc.dcc.song.server.utils.ParameterChecker;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ILLEGAL_FILTER_PARAMETER;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ILLEGAL_QUERY_PARAMETER;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.utils.ParameterChecker.createParameterChecker;
import static org.icgc.dcc.song.server.utils.TestFiles.assertSetsMatch;

public class ParameterCheckerTests {


  private static final Class<?> REGISTERED_TYPE = LegacyDto.class;
  private static final Class<?> UNREGISTERED_TYPE = Donor.class;
  private ParameterChecker parameterChecker = createParameterChecker(REGISTERED_TYPE);
  private static Set<String> expectedFieldNames = newHashSet("id", "fileName", "access", "projectCode", "gnosId");
  private final RandomGenerator randomGenerator = createRandomGenerator(ParameterCheckerTests.class.getSimpleName());

  @Test
  public void testFieldNameExtraction(){
    val actualFieldNames = parameterChecker.getFieldNamesFor(REGISTERED_TYPE);
    assertSetsMatch(actualFieldNames, expectedFieldNames);
    assertSongError(() -> parameterChecker.getFieldNamesFor(UNREGISTERED_TYPE),
        ServerErrors.UNREGISTERED_TYPE);
  }

  @Test
  public void testFieldNameValidator(){
    val fieldNames = randomGenerator.randomSublist(newArrayList(expectedFieldNames));
    val result = parameterChecker.isLegal(REGISTERED_TYPE, newHashSet(fieldNames));
    assertThat(result).isTrue();

    val corruptedFieldNameSet = buildCorruptedFieldNameSet(fieldNames);

    val result2 = parameterChecker.isLegal(REGISTERED_TYPE, corruptedFieldNameSet);
    assertThat(result2).isFalse();

    assertSongError(() -> parameterChecker.isLegal(UNREGISTERED_TYPE, newHashSet(fieldNames)),
        ServerErrors.UNREGISTERED_TYPE);

    val result3 = parameterChecker.isLegal(REGISTERED_TYPE, newHashSet());
    assertThat(result3).isTrue();
  }

  @Test
  public void testQueryChecker(){
    val fieldNames = randomGenerator.randomSublist(newArrayList(expectedFieldNames));
    parameterChecker.checkQueryParameters(REGISTERED_TYPE, newHashSet(fieldNames));

    //Test empty
    parameterChecker.checkQueryParameters(REGISTERED_TYPE, newHashSet());

    assertSongError(() -> parameterChecker.checkQueryParameters(UNREGISTERED_TYPE, newHashSet(fieldNames)),
        ServerErrors.UNREGISTERED_TYPE);

    val corruptedFieldNameSet = buildCorruptedFieldNameSet(fieldNames);

    assertSongError(() -> parameterChecker.checkQueryParameters(REGISTERED_TYPE, newHashSet(corruptedFieldNameSet)),
        ILLEGAL_QUERY_PARAMETER);
  }

  @Test
  public void testFilterChecker(){
    val fieldNames = randomGenerator.randomSublist(newArrayList(expectedFieldNames));
    parameterChecker.checkFilterParameters(REGISTERED_TYPE, newHashSet(fieldNames));

    //Test empty
    parameterChecker.checkFilterParameters(REGISTERED_TYPE, newHashSet());

    assertSongError(() -> parameterChecker.checkFilterParameters(UNREGISTERED_TYPE, newHashSet(fieldNames)),
        ServerErrors.UNREGISTERED_TYPE);

    val corruptedFieldNameSet = buildCorruptedFieldNameSet(fieldNames);

    assertSongError(() -> parameterChecker.checkFilterParameters(REGISTERED_TYPE, newHashSet(corruptedFieldNameSet)),
        ILLEGAL_FILTER_PARAMETER);
  }

  private Set<String> buildCorruptedFieldNameSet(List<String> fieldNames){
    val randomIndex = randomGenerator.generateRandomIntRange(0, fieldNames.size());
    val corruptedFieldNameSet = Sets.<String>newHashSet();
    for (int i =0; i<fieldNames.size(); i++){
      if (i == randomIndex){
        corruptedFieldNameSet.add(fieldNames.get(i)+"_nonsense");
      } else {
        corruptedFieldNameSet.add(fieldNames.get(i));
      }
    }
    return corruptedFieldNameSet;
  }

}
