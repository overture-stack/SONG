package org.icgc.dcc.song.server.service;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerError;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.converter.FileConverter;
import org.icgc.dcc.song.server.model.StorageObject;
import org.icgc.dcc.song.server.model.analysis.AbstractAnalysis;
import org.icgc.dcc.song.server.model.entity.file.impl.File;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.model.enums.AnalysisTypes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.MISMATCHING_STORAGE_OBJECT_CHECKSUMS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.MISMATCHING_STORAGE_OBJECT_SIZES;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.MISSING_STORAGE_OBJECTS;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.core.utils.RandomGenerator.randomList;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.PUBLISHED;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.UNPUBLISHED;
import static org.icgc.dcc.song.server.model.enums.FileTypes.BAM;
import static org.icgc.dcc.song.server.model.enums.FileTypes.VCF;
import static org.icgc.dcc.song.server.service.PublishAnalysisTest.RangeType.ALL;
import static org.icgc.dcc.song.server.service.PublishAnalysisTest.RangeType.NONE;
import static org.icgc.dcc.song.server.service.PublishAnalysisTest.RangeType.SOME;
import static org.icgc.dcc.song.server.utils.AnalysisGenerator.createAnalysisGenerator;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@Transactional
public class PublishAnalysisTest {

  private static final String STORAGE_SERVICE = "storageService";
  private static final int MAX_FILES = 10;
  private static final int MIN_SIZE = 2;
  private static final List<File> EMPTY_FILE_LIST = ImmutableList.of();
  private static final String DEFAULT_ACCESS_TOKEN = "myAccessToken";

  @Autowired
  AnalysisService service;

  @Autowired
  FileService fileService;

  @Autowired
  StudyService studyService;

  private RandomGenerator randomGenerator;

  /**
   * State
   */
  private List<File> testFiles;
  private AbstractAnalysis testAnalysis;
  private String testAnalysisId;
  private String testStudyId;

  /**
   * Before each test, create a new analysis, with a fresh set of randomly generated files,
   * that will be used in the test
   */
  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    this.randomGenerator = createRandomGenerator(PublishAnalysisTest.class.getSimpleName());
    val analysisGenerator = createAnalysisGenerator(DEFAULT_STUDY_ID, service, randomGenerator);

    this.testAnalysis = analysisGenerator.createDefaultRandomAnalysis(randomGenerator.randomEnum(AnalysisTypes.class));
    this.testAnalysisId = testAnalysis.getAnalysisId();
    this.testStudyId = testAnalysis.getStudy();

    // Delete any previous files
    fileService.securedDelete(DEFAULT_STUDY_ID,
        testAnalysis.getFile().stream()
            .map(File::getObjectId)
            .collect(toList()));

    this.testFiles = generateFiles(MAX_FILES, testAnalysis );
    assertThat(testFiles).hasSize(MAX_FILES);
    assertThat(MIN_SIZE).isLessThan(MAX_FILES);
  }

  /**
   * Table showing the tests for the publish service. The middle columns represent the state of the StorageObjects located on the storage server side. The "rangeTypes
   * are ALL, NONE, and SOME. ALL represents the entire set of input files. SOME represents a randomly sized set of the input files bounded by [2,maxsize).
   * NONE represents an empty set. These range types model the practical states of the storage server, and therefore are used to test how the SONG server will
   * publish given these states. The reference is the input SONG file list, which is complete, and contains all correct information.
   *
   * For example, for the SomeUndefinedMismatchingSize test, ALL the SONG files exist on the storage server as StorageObjects
   * (i.e there is one StorageObject for each File), and of those StorageObjects that exist SOME of them have UNDEFINED md5 checksum values.
   * Of those that have DEFINED MD5 checksum values, NONE of them have mismatching MD5 checksums with their associated SONG File.
   * However, of all of the StorageObjects, only SOME of them have mismatching sizes with their associated SONG file. The expected result for this, is for an
   * MISMATCHING_STORAGE_OBJECT_SIZES ServerError to be thrown
   *   +------------------------------------+----------+--------------+--------------+----------------+------------------------------------------------------------+
   *   |                                    |          |              |              |                |                                                            |
   *   |              testname              | existing | undefinedMd5 |mismatchingMd5| mismatchingSize|                       ExpectedResult                       |
   *   |                                    |          |              |  if defined  |    if existing |                                                            |
   *   +------------------------------------+----------+--------------+--------------+----------------+------------------------------------------------------------+
   *   | Ideal                              | all      | none         | none         | none           | published state                                            |
   *   | MismatchingSize                    | all      | none         | none         | some           | unpublishedState, and MISMATCHING_STORAGE_OBJECT_SIZES     |
   *   | MismatchingMd5                     | all      | none         | some         | none           | unpublishedState, and MISMATCHING_STORAGE_OBJECT_CHECKSUMS |
   *   | MismatchingMd5AndSize              | all      | none         | some         | some           | unpublishedState, and MISMATCHING_STORAGE_OBJECT_SIZES     |
   *   | SomeUndefinedMd5                   | all      | some         | none         | none           | published state                                            |
   *   | SomeUndefinedMismatchingSize       | all      | some         | none         | some           | unpublishedState, and MISMATCHING_STORAGE_OBJECT_SIZES     |
   *   | SomeUndefinedMismatchingMd5        | all      | some         | some         | none           | unpublishedState, and MISMATCHING_STORAGE_OBJECT_CHECKSUMS |
   *   | SomeUndefinedMismatchingMd5AndSize | all      | some         | some         | some           | unpublishedState, and MISMATCHING_STORAGE_OBJECT_SIZES     |
   *   | AllUndefined                       | all      | all          | X            | none           | published state                                            |
   *   | AllUndefinedMismatchingSize        | all      | all          | X            | some           | unpublishedState, and MISMATCHING_STORAGE_OBJECT_SIZES     |
   *   | SomeExisting                       | some     | X            | X            | X              | unpublishedState, MISSING_STORAGE_OBJECTS                  |
   *   | NoneExisting                       | none     | X            | X            | X              | unpublishedState, MISSING_STORAGE_OBJECTS                  |
   *   +------------------------------------+----------+--------------+--------------+----------------+------------------------------------------------------------+
   */


  @Test
	public void testIdeal(){
    setupTest(ALL, NONE, NONE, NONE);
    assertAllPublish();
	}

	@Test
	public void testMismatchingSize(){
    setupTest(ALL, NONE, NONE, SOME);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_SIZES);
	}

	@Test
	public void testMismatchingMd5(){
    setupTest(ALL, NONE, SOME, NONE);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_CHECKSUMS);
	}

	@Test
	public void testMismatchingMd5AndSize(){
    setupTest(ALL, NONE, SOME, SOME);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_SIZES);
	}

	@Test
	public void testSomeUndefinedMd5(){
    setupTest(ALL, SOME, NONE, NONE);
    assertPublishError(MISMATCHING_STORAGE_OBJECT_CHECKSUMS, false);

    assertPublish(true);
	}

	@Test
	public void testSomeUndefinedMismatchingSize(){
    setupTest(ALL, SOME, NONE, SOME);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_SIZES);
	}

	@Test
	public void testSomeUndefinedMismatchingMd5(){
    setupTest(ALL, SOME, SOME, NONE);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_CHECKSUMS);
	}

	@Test
	public void testSomeUndefinedMismatchingMd5AndSize(){
    setupTest(ALL, SOME, SOME, SOME);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_SIZES);
	}

	@Test
	public void testAllUndefined(){
    setupTest(ALL, ALL, NONE, NONE);
    assertPublishError(MISMATCHING_STORAGE_OBJECT_CHECKSUMS, false);
    assertPublish(true);
	}

	@Test
	public void testAllUndefinedMismatchingSize(){
    setupTest(ALL, ALL, NONE, SOME);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_SIZES);
	}

	@Test
	public void testSomeExisting(){
    setupTest(SOME, NONE, NONE, NONE);
    assertAllPublishErrors(MISSING_STORAGE_OBJECTS);
	}

	@Test
	public void testNoneExisting(){
    setupTest(NONE, NONE, NONE, NONE);
    assertAllPublishErrors(MISSING_STORAGE_OBJECTS);
	}

  private void assertAllPublish(){
    assertPublish(true, false);
  }

  /**
   * Publishes the analysis and asserts the correct state for this test, for each of the ignoreUndefinedMd5 arguments
   * @param ignoreUndefinedMd5Options a non-empty set of true and/or false values
   */
  private void assertPublish(Boolean ... ignoreUndefinedMd5Options){
    val options = newHashSet(ignoreUndefinedMd5Options);
    assertThat(options.size()).isGreaterThan(0);
    for(val ignoreUndefinedMd5: options){
      //change state to unpublished
      service.securedUpdateState(testStudyId, testAnalysisId, UNPUBLISHED);

      // Test publish changes to published state
      assertThat(service.readState(testAnalysisId)).isEqualTo(UNPUBLISHED);
      service.publish(DEFAULT_ACCESS_TOKEN, testStudyId, testAnalysisId, ignoreUndefinedMd5 );
      assertThat(service.readState(testAnalysisId)).isEqualTo(PUBLISHED);
    }
  }

  private void assertAllPublishErrors(ServerError expectedServerError){
    assertPublishError(expectedServerError, true, false);
  }

  /**
   * Attempts to publish, and assert the {@param expectedServerError}
   * {@link org.icgc.dcc.song.core.exceptions.ServerErrors} is thrown, for each of the ignoreUndefinedMd5 arguments
   */
  private void assertPublishError(ServerError expectedServerError, Boolean ... ignoreUndefinedMd5Options){
    assertThat(service.readState(testAnalysisId)).isEqualTo(UNPUBLISHED);
    val options = newHashSet(ignoreUndefinedMd5Options);
    assertThat(options.size()).isGreaterThan(0);
    for(val ignoreUndefinedMd5 : options){
      assertSongError(() -> service.publish(DEFAULT_ACCESS_TOKEN, testStudyId, testAnalysisId, ignoreUndefinedMd5 ),
          expectedServerError);
    }
    assertThat(service.readState(testAnalysisId)).isEqualTo(UNPUBLISHED);
  }

  enum RangeType{
    ALL,
    SOME,
    NONE;
  }

  /**
   * Generates the correct StorageObjects given the input SONG files used in this test, and then creates a mock
   * storage service. That StorageService is then forcefully injected into the analysisService using reflection magic.
   * This is neccessary, since mocking the analysisService is currently an tedious task. The use of reflection to
   * replace a dependency is dirty, but completely effective for the purpose of this test.
   */
  private void setupTest(RangeType existingRange, RangeType undefinedMd5Range,
      RangeType mismatchingMd5Range, RangeType mismatchingSizeRange){
    val generator = StorageObjectGenerator.builder()
        .files(testFiles)
        .existingRange(existingRange)
        .undefinedMd5Range(undefinedMd5Range)
        .mismatchingMd5Range(mismatchingMd5Range)
        .mismatchingSizeRange(mismatchingSizeRange)
        .randomGenerator(randomGenerator)
        .build();
    val storageObjects = generator.generateStorageObjects();
    val storageObjectIds = storageObjects.stream().map(StorageObject::getObjectId).collect(toImmutableSet());
    val nonExistingObjectIds = testFiles.stream()
        .map(File::getObjectId)
        .filter(x -> !storageObjectIds.contains(x))
        .collect(toImmutableList());

    val mockStorageService = mock(StorageService.class);

    for(val storageObject : storageObjects){
      when(mockStorageService.downloadObject(DEFAULT_ACCESS_TOKEN, storageObject.getObjectId())).thenReturn(storageObject);
      when(mockStorageService.isObjectExist(DEFAULT_ACCESS_TOKEN, storageObject.getObjectId())).thenReturn(true);
    }
    for(val objectId : nonExistingObjectIds){
      when(mockStorageService.isObjectExist(DEFAULT_ACCESS_TOKEN, objectId)) .thenReturn(false);
    }
    ReflectionTestUtils.setField(service, STORAGE_SERVICE, mockStorageService);
  }

  public List<File> generateFiles(int maxSize, AbstractAnalysis a){
    return randomList(() -> generateFile(a), maxSize);
  }

  /**
   * Generate a random file given an input analysis
   */
  public File generateFile(AbstractAnalysis a){
    val analysisType = AnalysisTypes.resolveAnalysisType(a.getAnalysisType()) ;
    String fileType = null;
    String fileName = randomGenerator.generateRandomUUIDAsString()+".";

    if (analysisType == AnalysisTypes.SEQUENCING_READ){
      fileType = BAM.toString();
      fileName += BAM.getExtension();
    } else if (analysisType == AnalysisTypes.VARIANT_CALL){
      fileType = VCF.toString();
      fileName += VCF.getExtension()+".gz";
    }
    val file = File.builder()
        .studyId(a.getStudy())
        .analysisId(a.getAnalysisId())
        .fileType(fileType)
        .fileAccess(randomGenerator.randomEnum(AccessTypes.class).toString())
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .fileName(fileName)
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .build();
    fileService.create(a.getAnalysisId(), a.getStudy(), file);
    return file;
  }

  /**
   * This generator is responsible for converting the input constraints
   * (i.e the different rangeTypes for each characteristic of the storage service) to a single list of StorageObjects
   * reflecting the constraints.
   */
  @Value
  @Builder
  public static class StorageObjectGenerator {

    private static final FileConverter FILE_CONVERTER = Mappers.getMapper(FileConverter.class);

    @NonNull private final List<File> files;
    @NonNull private final RangeType existingRange;
    @NonNull private final RangeType undefinedMd5Range;
    @NonNull private final RangeType mismatchingMd5Range;
    @NonNull private final RangeType mismatchingSizeRange;
    @NonNull private final RandomGenerator randomGenerator;

    public List<StorageObject> generateStorageObjects(){
      val existingFiles = getExistingFiles();
      val mismatchingSizeFiles = processMismatchingSize(existingFiles);
      val undefinedFiles = processUndefinedMd5Files(existingFiles);
      val definedFiles = getDefinedMd5Files(existingFiles, undefinedFiles);
      val mismatchingMd5Files = processMismatchingMd5Files(definedFiles);
      return existingFiles.stream()
          .map(FILE_CONVERTER::toStorageObject)
          .collect(toImmutableList());
    }

    private List<File> processMismatchingMd5Files(List<File> definedMd5Files){
      val maxtry = 10000;
      val out = ImmutableList.<File>builder();
      for (val file: filter(definedMd5Files, mismatchingMd5Range)){
        int trynum = 0;
        String candidateMd5 = randomGenerator.generateRandomMD5();
        boolean ismatching = true;
        while(ismatching){
          checkState(trynum < maxtry, "could not find a nonmatching md5 after %s tries", maxtry);
          candidateMd5= randomGenerator.generateRandomMD5();
          ismatching = candidateMd5.equals(file.getFileMd5sum());
          trynum++;
        }
        file.setFileMd5sum(candidateMd5);
        out.add(file);
      }
      return out.build();
    }

    private List<File> getDefinedMd5Files(List<File> existingFiles, List<File> undefinedFiles){
      val set = newHashSet(undefinedFiles);
      return existingFiles.stream()
          .filter(x -> !set.contains(x))
          .collect(toImmutableList());
    }

    private List<File> getExistingFiles(){
      return FILE_CONVERTER.copyFiles(filter(files, existingRange));
    }

    private List<File> processUndefinedMd5Files(List<File> existingFiles){
      return filter(existingFiles, undefinedMd5Range).stream()
          .peek(x -> x.setFileMd5sum(null))
          .collect(toImmutableList());
    }

    private List<File> processMismatchingSize(List<File> existingFiles){
      return filter(existingFiles, mismatchingSizeRange).stream()
          .peek(x -> x.setFileSize(x.getFileSize()+777))
          .collect(toImmutableList());
    }

    private List<File> getSome(List<File> input, int maxSize){
      val size = randomGenerator.generateRandomIntRange(MIN_SIZE, maxSize);
      return randomGenerator.randomSublist(input, size);
    }

    private List<File> filter(List<File> input, RangeType rangeType){
      if (rangeType == ALL){
        return input;
      } else if (rangeType == SOME) {
        return getSome(input, input.size());
      } else if (rangeType== NONE){
        return EMPTY_FILE_LIST;
      }
      throw new IllegalStateException("should not be here");
    }
  }

}
