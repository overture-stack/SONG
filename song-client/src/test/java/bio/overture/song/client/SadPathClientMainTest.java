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

package bio.overture.song.client;

import bio.overture.song.client.cli.ClientMain;
import bio.overture.song.client.config.CustomRestClientConfig;
import bio.overture.song.core.model.Analysis;
import bio.overture.song.core.model.AnalysisType;
import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.model.Donor;
import bio.overture.song.core.model.FileDTO;
import bio.overture.song.core.model.FileUpdateRequest;
import bio.overture.song.core.model.FileUpdateResponse;
import bio.overture.song.core.model.PageDTO;
import bio.overture.song.core.model.Sample;
import bio.overture.song.core.model.Specimen;
import bio.overture.song.core.model.SubmitResponse;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.sdk.ManifestClient;
import bio.overture.song.sdk.SongApi;
import bio.overture.song.sdk.Toolbox;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Lombok;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static bio.overture.song.core.model.ExportedPayload.createExportedPayload;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.JsonUtils.objectToTree;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class SadPathClientMainTest extends AbstractClientMainTest {

  private static final String DUMMY_STUDY_ID = "ABC123";

  @Mock private SongApi songApi;
  @Mock private CustomRestClientConfig customRestClientConfig;

  @Override
  protected ClientMain getClientMain() {
    // Needs to be a new instance, to avoid appending status
    return new ClientMain(customRestClientConfig, songApi, new ManifestClient(songApi));
  }

  @Test
  public void testExitCodeOnError() {
    when(songApi.isAlive()).thenThrow(new IllegalStateException("a test error"));
    val e1 = executeMain("ping");
    assertTrue(getExitCode() == 1);
  }

}
