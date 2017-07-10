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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNPUBLISHED_FILE_IDS;
import static org.icgc.dcc.song.core.exceptions.SongError.error;
import static org.icgc.dcc.song.core.utils.Responses.ok;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

  @Autowired
  private final AnalysisRepository repository;
  @Autowired
  private final IdService idService;
  @Autowired
  private final CompositeEntityService compositeEntityService;

  @Autowired
  private final FileService fileService;
  @Autowired
  private final ExistenceService existence;

  public ResponseEntity<String> updateAnalysis(String studyId, Analysis analysis) {
    repository.deleteCompositeEntities(analysis.getAnalysisId());
    saveCompositeEntities(studyId, analysis.getAnalysisId(), analysis.getSample());
    repository.deleteFiles(analysis.getAnalysisId());
    saveFiles(analysis.getAnalysisId(), studyId, analysis.getFile());

    if (analysis instanceof SequencingReadAnalysis ) {
      repository.updateSequencingRead(((SequencingReadAnalysis) analysis).getExperiment() );
    } else if (analysis instanceof VariantCallAnalysis) {
      repository.updateVariantCall(((VariantCallAnalysis) analysis).getExperiment());
    }
    return ok("AnalysisId %s was updated successfully", analysis.getAnalysisId());
  }

  public String create(String studyId, Analysis a) {
    val id = idService.generateAnalysisId();
    a.setAnalysisId(id);
    a.setStudy(studyId);
    repository.createAnalysis(a);

    saveCompositeEntities(studyId, id, a.getSample() );
    saveFiles(id, studyId, a.getFile());

   if (a instanceof SequencingReadAnalysis) {
     val experiment = ((SequencingReadAnalysis) a).getExperiment();
     experiment.setAnalysisId(id);
     repository.createSequencingRead(experiment) ;
   } else if (a instanceof VariantCallAnalysis) {
     val experiment = ((VariantCallAnalysis) a).getExperiment();
     experiment.setAnalysisId(id);
     repository.createVariantCall(experiment);
   } else {
     // shouldn't be possible if we validated our JSON first...
     throw new IllegalArgumentException("Invalid analysis type");
   }
   return id;
  }

  void saveCompositeEntities(String studyId, String id, List<CompositeEntity> samples) {
    samples.stream()
            .map(sample->compositeEntityService.save(studyId,sample))
            .forEach(sampleId->repository.addSample(id, sampleId));
  }

  void saveFiles(String id, String studyId, List<File> files) {
    files.stream()
            .map(f->fileService.save(id, studyId, f))
            .forEach(fileId->addFile(id, fileId));
  }

  void addFile(String id, String fileId) {
    repository.addFile(id, fileId);
  }


  /**
   * Gets all analysis for a given study.
   * This method should be watched in case performance becomes a problem.
   * @param studyId the study ID
   * @return returns a List of analysis with the child entities.
   */
  public List<Analysis> getAnalysis(@NonNull String studyId) {
    val analysisList = repository.find(studyId);
    analysisList.forEach(a -> {
          if (a != null) {
            String id = a.getAnalysisId();
            a.setFile(readFiles(id));
            a.setSample(readSamples(id));
            if (a instanceof SequencingReadAnalysis) {
              ((SequencingReadAnalysis) a).setExperiment(repository.readSequencingRead(id));
            } else if (a instanceof VariantCallAnalysis) {
              ((VariantCallAnalysis) a).setExperiment(repository.readVariantCall(id));
            }
          }
        });
    return analysisList;
  }

  public Analysis read(String id) {
    val analysis = repository.read(id);
    if (analysis == null) {
      return null;
    }

    analysis.setFile(readFiles(id));
    analysis.setSample(readSamples(id));

    if (analysis instanceof SequencingReadAnalysis) {
      val experiment = repository.readSequencingRead(id);
      ((SequencingReadAnalysis) analysis).setExperiment(experiment);
    } else if (analysis instanceof VariantCallAnalysis) {
      val experiment = repository.readVariantCall(id);
      ((VariantCallAnalysis) analysis).setExperiment(experiment);
    }

    return analysis;
  }

  public List<File> readFiles(String id) {
    return repository.readFiles(id);
  }

  List<CompositeEntity> readSamples(String id) {
    val samples = new ArrayList<CompositeEntity>();
    for(val sampleId: repository.findSampleIds(id)) {
      samples.add(compositeEntityService.read(sampleId));
    }
    return samples;
  }

  public ResponseEntity<String> publish(@NonNull String accessToken, @NonNull String id) {
    val files = readFiles(id);
    List<String> missingUploads=new ArrayList<>();
    for (val f: files) {
       if ( !confirmUploaded(accessToken,f.getObjectId()) ) {
         missingUploads.add(f.getObjectId());
       }
    }
    if (missingUploads.isEmpty()) {
      repository.updateState(id,"PUBLISHED");
      return ok("AnalysisId %s successfully published", id);
    }
    return error(UNPUBLISHED_FILE_IDS,
        "The following file ids must be published before analysisId %s can be published: %s",
        id, files);
  }

  public ResponseEntity<String> suppress(String id) {
    repository.updateState(id, "SUPPRESSED");
    return ok("AnalysisId %s was suppressed",id);
  }

  boolean confirmUploaded(String accessToken, String fileId) {
    return existence.isObjectExist(accessToken,fileId);
  }
}
