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

import org.icgc.dcc.song.server.model.enums.InfoTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.icgc.dcc.song.server.repository.InfoRepository;
@RequiredArgsConstructor
@Service
abstract class InfoService {

  private final InfoTypes type;
  private final InfoRepository infoRepository;

  public String read(@NonNull String id) {
    return infoRepository.read(id, type.name());
  }

  public void create( @NonNull String id,  String info) {
    infoRepository.create(id, type.name(), info);
  }

  public void update(@NonNull String id, String info) {
    infoRepository.set(id, type.name(), info);
  }

  public void delete(@NonNull String id) {
    infoRepository.delete(id);
  }

}

@Service
class StudyInfoService extends InfoService {
  @Autowired
  StudyInfoService(InfoRepository repo) {
    super(InfoTypes.Study, repo);
  }
}

@Service
class DonorInfoService extends InfoService {
  @Autowired
  DonorInfoService(InfoRepository repo) {
    super(InfoTypes.Donor, repo);
  }
}

@Service
class SpecimenInfoService extends InfoService {
  @Autowired
  SpecimenInfoService(InfoRepository repo) {
    super(InfoTypes.Specimen, repo);
  }
}

@Service
class SampleInfoService extends InfoService {
  @Autowired
  SampleInfoService(InfoRepository repo) {
    super(InfoTypes.Sample, repo);
  }
}

@Service
class FileInfoService extends InfoService {
  @Autowired
  FileInfoService(InfoRepository repo) {
    super(InfoTypes.File, repo);
  }
}

@Service
class AnalysisInfoService extends InfoService {
  @Autowired
  AnalysisInfoService(InfoRepository repo) {
    super(InfoTypes.Analysis, repo);
  }
}

@Service
class VariantCallInfoService extends InfoService {
  @Autowired
  VariantCallInfoService(InfoRepository repo) {
    super(InfoTypes.VariantCall, repo);
  }
}

@Service
class SequencingReadInfoService extends InfoService {
  @Autowired
  SequencingReadInfoService(InfoRepository repo) {
    super(InfoTypes.SequencingRead, repo);
  }
}

