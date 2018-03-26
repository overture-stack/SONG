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

package org.icgc.dcc.song.importer;

import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.util.stream.Collectors;
import org.icgc.dcc.song.importer.dao.dcc.impl.DccMetadataMemoryDao;
import org.icgc.dcc.song.importer.persistence.ObjectPersistance;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.importer.convert.DccMetadataUrlConverter.createDccMetadataUrlConverter;
import static org.icgc.dcc.song.importer.download.DownloadIterator.createDownloadIterator;
import static org.icgc.dcc.song.importer.download.urlgenerator.impl.DccMetadataUrlGenerator.createDccMetadataUrlGenerator;
import static org.icgc.dcc.song.importer.model.DccMetadata.createDccMetadata;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.OPEN;

@Slf4j
public class DccMetadataTest {

  @Test
  @SneakyThrows
  public void testDccMetadataSerialization(){
    val expectedDccMetadata = createDccMetadata("myId", OPEN, 23423423, "myFilename", "myGnosId", "myProjectCode");
    val path = Paths.get("./testDccMetadata.dat");
    ObjectPersistance.store(expectedDccMetadata, path );
    val actualDccMetadata = ObjectPersistance.restore(path);
    assertThat(actualDccMetadata).isEqualTo(expectedDccMetadata);
    Files.deleteIfExists(path);
  }

  @Test
  @Ignore
  public void testDccMetadataDownloader(){
    val urlGenerator = createDccMetadataUrlGenerator("https://meta.icgc.org");
    val urlConverter  = createDccMetadataUrlConverter();
    val downloadIterator = createDownloadIterator(urlConverter,urlGenerator, 2000, 2000, 0);
    val datas = downloadIterator.stream().collect(Collectors.toImmutableSet());
    val dao = DccMetadataMemoryDao.createDccMetadataMemoryDao(datas);
    val results = dao.findByMultiObjectIds(Sets.newHashSet("0185e7b8-fc93-5fde-999d-614884f4f798",
        "01864257-6b3a-5493-85b8-9d9e805c9c41"));
    log.info("sdf");

  }


}
