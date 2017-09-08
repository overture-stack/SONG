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
import org.icgc.dcc.song.server.model.analysis.AnalysisSearchRequest;
import org.icgc.dcc.song.server.repository.search.InfoSearchResponse;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.icgc.dcc.song.server.model.enums.AnalysisStates;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.search.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_STATE_UPDATE_FAILED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNPUBLISHED_FILE_IDS;
import static org.icgc.dcc.song.core.exceptions.ServerException.buildServerException;
import static org.icgc.dcc.song.core.exceptions.SongError.error;
import static org.icgc.dcc.song.core.utils.Responses.ok;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.PUBLISHED;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.SUPPRESSED;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.createMultiSearchTerms;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

  @Autowired
  private final AnalysisRepository repository;
  @Autowired
  private final AnalysisInfoService infoService;
  @Autowired
  private final SequencingReadInfoService sequencingReadInfoService;
  @Autowired
  private final VariantCallInfoService variantCallInfoService;

  @Autowired
  private final IdService idService;
  @Autowired
  private final CompositeEntityService compositeEntityService;

  @Autowired
  private final FileService fileService;
  @Autowired
  private final ExistenceService existence;

  @Autowired
  private final SearchRepository searchRepository;


  public String create(String studyId, Analysis a) {
    val id = idService.generateAnalysisId();
    a.setAnalysisId(id);
    a.setStudy(studyId);
    repository.createAnalysis(a);
    infoService.create(id, a.getInfo());

    saveCompositeEntities(studyId, id, a.getSample() );
    saveFiles(id, studyId, a.getFile());

   if (a instanceof SequencingReadAnalysis) {
     val experiment = ((SequencingReadAnalysis) a).getExperiment();
     createSequencingRead(id, experiment);
   } else if (a instanceof VariantCallAnalysis) {
     val experiment = ((VariantCallAnalysis) a).getExperiment();
     createVariantCall(id, experiment);
   } else {
     // shouldn't be possible if we validated our JSON first...
     throw new IllegalArgumentException("Invalid analysis type");
   }
   return id;
  }

  public void createSequencingRead(String id, SequencingRead experiment) {
    experiment.setAnalysisId(id);
    repository.createSequencingRead(experiment);
    sequencingReadInfoService.create(id, experiment.getInfo());
  }

  public void createVariantCall(String id, VariantCall experiment) {
    experiment.setAnalysisId(id);
    repository.createVariantCall(experiment);
    variantCallInfoService.create(id, experiment.getInfo());
  }

  void saveCompositeEntities(String studyId, String id, List<CompositeEntity> samples) {
    samples.stream()
            .map(sample->compositeEntityService.save(studyId,sample))
            .forEach(sampleId->repository.addSample(id, sampleId));
  }

  void saveFiles(String id, String studyId, List<File> files) {
    files.forEach(f->fileService.save(id, studyId, f));
  }

  public ResponseEntity<String> updateAnalysis(String studyId, Analysis analysis) {
    val id = analysis.getAnalysisId();
    repository.deleteCompositeEntities(id);
    saveCompositeEntities(studyId, id, analysis.getSample());
    repository.deleteFiles(id);
    saveFiles(id, studyId, analysis.getFile());
    infoService.update(id, analysis.getInfo());

    if (analysis instanceof SequencingReadAnalysis ) {
      val experiment = ((SequencingReadAnalysis) analysis).getExperiment();
      updateSequencingRead(id, experiment);
    } else if (analysis instanceof VariantCallAnalysis) {
      val experiment = ((VariantCallAnalysis) analysis).getExperiment();
      updateVariantCall(id, experiment);
    }
    return ok("AnalysisId %s was updated successfully", analysis.getAnalysisId());
  }


  public void updateSequencingRead(String id, SequencingRead experiment) {
    repository.updateSequencingRead( experiment);
    sequencingReadInfoService.update(id, experiment.getInfo());
  }

  public void updateVariantCall(String id, VariantCall experiment) {
    repository.updateVariantCall( experiment);
    variantCallInfoService.update(id, experiment.getInfo());
  }

  /**
   * Gets all analysis for a given study.
   * This method should be watched in case performance becomes a problem.
   * @param studyId the study ID
   * @return returns a List of analysis with the child entities.
   */
  public List<Analysis> getAnalysis(@NonNull String studyId) {
    val analysisList = repository.find(studyId);
    return processAnalysisList(analysisList);
  }

  /**
   * Searches all analysis matching the AnalysisSearchRequest
   * @param request which defines the query
   * @return returns a list of analysis with child entities in response to the search request
   */
  public List<Analysis> searchAnalysis(@NonNull String studyId, @NonNull AnalysisSearchRequest request){
    val analysisList = repository.search(studyId,
        request.getDonorId(),
        request.getSpecimenId(),
        request.getSampleId(),
        request.getFileId() );
    return processAnalysisList(analysisList);
  }

  public List<InfoSearchResponse> infoSearch(@NonNull String studyId,
      boolean includeInfo, @NonNull MultiValueMap<String, String> multiKeyValueMap){
    val searchTerms = multiKeyValueMap.entrySet()
        .stream()
        .map(x -> createMultiSearchTerms(x.getKey(), x.getValue()))
        .flatMap(Collection::stream)
        .collect(toImmutableList());
    return searchRepository.infoSearch(includeInfo, searchTerms);
  }

  public Analysis read(String id) {
    val analysis = repository.read(id);
    if (analysis == null) {
      return null;
    }
    analysis.setInfo(infoService.read(id));

    analysis.setFile(readFiles(id));
    analysis.setSample(readSamples(id));

    if (analysis instanceof SequencingReadAnalysis) {
      val experiment = readSequencingRead(id);
      ((SequencingReadAnalysis) analysis).setExperiment(experiment);
    } else if (analysis instanceof VariantCallAnalysis) {
      val experiment =readVariantCall(id);
      ((VariantCallAnalysis) analysis).setExperiment(experiment);
    }

    return analysis;
  }

  SequencingRead readSequencingRead(String id) {
    val experiment = repository.readSequencingRead(id);
    if (experiment == null) {
      return null;
    }
    experiment.setInfo(sequencingReadInfoService.read(id));

    return experiment;
  }

  VariantCall readVariantCall(String id) {
    val experiment = repository.readVariantCall(id);
    if (experiment == null) {
      return null;
    }
    experiment.setInfo(variantCallInfoService.read(id));

    return experiment;
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
      checkedUpdateState(id, PUBLISHED);
      return ok("AnalysisId %s successfully published", id);
    }
    return error(UNPUBLISHED_FILE_IDS,
        "The following file ids must be published before analysisId %s can be published: %s",
        id, files);
  }

  public ResponseEntity<String> suppress(String id) {
    checkedUpdateState(id, SUPPRESSED);
    return ok("AnalysisId %s was suppressed",id);
  }

  private void checkedUpdateState(String id, AnalysisStates analysisState) {
    val state = analysisState.name();
    val status = repository.updateState(id, state);
    if (status != 1) {
      throw buildServerException(this.getClass(),
          ANALYSIS_STATE_UPDATE_FAILED,
          "Cannot update analysisId '%s' with state '%s'. Ensure analysisId exists, and the state is allowed",
          id, state);
    }
  }

  boolean confirmUploaded(String accessToken, String fileId) {
    return existence.isObjectExist(accessToken,fileId);
  }

  /**
   * Adds all child entities for each analysis
   * This method should be watched in case performance becomes a problem.
   * @param analysisList list of Analysis to be updated
   * @return returns a List of analysis with the child entities
   */
  private List<Analysis> processAnalysisList(List<Analysis> analysisList){
    analysisList.stream()
        .filter(Objects::nonNull)
        .forEach(this::processAnalysis);
    return analysisList;
  }

  /**
   * Adds child entities to analysis
   * This method should be watched in case performance becomes a problem.
   * @param analysis is the Analysis to be updated
   * @return updated analysis with the child entity
   */
  private Analysis processAnalysis(Analysis analysis) {
    String id = analysis.getAnalysisId();
    analysis.setFile(readFiles(id));
    analysis.setSample(readSamples(id));
    analysis.setInfo(infoService.read(id));

    if (analysis instanceof SequencingReadAnalysis) {
      ((SequencingReadAnalysis) analysis).setExperiment(readSequencingRead(id));
    } else if (analysis instanceof VariantCallAnalysis) {
      ((VariantCallAnalysis) analysis).setExperiment(readVariantCall(id));
    }
    return analysis;
  }


}
