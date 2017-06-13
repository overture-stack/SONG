/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.SneakyThrows;
import lombok.val;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;

import org.icgc.dcc.song.server.model.Upload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.File;

import java.nio.file.*;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class UploadServiceTest {

  @Autowired
  UploadService uploadService;

  @Test
  @SneakyThrows
  public void testUploadSequencingRead() {
    val json = new String(Files.readAllBytes(new File("..", "meta2.json").toPath()));
    testUpload(json);
  }

  @Test
  @SneakyThrows
  public void testUploadVariantCall() {
    val json = new String(Files.readAllBytes(new File("..", "variant2.json").toPath()));
    testUpload(json);
  }

  @SneakyThrows
  public void testUpload(String json) {
    val uploadStatus=uploadService.upload("ABC123", json);
    assertThat(uploadStatus != null).isTrue();
    val uploadId=uploadStatus.getBody();
    assertThat(uploadId != null).isTrue();
    System.out.printf("Got uploadId='%s",uploadId);

    Upload status = uploadService.read(uploadId.toString());
    assertThat(status != null).isTrue();
    assertThat(status.getState()).isEqualTo("CREATED");

    Thread.sleep(3500);
    status = uploadService.read(uploadId.toString());
    assertThat(status.getState()).isEqualTo("VALIDATED");
  }



  @SneakyThrows
  @Test
  public void testSaveSequencingRead() {
    val json = new String(Files.readAllBytes(new File("..","meta2.json").toPath()));
    testSave(json);
  }

  @SneakyThrows
  @Test
  public void testSaveVariantCall() {
    val json = new String(Files.readAllBytes(new File("..", "variant2.json").toPath()));
    testSave(json);
  }

  @SneakyThrows
  public void testSave(String json) {
    val study="ABC123";

    val uploadStatus=uploadService.upload(study, json);
    assertThat(uploadStatus != null).isTrue();
    val uploadId=uploadStatus.getBody();
    assertThat(uploadId != null).isTrue();
    System.out.printf("Got uploadId='%s",uploadId);

    Upload status = uploadService.read(uploadId.toString());
    assertThat(status != null).isTrue();
    assertThat(status.getState()).isEqualTo("CREATED");
    Thread.sleep(3500);

    status = uploadService.read(uploadId.toString());
    assertThat(status.getState()).isEqualTo("VALIDATED");

    val analysisResponse = uploadService.save(study, uploadId.toString());
    assertThat(analysisResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    val analysisId = analysisResponse.getBody();
    assertThat(analysisId.toString().startsWith("AN")).isTrue();
  }



}
