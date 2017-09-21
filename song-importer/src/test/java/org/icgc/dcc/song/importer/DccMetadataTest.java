package org.icgc.dcc.song.importer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.icgc.dcc.song.importer.config.DccMetadataConfig;
import org.icgc.dcc.song.importer.config.DccStorageConfig;
import org.icgc.dcc.song.importer.dao.dcc.impl.DccMetadataDbDao;
import org.icgc.dcc.song.importer.download.fetcher.DccMetadataFetcher;
import org.icgc.dcc.song.importer.persistence.ObjectPersistance;
import org.icgc.dcc.song.importer.resolvers.AccessTypes;
import org.icgc.dcc.song.importer.storage.SimpleDccStorageClient;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.icgc.dcc.song.importer.Factory.DATA_CONTAINER_FILE_RESTORER;
import static org.icgc.dcc.song.importer.Factory.buildFileFilter;
import static org.icgc.dcc.song.importer.model.DccMetadata.createDccMetadata;
import static org.icgc.dcc.song.importer.persistence.PersistenceFactory.createPersistenceFactory;

@SpringBootTest(classes = {DccStorageConfig.class, DccMetadataConfig.class, Config.class})
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
public class DccMetadataTest {

  @Autowired
  private DccMetadataDbDao dccMetadataDbDao;

  @Autowired
  private DccStorageConfig dccStorageConfig;

  @Autowired
  private DccMetadataFetcher dccMetadataFetcher;

  @Autowired
  private Factory factory;

  @Autowired
  private SimpleDccStorageClient simpleDccStorageClient;


  @Test
  @SneakyThrows
  public void testDccMetadataSerialization(){
    val expectedDccMetadata = createDccMetadata("myCls","myId", AccessTypes.OPEN, 23423423, "myFilename", "myGnosId", "myProjectCode");
    val path = Paths.get("./testDccMetadata.dat");
    ObjectPersistance.store(expectedDccMetadata, path );
    val actualDccMetadata = ObjectPersistance.restore(path);
    Assertions.assertThat(actualDccMetadata).isEqualTo(expectedDccMetadata);
    Files.deleteIfExists(path);
  }

  @Ignore
  @Test
  public void testStorage(){
    val objectId1 = "e2918e9d-a558-50cd-b199-a16b318de283";
    val file = simpleDccStorageClient.getFile(objectId1, "N/A", "NA");
    log.info("sdfsdf");
  }

  @Ignore
  @Test
  public void test1(){
    val objectId1 = "e2918e9d-a558-50cd-b199-a16b318de283";
    val objectId2 = "2f96e364-edf9-5f21-9e22-2f1fe80d4779";
    val d = dccMetadataDbDao.findByObjectId(objectId1);
    val dd = dccMetadataDbDao.findByMultiObjectIds(objectId1, objectId2);

    val fileFilter = buildFileFilter();
    log.info("Persisting or fetching data...");
    val dataFetcher = factory.buildDataFetcher();
    val persistenceFactory = createPersistenceFactory(DATA_CONTAINER_FILE_RESTORER, dataFetcher::fetchData);
    val dataContainer = persistenceFactory.getObject("dataContainer.dat");

    log.info("Sdfsdf");


  }

}
