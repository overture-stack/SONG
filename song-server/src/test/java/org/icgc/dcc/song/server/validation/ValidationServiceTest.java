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
package org.icgc.dcc.song.server.validation;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.icgc.dcc.song.server.service.ValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.server.utils.TestFiles.getJsonNodeFromClasspath;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"dev","test"})
public class ValidationServiceTest {
  private static final String SEQ_READ="SequencingRead";
  private static final String VAR_CALL="VariantCall";

  @Autowired
  private ValidationService service;

  @Test
  public void testValidateValidSequencingRead() {
    val payload=getJsonFile("sequencingRead.json").toString();
    val results=service.validate(payload,SEQ_READ);
    assertThat(results).isNull();
  }

  @Test
  public void testValidateValidVariantCall() {
    val payload=getJsonFile("variantCall.json").toString();
    val results=service.validate(payload,VAR_CALL);
    assertThat(results).isNull();
  }

  @Test
  public void testValidateSequencingReadWithStudy() {
    val payload=getJsonFile("sequencingReadStudy.json").toString();
    val results=service.validate(payload,SEQ_READ);
    assertThat(results).isEqualTo("Uploaded JSON document must not contain a study field");
  }

  @Test
  public void testValidateVariantCallWithStudy() {
    val payload=getJsonFile("variantCallStudy.json").toString();
    val results=service.validate(payload,VAR_CALL);
    assertThat(results).isEqualTo("Uploaded JSON document must not contain a study field");
  }

  private JsonNode getJsonFile(String name) {
    return getJsonNodeFromClasspath("documents/validation/" + name);
  }
}
