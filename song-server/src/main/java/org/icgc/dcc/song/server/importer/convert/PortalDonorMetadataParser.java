/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.
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
 */
package org.icgc.dcc.song.server.importer.convert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.song.server.importer.convert.FieldNames.ID;

@NoArgsConstructor(access = PRIVATE)
public final class PortalDonorMetadataParser {

  public static String getId(@NonNull ObjectNode donor){
    return donor.path(ID).textValue();
  }

  public static String getProjectId(@NonNull ObjectNode donor){
    return donor.path(FieldNames.PROJECT_ID).textValue();
  }

  public static String getProjectName(@NonNull ObjectNode donor){
    return donor.path(FieldNames.PROJECT_NAME).textValue();
  }

  public static String getGender(@NonNull ObjectNode donor){
    return donor.path(FieldNames.GENDER).textValue();
  }

  public static String getSubmitterDonorId(@NonNull ObjectNode donor){
    return donor.path(FieldNames.SUBMITTED_DONOR_ID).textValue();
  }

  public static int getNumSpecimens(@NonNull ObjectNode donor){
    return donor.path(FieldNames.SPECIMEN).size();
  }

  public static int getNumSamples(@NonNull ObjectNode donor, int specimenIdx){
    return getSpecimen(donor, specimenIdx).path(FieldNames.SAMPLES).size();
  }

  public static String getSpecimenId(@NonNull ObjectNode donor, int specimenIdx){
    return getSpecimen(donor, specimenIdx).path(ID).textValue();
  }

  public static String getSpecimenSubmittedId(@NonNull ObjectNode donor, int specimenIdx){
    return getSpecimen(donor, specimenIdx).path(FieldNames.SUBMITTED_ID).textValue();
  }

  public static String getSpecimenType(@NonNull ObjectNode donor, int specimenIdx){
    return getSpecimen(donor, specimenIdx).path(FieldNames.TYPE).textValue();
  }

  public static String getSampleId(@NonNull ObjectNode donor, int specimenIdx, int sampleIdx){
    return getSample(donor, specimenIdx, sampleIdx).path(ID).textValue();
  }

  public static String getSampleAnalyzedId(@NonNull ObjectNode donor, int specimenIdx, int sampleIdx){
    return getSample(donor, specimenIdx, sampleIdx).path(FieldNames.ANALYZED_ID).textValue();
  }

  public static String getSampleStudy(@NonNull ObjectNode donor, int specimenIdx, int sampleIdx){
    return getSample(donor, specimenIdx, sampleIdx).path(FieldNames.STUDY).textValue();
  }

  public static String getSampleLibraryStrategy(@NonNull ObjectNode donor, int specimenIdx, int sampleIdx){
    return getFirstSampleAvailableRawSequenceData(donor, specimenIdx, sampleIdx).path(FieldNames.LIBRARY_STRATEGY).textValue();
  }
  private static JsonNode getFirstSampleAvailableRawSequenceData(@NonNull ObjectNode donor, int specimenIdx, int
      sampleIdx){
    return getSample(donor, specimenIdx, sampleIdx).path(FieldNames.AVAILABLE_RAW_SEQUENCE_DATA);
  }


  private static JsonNode getSpecimen(@NonNull ObjectNode donor, int specimenIdx){
    return donor.path(FieldNames.SPECIMEN).get(specimenIdx);
  }

  private static JsonNode getSample(@NonNull ObjectNode donor, int specimenIdx, int sampleIdx){
    return getSpecimen(donor, specimenIdx).path(FieldNames.SAMPLES).get(sampleIdx);
  }



}
