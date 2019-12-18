/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.MISMATCHING_STORAGE_OBJECT_CHECKSUMS;
import static bio.overture.song.core.exceptions.ServerErrors.MISMATCHING_STORAGE_OBJECT_SIZES;
import static bio.overture.song.core.exceptions.ServerErrors.MISSING_STORAGE_OBJECTS;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.model.enums.FileTypes.BAM;
import static bio.overture.song.core.model.enums.FileTypes.VCF;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.core.utils.RandomGenerator.randomList;
import static bio.overture.song.server.service.PublishAnalysisTest.RangeType.ALL;
import static bio.overture.song.server.service.PublishAnalysisTest.RangeType.NONE;
import static bio.overture.song.server.service.PublishAnalysisTest.RangeType.SOME;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.resolveLegacyAnalysisTypeName;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bio.overture.song.core.exceptions.ServerError;
import bio.overture.song.core.exceptions.ServerErrors;
import bio.overture.song.core.model.enums.AccessTypes;
import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.converter.FileConverter;
import bio.overture.song.server.model.StorageObject;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.utils.generator.LegacyAnalysisTypeName;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.transaction.Transactional;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Transactional
public class PublishAnalysisTest {

  private static final String STORAGE_SERVICE = "storageService";
  private static final int MAX_FILES = 1 << 4;
  private static final int MIN_SIZE = 1 << 3;
  private static final List<FileEntity> EMPTY_FILE_LIST = ImmutableList.of();

  @Autowired private AnalysisService service;

  @Autowired private FileService fileService;

  @Autowired private StudyService studyService;

  private RandomGenerator randomGenerator;

  /** State */
  private List<FileEntity> testFiles;

  private Analysis testAnalysis;
  private String testAnalysisId;
  private String testStudyId;

  /**
   * Before each test, create a new analysis, with a fresh set of randomly generated files, that
   * will be used in the test
   */
  @Before
  public void beforeTest() {
    this.randomGenerator = createRandomGenerator(PublishAnalysisTest.class.getSimpleName());
    val newStudyId = createStudyGenerator(studyService, randomGenerator).createRandomStudy();
    assertTrue(studyService.isStudyExist(newStudyId));
    val analysisGenerator = createAnalysisGenerator(newStudyId, service, randomGenerator);

    this.testAnalysis =
        analysisGenerator.createDefaultRandomAnalysis(
            randomGenerator.randomEnum(LegacyAnalysisTypeName.class));
    this.testAnalysisId = testAnalysis.getAnalysisId();
    this.testStudyId = testAnalysis.getStudyId();

    // Delete any previous files
    fileService.securedDelete(
        newStudyId, testAnalysis.getFiles().stream().map(FileEntity::getObjectId).collect(toList()));

    this.testFiles = generateFiles(MAX_FILES, testAnalysis);
    assertEquals(testFiles.size(), MAX_FILES);
    assertTrue(MIN_SIZE < MAX_FILES);
  }

  /**
   * Table showing the tests for the publish service. The middle columns represent the state of the
   * StorageObjects located on the storage server side. The "rangeTypes are ALL, NONE, and SOME. ALL
   * represents the entire set of input files. SOME represents a randomly sized set of the input
   * files bounded by [2,maxsize). NONE represents an empty set. These range types model the
   * practical states of the storage server, and therefore are used to test how the SONG server will
   * publish given these states. The reference is the input SONG file list, which is complete, and
   * contains all correct information.
   *
   * <p>For example, for the SomeUndefinedMismatchingSize test, ALL the SONG files exist on the
   * storage server as StorageObjects (i.e there is one StorageObject for each File), and of those
   * StorageObjects that exist SOME of them have UNDEFINED md5 checksum values. Of those that have
   * DEFINED MD5 checksum values, NONE of them have mismatching MD5 checksums with their associated
   * SONG File. However, of all of the StorageObjects, only SOME of them have mismatching sizes with
   * their associated SONG file. The expected result for this, is for an
   * MISMATCHING_STORAGE_OBJECT_SIZES ServerError to be thrown
   * +------------------------------------+----------+--------------+--------------+----------------+------------------------------------------------------------+
   * | | | | | | | | testname | existing | undefinedMd5 |mismatchingMd5| mismatchingSize|
   * ExpectedResult | | | | | if defined | if existing | |
   * +------------------------------------+----------+--------------+--------------+----------------+------------------------------------------------------------+
   * | Ideal | all | none | none | none | published state | | MismatchingSize | all | none | none |
   * some | unpublishedState, and MISMATCHING_STORAGE_OBJECT_SIZES | | MismatchingMd5 | all | none |
   * some | none | unpublishedState, and MISMATCHING_STORAGE_OBJECT_CHECKSUMS | |
   * MismatchingMd5AndSize | all | none | some | some | unpublishedState, and
   * MISMATCHING_STORAGE_OBJECT_SIZES | | SomeUndefinedMd5 | all | some | none | none | published
   * state | | SomeUndefinedMismatchingSize | all | some | none | some | unpublishedState, and
   * MISMATCHING_STORAGE_OBJECT_SIZES | | SomeUndefinedMismatchingMd5 | all | some | some | none |
   * unpublishedState, and MISMATCHING_STORAGE_OBJECT_CHECKSUMS | |
   * SomeUndefinedMismatchingMd5AndSize | all | some | some | some | unpublishedState, and
   * MISMATCHING_STORAGE_OBJECT_SIZES | | AllUndefined | all | all | X | none | published state | |
   * AllUndefinedMismatchingSize | all | all | X | some | unpublishedState, and
   * MISMATCHING_STORAGE_OBJECT_SIZES | | SomeExisting | some | X | X | X | unpublishedState,
   * MISSING_STORAGE_OBJECTS | | NoneExisting | none | X | X | X | unpublishedState,
   * MISSING_STORAGE_OBJECTS |
   * +------------------------------------+----------+--------------+--------------+----------------+------------------------------------------------------------+
   */
  @Test
  public void testIdeal() {
    setupTest(ALL, NONE, NONE, NONE);
    assertAllPublish();
  }

  @Test
  public void testMismatchingSize() {
    setupTest(ALL, NONE, NONE, SOME);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_SIZES);
  }

  @Test
  public void testMismatchingMd5() {
    setupTest(ALL, NONE, SOME, NONE);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_CHECKSUMS);
  }

  @Test
  public void testMismatchingMd5AndSize() {
    setupTest(ALL, NONE, SOME, SOME);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_SIZES);
  }

  @Test
  public void testSomeUndefinedMd5() {
    setupTest(ALL, SOME, NONE, NONE);
    assertPublishError(MISMATCHING_STORAGE_OBJECT_CHECKSUMS, false);

    assertPublish(true);
  }

  @Test
  public void testSomeUndefinedMismatchingSize() {
    setupTest(ALL, SOME, NONE, SOME);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_SIZES);
  }

  @Test
  public void testSomeUndefinedMismatchingMd5() {
    setupTest(ALL, SOME, SOME, NONE);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_CHECKSUMS);
  }

  @Test
  public void testSomeUndefinedMismatchingMd5AndSize() {
    setupTest(ALL, SOME, SOME, SOME);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_SIZES);
  }

  @Test
  public void testAllUndefined() {
    setupTest(ALL, ALL, NONE, NONE);
    assertPublishError(MISMATCHING_STORAGE_OBJECT_CHECKSUMS, false);
    assertPublish(true);
  }

  @Test
  public void testAllUndefinedMismatchingSize() {
    setupTest(ALL, ALL, NONE, SOME);
    assertAllPublishErrors(MISMATCHING_STORAGE_OBJECT_SIZES);
  }

  @Test
  public void testSomeExisting() {
    setupTest(SOME, NONE, NONE, NONE);
    assertAllPublishErrors(MISSING_STORAGE_OBJECTS);
  }

  @Test
  public void testNoneExisting() {
    setupTest(NONE, NONE, NONE, NONE);
    assertAllPublishErrors(MISSING_STORAGE_OBJECTS);
  }

  private void assertAllPublish() {
    assertPublish(true, false);
  }

  /**
   * Publishes the analysis and asserts the correct state for this test, for each of the
   * ignoreUndefinedMd5 arguments
   *
   * @param ignoreUndefinedMd5Options a non-empty set of true and/or false values
   */
  private void assertPublish(Boolean... ignoreUndefinedMd5Options) {
    val options = newHashSet(ignoreUndefinedMd5Options);
    assertTrue(options.size() > 0);
    for (val ignoreUndefinedMd5 : options) {
      // change state to unpublished
      service.securedUpdateState(testStudyId, testAnalysisId, UNPUBLISHED);

      // Test publish changes to published state
      assertEquals(service.readState(testAnalysisId), UNPUBLISHED);
      service.publish(testStudyId, testAnalysisId, ignoreUndefinedMd5);
      assertEquals(service.readState(testAnalysisId), PUBLISHED);
    }
  }

  private void assertAllPublishErrors(ServerError expectedServerError) {
    assertPublishError(expectedServerError, true, false);
  }

  /**
   * Attempts to publish, and assert the {@param expectedServerError} {@link ServerErrors} is
   * thrown, for each of the ignoreUndefinedMd5 arguments
   */
  private void assertPublishError(
      ServerError expectedServerError, Boolean... ignoreUndefinedMd5Options) {
    assertEquals(service.readState(testAnalysisId), UNPUBLISHED);
    val options = newHashSet(ignoreUndefinedMd5Options);
    assertTrue(options.size() > 0);
    for (val ignoreUndefinedMd5 : options) {
      SongErrorAssertions.assertSongError(
          () -> service.publish(testStudyId, testAnalysisId, ignoreUndefinedMd5),
          expectedServerError);
    }
    assertEquals(service.readState(testAnalysisId), UNPUBLISHED);
  }

  enum RangeType {
    ALL,
    SOME,
    NONE;
  }

  /**
   * Generates the correct StorageObjects given the input SONG files used in this test, and then
   * creates a mock storage service. That StorageService is then forcefully injected into the
   * analysisService using reflection magic. This is neccessary, since mocking the analysisService
   * is currently an tedious task. The use of reflection to replace a dependency is dirty, but
   * completely effective for the purpose of this test.
   */
  private void setupTest(
      RangeType existingRange,
      RangeType undefinedMd5Range,
      RangeType mismatchingMd5Range,
      RangeType mismatchingSizeRange) {
    val generator =
        StorageObjectGenerator.builder()
            .files(testFiles)
            .existingRange(existingRange)
            .undefinedMd5Range(undefinedMd5Range)
            .mismatchingMd5Range(mismatchingMd5Range)
            .mismatchingSizeRange(mismatchingSizeRange)
            .randomGenerator(randomGenerator)
            .build();
    val storageObjects = generator.generateStorageObjects();
    val storageObjectIds =
        storageObjects.stream().map(StorageObject::getObjectId).collect(toImmutableSet());
    val nonExistingObjectIds =
        testFiles.stream()
            .map(FileEntity::getObjectId)
            .filter(x -> !storageObjectIds.contains(x))
            .collect(toImmutableList());

    val mockStorageService = mock(StorageService.class);

    for (val storageObject : storageObjects) {
      when(mockStorageService.downloadObject(storageObject.getObjectId()))
          .thenReturn(storageObject);
      when(mockStorageService.isObjectExist(storageObject.getObjectId())).thenReturn(true);
    }
    for (val objectId : nonExistingObjectIds) {
      when(mockStorageService.isObjectExist(objectId)).thenReturn(false);
    }
    ReflectionTestUtils.setField(service, STORAGE_SERVICE, mockStorageService);
  }

  public List<FileEntity> generateFiles(int maxSize, Analysis a) {
    return randomList(() -> generateFile(a), maxSize);
  }

  /** Generate a random file given an input analysis */
  public FileEntity generateFile(Analysis a) {
    val legacyAnalysisTypeName = resolveLegacyAnalysisTypeName(a.getAnalysisSchema().getName());
    String fileType = null;
    String fileName = randomGenerator.generateRandomUUIDAsString() + ".";

    if (legacyAnalysisTypeName == LegacyAnalysisTypeName.SEQUENCING_READ) {
      fileType = BAM.toString();
      fileName += BAM.getExtension();
    } else if (legacyAnalysisTypeName == LegacyAnalysisTypeName.VARIANT_CALL) {
      fileType = VCF.toString();
      fileName += VCF.getExtension() + ".gz";
    }
    val file =
        FileEntity.builder()
            .studyId(a.getStudyId())
            .analysisId(a.getAnalysisId())
            .fileType(fileType)
            .fileAccess(randomGenerator.randomEnum(AccessTypes.class).toString())
            .fileMd5sum(randomGenerator.generateRandomMD5())
            .fileName(fileName)
            .fileSize((long) randomGenerator.generateRandomIntRange(1, 100000))
            .objectId(randomGenerator.generateRandomUUIDAsString())
            .build();
    fileService.create(a.getAnalysisId(), a.getStudyId(), file);
    return file;
  }

  /**
   * This generator is responsible for converting the input constraints (i.e the different
   * rangeTypes for each characteristic of the storage service) to a single list of StorageObjects
   * reflecting the constraints.
   */
  @Value
  @Builder
  public static class StorageObjectGenerator {

    private static final FileConverter FILE_CONVERTER = FileConverter.INSTANCE;

    @NonNull private final List<FileEntity> files;
    @NonNull private final RangeType existingRange;
    @NonNull private final RangeType undefinedMd5Range;
    @NonNull private final RangeType mismatchingMd5Range;
    @NonNull private final RangeType mismatchingSizeRange;
    @NonNull private final RandomGenerator randomGenerator;

    public List<StorageObject> generateStorageObjects() {
      val existingFiles = getExistingFiles();
      val mismatchingSizeFiles = processMismatchingSize(existingFiles);
      val undefinedFiles = processUndefinedMd5Files(existingFiles);
      val definedFiles = getDefinedMd5Files(existingFiles, undefinedFiles);
      val mismatchingMd5Files = processMismatchingMd5Files(definedFiles);
      return existingFiles.stream().map(FILE_CONVERTER::toStorageObject).collect(toImmutableList());
    }

    private List<FileEntity> processMismatchingMd5Files(List<FileEntity> definedMd5Files) {
      val maxtry = 10000;
      val out = ImmutableList.<FileEntity>builder();
      for (val file : filter(definedMd5Files, mismatchingMd5Range)) {
        int trynum = 0;
        String candidateMd5 = randomGenerator.generateRandomMD5();
        boolean ismatching = true;
        while (ismatching) {
          checkState(trynum < maxtry, "could not find a nonmatching md5 after %s tries", maxtry);
          candidateMd5 = randomGenerator.generateRandomMD5();
          ismatching = candidateMd5.equals(file.getFileMd5sum());
          trynum++;
        }
        file.setFileMd5sum(candidateMd5);
        out.add(file);
      }
      return out.build();
    }

    private List<FileEntity> getDefinedMd5Files(
        List<FileEntity> existingFiles, List<FileEntity> undefinedFiles) {
      val set = newHashSet(undefinedFiles);
      return existingFiles.stream().filter(x -> !set.contains(x)).collect(toImmutableList());
    }

    private List<FileEntity> getExistingFiles() {
      return FILE_CONVERTER.copyFiles(filter(files, existingRange));
    }

    private List<FileEntity> processUndefinedMd5Files(List<FileEntity> existingFiles) {
      return filter(existingFiles, undefinedMd5Range).stream()
          .peek(x -> x.setFileMd5sum(null))
          .collect(toImmutableList());
    }

    private List<FileEntity> processMismatchingSize(List<FileEntity> existingFiles) {
      return filter(existingFiles, mismatchingSizeRange).stream()
          .peek(x -> x.setFileSize(x.getFileSize() + 777))
          .collect(toImmutableList());
    }

    private List<FileEntity> getSome(List<FileEntity> input) {
      assertTrue(input.size() >= 2);
      val size = input.size() / 2;
      return randomGenerator.randomSublist(input, size);
    }

    private List<FileEntity> filter(List<FileEntity> input, RangeType rangeType) {
      if (rangeType == ALL) {
        return input;
      } else if (rangeType == SOME) {
        return getSome(input);
      } else if (rangeType == NONE) {
        return EMPTY_FILE_LIST;
      }
      throw new IllegalStateException("should not be here");
    }
  }
}
