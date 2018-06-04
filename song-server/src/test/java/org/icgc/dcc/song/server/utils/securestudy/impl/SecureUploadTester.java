package org.icgc.dcc.song.server.utils.securestudy.impl;

import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.service.StudyService;
import org.icgc.dcc.song.server.service.UploadService;
import org.icgc.dcc.song.server.utils.securestudy.AbstractSecureTester;
import org.icgc.dcc.song.server.utils.securestudy.SecureTestData;

import java.util.List;
import java.util.function.BiConsumer;

import static org.assertj.core.util.Lists.newArrayList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UPLOAD_ID_NOT_FOUND;
import static org.icgc.dcc.song.core.utils.JsonUtils.readTree;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.server.utils.TestFiles.getJsonNodeFromClasspath;

public class SecureUploadTester extends AbstractSecureTester {
  private static final String SEQUENCING_READ_DEFAULT_FILENAME = "documents/sequencingread-valid.json";
  private static final String VARIANT_CALL_DEFAULT_FILENAME = "documents/variantcall-valid.json";
  private static final List<String> FILENAMES = newArrayList(SEQUENCING_READ_DEFAULT_FILENAME, VARIANT_CALL_DEFAULT_FILENAME);

  private final UploadService uploadService;

  private SecureUploadTester(RandomGenerator randomGenerator,
      StudyService studyService,
      UploadService uploadService) {
    super(randomGenerator, studyService, UPLOAD_ID_NOT_FOUND);
    this.uploadService = uploadService;
  }

  public SecureTestData runSecureTest(BiConsumer<String, String> biConsumer){
    return runSecureTest(biConsumer, new Object());
  }

  @Override protected boolean isIdExist(String id) {
    return uploadService.isUploadExist(id);
  }

  @SneakyThrows
  @Override protected String createId(String existingStudyId, Object context) {
    val payload = getJsonNodeFromClasspath(getRandomGenerator().randomElement(FILENAMES));
    val response = uploadService.upload(existingStudyId, toJson(payload), false);
    val id = readTree(response.getBody()).path("uploadId").textValue();
    return id;
  }

  public static SecureUploadTester createSecureUploadTester(RandomGenerator randomGenerator,
      StudyService studyService,
      UploadService uploadService) {
    return new SecureUploadTester(randomGenerator, studyService, uploadService);
  }

}
