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

import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static bio.overture.song.server.utils.TestFiles.getInfoName;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.utils.RandomGenerator;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.CannotCreateTransactionException;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.jdbc.ContainerDatabaseDriver;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"test"})
public class InterruptedDatabaseConnectionTest {

  private static final String JDBC_URL_CONTAINER_CACHE_FIELD_NAME = "jdbcUrlContainerCache";

  @Autowired private StudyService service;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(StudyServiceTest.class.getSimpleName());

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Value("${spring.datasource.driver-class-name}")
  private String driverClassName;

  @Test
  @SneakyThrows
  public void testInterruptDBConnection() {
    testThatServiceWorks();
    val dataSourceContainer = getDataSourceContainer();
    // simulate a db outage
    stopDB(dataSourceContainer);
    assertDatabaseIsUnreachable();
    resumeDB(dataSourceContainer);
    // make sure the app service recovered and able to connect
    testThatServiceReallyWorks();
  }

  private void testThatServiceWorks() {
    // Random test copied from StudyServiceTest;
    // it gets data from postgres when the database is up.
    // Unfortunately, it doesn't return anything when it hangs...
    val study = service.read("ABC123");
    assertNotNull(study);
    assertEquals(study.getStudyId(), "ABC123");
    assertEquals(study.getName(), "X1-CA");
    assertEquals(study.getDescription(), "A fictional study");
    assertEquals(study.getOrganization(), "Sample Data Research Institute");
    assertEquals(getInfoName(study), "study1");
  }

  private void assertDatabaseIsUnreachable() {
    boolean exceptionCaught = false;
    try {
      // we expect this to fail since the connection we have got closed and the
      // pool won't be able to obtain a connection.
      testThatServiceWorks();
    } catch (CannotCreateTransactionException e) {
      exceptionCaught = true;
    }
    assertTrue(
        "No exception caught while connecting to db while supposed to be down", exceptionCaught);
  }

  private void testThatServiceReallyWorks() {
    val studyIds = service.findAllStudies();
    assertThat(studyIds, hasItems(DEFAULT_STUDY_ID, "XYZ234"));
    val studyId = createStudyGenerator(service, randomGenerator).createRandomStudy();
    val study = service.read(studyId);
    val studyIds2 = service.findAllStudies();
    assertThat(studyIds2, hasItems(DEFAULT_STUDY_ID, "XYZ234", study.getStudyId()));
  }

  /**
   * Returns a reference to the current db docker container (this assumes we are using
   * testcontainers jdbc driver)
   *
   * @throws Exception in case getting the container failed
   */
  @SuppressWarnings("unchecked")
  @SneakyThrows
  private JdbcDatabaseContainer getDataSourceContainer() {
    // here we had to use reflection to get access to the cached containers used by testcontainers
    // library
    // since we don't have a reference when containers are created using data source urls.
    val containersMapField =
        ContainerDatabaseDriver.class.getDeclaredField(JDBC_URL_CONTAINER_CACHE_FIELD_NAME);
    containersMapField.setAccessible(true);
    val containersCache = (Map<String, JdbcDatabaseContainer>) containersMapField.get(null);
    val container = containersCache.get(dataSourceUrl);
    if (container == null) {
      throw new RuntimeException(
          "no container found, check the data source url and that testcontainers is running");
    }
    return container;
  }

  @SneakyThrows
  private void stopDB(JdbcDatabaseContainer dataSourceContainer) {
    val pauseCmd =
        dataSourceContainer
            .getDockerClient()
            .pauseContainerCmd(dataSourceContainer.getContainerId());
    pauseCmd.exec();
    // without this the test can be flaky and hangs
    Thread.sleep(2000);
  }

  @SneakyThrows
  private void resumeDB(JdbcDatabaseContainer dataSourceContainer) {
    // resume db container
    val unpauseContainerCmd =
        dataSourceContainer
            .getDockerClient()
            .unpauseContainerCmd(dataSourceContainer.getContainerId());
    unpauseContainerCmd.exec();
    // give it some time
    Thread.sleep(2000);
  }
}
