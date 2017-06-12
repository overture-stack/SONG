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

import static java.lang.String.format;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingRead;
import org.icgc.dcc.song.server.model.analysis.VariantCall;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.composites.AnalysisSample;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

  @Autowired
  private final AnalysisRepository repository;
  @Autowired
  private final IdService idService;
  @Autowired
  private final AnalysisSampleService sampleService;
  @Autowired
  private final FileService fileService;
  @Autowired
  private final ExistenceService existence;

  @SneakyThrows
  public String create(String studyId, Analysis a) {
    val id = idService.generate(IdPrefix.Analysis);
    a.setAnalysisId(id);
    a.setStudy(studyId);

    repository.createAnalysis(a);

    saveSamples(studyId, id, a.getSample() );
    saveFiles(id, studyId, a.getFile());

    val experiment = a.getExperiment();

   if (experiment instanceof SequencingRead) {
     repository.createSequencingRead((SequencingRead) experiment) ;
   } else if (experiment instanceof VariantCall) {
     repository.createVariantCall((VariantCall) experiment);
   } else {
     return "Analysis failed: Unknown Analysis Type"; // shouldn't be possible if we validated our JSON first...
   }

  return id;
  }

  public void saveSamples(String studyId, String id, List<AnalysisSample> samples) {
    for(val sample: samples) {
      val sampleId = sampleService.save(studyId, sample);
      repository.addSample(id, sampleId);
    }
  }

  void saveFiles(String id, String studyId, List<File> files) {
    for (val f : files) {
      val fileId = fileService.save(studyId, f);
      addFile(id, fileId);
    }
  }

  void addFile(String id, String fileId) {
    repository.addFile(id, fileId);
  }


  public List<String> getAnalyses(Map<String, String> params) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getAnalysisById(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public String updateAnalysis(String studyId, String json) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<File> readFilesByAnalysisId(String id) {
    return repository.getFilesById(id);
  }

  public String publish(@NonNull String accessToken, @NonNull String id) {
    val files = readFilesByAnalysisId(id);
    List<String> missingUploads=new ArrayList<>();
    for (val f: files) {
       if ( !confirmUploaded(accessToken,f.getObjectId()) ) {
         missingUploads.add(f.getObjectId());
       }
    }
    if (missingUploads.isEmpty()) {
      repository.updateState(id,"PUBLISHED");
      return JsonUtils.fromSingleQuoted(format("'status':'success','msg': 'Analysis %s' successfully published.'", id));
    }
    return JsonUtils.fromSingleQuoted(format("'status': 'failure', 'msg': 'The following file ids must be published before analysis analysisId %s can be published: %s',analysisId, files"));
  }

  public String suppress(String id) {
    repository.updateState(id, "SUPPRESSED");
    return JsonUtils.fromSingleQuoted(format("'status':'ok', 'msg': 'Analysis %s was suppressed'",id));
  }

  public boolean confirmUploaded(String accessToken, String fileId) {
    return existence.isObjectExist(accessToken,fileId);
  }

}
