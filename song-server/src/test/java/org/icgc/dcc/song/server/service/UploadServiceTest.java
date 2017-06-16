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

import lombok.SneakyThrows;
import lombok.val;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;

import org.icgc.dcc.song.server.model.Upload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.nio.file.*;

import static org.springframework.http.HttpStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles({"dev", "secure"})
public class UploadServiceTest {

  @Autowired
  UploadService uploadService;

  @Test
  @SneakyThrows
  public void testSequencingRead() {
    test("sequencingRead.json");
  }

  @Test
  @SneakyThrows
  public void testVariantCall() {
    test("variantCall.json");
  }

  @SneakyThrows
  private String readFile(String name) {
    return new String(Files.readAllBytes(new java.io.File("..", name).toPath()));
  }

  public String read(String uploadId) {
    Upload status = uploadService.read(uploadId);
    return status.getState();
  }

  public String validate(String uploadId) throws InterruptedException {
    String state=read(uploadId);
    // wait for the server to finish
    while(state.equals("CREATED")) {
      Thread.sleep(50);
      state=read(uploadId);
    }
    return state;
  }

  @SneakyThrows
  public void test(String fileName) {
    val study="ABC123";
    val json = readFile(fileName);

    // test upload
    val uploadStatus=uploadService.upload(study, json);
    assertThat(uploadStatus.getStatusCode()).isEqualTo(OK);
    val uploadId=uploadStatus.getBody().toString();
    assertThat(uploadId.startsWith("UP")).isTrue();

    // test create
    val initialState = read(uploadId);
    assertThat(initialState).isEqualTo("CREATED");

    // test validation
    val finalState = validate(uploadId);
    assertThat(finalState).isEqualTo("VALIDATED");

    // test save
   val response = uploadService.save(study,uploadId);
   assertThat(response.getStatusCode()).isEqualTo(OK);
   val analysisId = response.getBody().toString();
   assertThat(analysisId.startsWith("AN")).isTrue();
  }

}
