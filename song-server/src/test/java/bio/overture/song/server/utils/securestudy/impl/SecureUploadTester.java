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

package bio.overture.song.server.utils.securestudy.impl;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.UploadService;
import bio.overture.song.server.utils.securestudy.AbstractSecureTester;
import bio.overture.song.server.utils.securestudy.SecureTestData;
import lombok.SneakyThrows;
import lombok.val;

import java.util.List;
import java.util.function.BiConsumer;

import static com.google.common.collect.Lists.newArrayList;
import static bio.overture.song.core.exceptions.ServerErrors.UPLOAD_ID_NOT_FOUND;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;
import static bio.overture.song.server.utils.generator.PayloadGenerator.updateStudyInPayload;

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
    updateStudyInPayload(payload, existingStudyId);
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
