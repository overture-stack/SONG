package org.icgc.dcc.song.importer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.persistence.ObjectPersistance;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.importer.model.DccMetadata.createDccMetadata;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.OPEN;

@Slf4j
public class DccMetadataTest {

  @Test
  @SneakyThrows
  public void testDccMetadataSerialization(){
    val expectedDccMetadata = createDccMetadata("myCls","myId", OPEN, 23423423, "myFilename", "myGnosId", "myProjectCode");
    val path = Paths.get("./testDccMetadata.dat");
    ObjectPersistance.store(expectedDccMetadata, path );
    val actualDccMetadata = ObjectPersistance.restore(path);
    assertThat(actualDccMetadata).isEqualTo(expectedDccMetadata);
    Files.deleteIfExists(path);
  }


}
