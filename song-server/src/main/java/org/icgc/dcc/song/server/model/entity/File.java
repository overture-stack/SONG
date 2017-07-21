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

package org.icgc.dcc.song.server.model.entity;

import org.icgc.dcc.song.server.model.Metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.Constants;

@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Data
public class File extends Metadata {

  private String objectId = "";
  private String analysisId = "";
  private String fileName = "";
  private String studyId = "";
  private Long fileSize = -1L;
  private String fileType = "";
  private String fileMd5sum = "";

  public static File create(String id, String analysisId, String name, String study, Long size, String type, String md5,
      String metadata) {
    val f = new File();
    f.setObjectId(id);
    f.setAnalysisId(analysisId);
    f.setFileName(name);
    f.setStudyId(study);
    f.setFileSize(size);
    f.setFileType(type);
    f.setFileMd5sum(md5);

    f.addInfo(metadata);
    return f;
  }

  public void setFileType(String type) {
    Constants.validate(Constants.FILE_TYPE, type);
    fileType = type;
  }

}
