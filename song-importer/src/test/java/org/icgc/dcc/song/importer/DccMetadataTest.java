package org.icgc.dcc.song.importer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.config.DccMetadataConfig;
import org.icgc.dcc.song.importer.dao.DccMetadataDbDao;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@Slf4j
@ContextConfiguration(classes = DccMetadataConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DccMetadataTest {

  @Autowired
  private DccMetadataDbDao dccMetadataDbDao;

  @Test
  @Ignore
  public void test1(){
    val d = dccMetadataDbDao.findByObjectId("e2918e9d-a558-50cd-b199-a16b318de283");
    val dd = dccMetadataDbDao.findByMultiObjectIds("e2918e9d-a558-50cd-b199-a16b318de283", "2f96e364-edf9-5f21-9e22-2f1fe80d4779");

    log.info("Sdfsdf");


  }

}
