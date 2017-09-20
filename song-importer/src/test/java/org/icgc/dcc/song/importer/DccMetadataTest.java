package org.icgc.dcc.song.importer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.config.DccMetadataConfig;
import org.icgc.dcc.song.importer.config.DccStorageConfig;
import org.icgc.dcc.song.importer.dao.dcc.impl.DccMetadataDbDao;
import org.icgc.dcc.song.importer.download.fetcher.DccMetadataFetcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.icgc.dcc.song.importer.Factory.DATA_CONTAINER_FILE_RESTORER;
import static org.icgc.dcc.song.importer.Factory.buildDataFetcher;
import static org.icgc.dcc.song.importer.Factory.buildFileFilter;
import static org.icgc.dcc.song.importer.persistence.PersistenceFactory.createPersistenceFactory;

@SpringBootTest
@Slf4j
@ContextConfiguration(classes = {DccMetadataConfig.class, DccStorageConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class DccMetadataTest {

  @Autowired
  private DccMetadataDbDao dccMetadataDbDao;

  @Autowired
  private DccStorageConfig dccStorageConfig;

  @Autowired
  private DccMetadataFetcher dccMetadataFetcher;



  @Test
  public void test1(){
    val objectId1 = "e2918e9d-a558-50cd-b199-a16b318de283";
    val objectId2 = "2f96e364-edf9-5f21-9e22-2f1fe80d4779";
    val d = dccMetadataDbDao.findByObjectId(objectId1);
    val dd = dccMetadataDbDao.findByMultiObjectIds(objectId1, objectId2);

    val fileFilter = buildFileFilter();
    log.info("Persisting or fetching data...");
    val dataFetcher = buildDataFetcher();
    val persistenceFactory = createPersistenceFactory(DATA_CONTAINER_FILE_RESTORER, dataFetcher::fetchData);
    val dataContainer = persistenceFactory.getObject("dataContainer.dat");
    val filteredPortalFileMetadataList = fileFilter.passList(dataContainer.getPortalFileMetadataList());
    val files = dccMetadataFetcher.fetchDccMetadataFiles(filteredPortalFileMetadataList);

    log.info("Sdfsdf");


  }

}
