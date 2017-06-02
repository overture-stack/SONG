
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

package org.icgc.dcc.sodalite.server.model.entity;

import java.util.Map;
import java.util.TreeMap;

import org.icgc.dcc.sodalite.server.model.enums.FileType;
import org.icgc.dcc.sodalite.server.utils.JsonUtils;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.val;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Data
public class File {

  private String objectId = "";
  private String fileName = "";
  private String sampleId = "";
  private Long fileSize = -1L;
  private FileType fileType = FileType.IDX;
  private String fileMd5 = "";

  private final Map<String, Object> metadata = new TreeMap<>();

  public static File create(String id, String name, String sample, Long size, String type, String md5,
      String metadata) {
    val f = new File();
    f.setObjectId(id);
    f.setFileName(name);
    f.setSampleId(sample);
    f.setFileSize(size);
    f.setFileType(type);
    f.setFileMd5(md5);

    f.addMetadata(metadata);
    return f;
  }

  public void setFileType(String type) {
    fileType = FileType.fromValue(type);
  }

  public String getFileType() {
    return fileType.value();
  }

  @JsonAnySetter
  public void setMetadata(String key, Object value) {
    metadata.put(key, value);
  }

  public void addMetadata(String json) {
    if (json == null) {
      return;
    }
    metadata.putAll(JsonUtils.toMap(json, "metadata"));
  }

  public String getMetadata() {
    return JsonUtils.toJson(metadata);
  }

  // @JsonIgnore
  // public String getId() {
  // return getFileId();
  // }
  //
  // @JsonIgnore
  // public void setId(String id) {
  // setFileId(id);
  // }
  //
  // @JsonIgnore
  // public String getBusinessKey() {
  // return getFileName();
  // }
  //
  // @JsonIgnore
  // public void setKey(String key) {
  // setFileName(key);
  // }
  //
  // @JsonIgnore
  // void setParentId(String id) {
  // setSampleId(id);
  // }
  //
  // @JsonIgnore
  // String getParentId(String id) {
  // return getSampleId();
  // }

}
