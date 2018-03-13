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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.InfoTypes;
import org.icgc.dcc.song.server.repository.InfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_REPOSITORY_CREATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_REPOSITORY_DELETE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_REPOSITORY_UPDATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.ANALYSIS;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.DONOR;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.FILE;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SAMPLE;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SPECIMEN;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.STUDY;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.VARIANT_CALL;

@RequiredArgsConstructor
@Service
abstract class InfoService {

  private final InfoTypes type;
  private final InfoRepository infoRepository;

  public Optional<String> readInfo(@NonNull String id) {
    checkInfoExists(id); //TODO: optimize by returning Info entity, so that only 1 db read is done instead of 2.
    return Optional.ofNullable(infoRepository.readInfo(id, type.toString()));
  }

  public String readNullableInfo(String id) {
    return readInfo(id).orElse(null);
  }

  public void checkInfoExists(@NonNull String id){
    checkServer(isInfoExist(id), getClass(), INFO_NOT_FOUND,
        "The Info record for id='%s' and type='%s' was not found", id, type.toString());
  }

  /**
   * Using readType method since readInfo can return null
   */
  public boolean isInfoExist(@NonNull String id){
    return !isNull(infoRepository.readType(id, type.toString()));
  }

  public void create( @NonNull String id,  String info) {
    checkServer(!isInfoExist(id),getClass(), INFO_ALREADY_EXISTS,
    "Could not create Info record for id='%s' and type='%s' because it already exists",
    id, type.toString());
    val status = infoRepository.create(id, type.toString(), info);
    checkServer(status == 1, getClass(), INFO_REPOSITORY_CREATE_RECORD,
        "Could not create Info record for id='%s' and type='%s' in repository", id, type.toString());
  }

  public void update(@NonNull String id, String info) {
    checkInfoExists(id);
    val status = infoRepository.set(id, type.toString(), info);
    checkServer(status == 1, getClass(), INFO_REPOSITORY_UPDATE_RECORD,
        "Could not update Info record for id='%s' and type='%s' in repository", id, type.toString());
  }

  public void delete(@NonNull String id) {
    checkInfoExists(id);
    val status = infoRepository.delete(id, type.toString());
    checkServer(status ==1, getClass(), INFO_REPOSITORY_DELETE_RECORD,
          "Could not delete Info record for id='%s' and type='%s' in repository", id, type.toString());
  }

}

@Service
class StudyInfoService extends InfoService {
  @Autowired
  StudyInfoService(InfoRepository repo) {
    super(STUDY, repo);
  }
}

@Service
class DonorInfoService extends InfoService {
  @Autowired
  DonorInfoService(InfoRepository repo) {
    super(DONOR, repo);
  }
}

@Service
class SpecimenInfoService extends InfoService {
  @Autowired
  SpecimenInfoService(InfoRepository repo) {
    super(SPECIMEN, repo);
  }
}

@Service
class SampleInfoService extends InfoService {
  @Autowired
  SampleInfoService(InfoRepository repo) {
    super(SAMPLE, repo);
  }
}

@Service
class FileInfoService extends InfoService {
  @Autowired
  FileInfoService(InfoRepository repo) {
    super(FILE, repo);
  }
}

@Service
class AnalysisInfoService extends InfoService {
  @Autowired
  AnalysisInfoService(InfoRepository repo) {
    super(ANALYSIS, repo);
  }
}

@Service
class VariantCallInfoService extends InfoService {
  @Autowired
  VariantCallInfoService(InfoRepository repo) {
    super(VARIANT_CALL, repo);
  }
}

@Service
class SequencingReadInfoService extends InfoService {
  @Autowired
  SequencingReadInfoService(InfoRepository repo) {
    super(SEQUENCING_READ, repo);
  }
}

