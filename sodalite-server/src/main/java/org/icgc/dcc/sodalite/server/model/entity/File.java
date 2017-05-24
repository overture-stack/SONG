
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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"objectId",
    "fileName",
    "fileSize",
    "fileType",
    "fileMd5",
    "metadataDoc"
})

public class File implements Entity {

  @JsonProperty("objectId")
  private String objectId;

  @JsonProperty("fileName")
  private String fileName;

  @JsonProperty("fileSize")
  private long fileSize;

    @JsonProperty("fileType")
    private FileType fileType;

    @JsonProperty("fileMd5")
    private String fileMd5;

    @JsonProperty("metadataDoc")
    private String metadataDoc;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
  
  @JsonProperty("objectId")
  public String getObjectId() {
    return objectId;
  }

  @JsonProperty("objectId")
  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public File withObjectId(String objectId) {
    this.objectId = objectId;
    return this;
  }

    @JsonProperty("fileName")
    public String getFileName() {
        return fileName;
    }

    @JsonProperty("fileName")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

  @JsonProperty("fileSize")
  public long getFileSize() {
    return fileSize;
  }

  @JsonProperty("fileSize")
  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public File withFileSize(long fileSize) {
    this.fileSize = fileSize;
    return this;
  }

    @JsonProperty("fileType")
    public FileType getFileType() {
        return fileType;
    }

    @JsonProperty("fileType")
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public File withFileType(FileType fileType) {
        this.fileType = fileType;
        return this;
    }

    @JsonProperty("fileMd5")
    public String getFileMd5() {
        return fileMd5;
    }

    @JsonProperty("fileMd5")
    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public File withFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
        return this;
    }

    @JsonProperty("metadataDoc")
    public String getMetadataDoc() {
        return metadataDoc;
    }

    @JsonProperty("metadataDoc")
    public void setMetadataDoc(String metadataDoc) {
        this.metadataDoc = metadataDoc;
    }

    public File withMetadataDoc(String metadataDoc) {
        this.metadataDoc = metadataDoc;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public File withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

  public static enum FileType {

    FASTA("FASTA"), FAI("FAI"), FASTQ("FASTQ"), BAM("BAM"), BAI("BAI"), VCF("VCF"), TBI("TBI"), IDX("IDX"), XML("XML");

    private final String value;
    private final static Map<String, FileType> CONSTANTS = new HashMap<String, FileType>();

    static {
      for (FileType c : FileType.values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    private FileType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static FileType fromValue(String value) {
      FileType constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
