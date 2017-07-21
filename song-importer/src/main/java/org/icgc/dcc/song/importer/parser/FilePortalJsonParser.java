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
package org.icgc.dcc.song.importer.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.icgc.dcc.song.importer.parser.FieldNames.*;

@NoArgsConstructor(access = PRIVATE)
public final class FilePortalJsonParser {

  public static String getAccess(@NonNull ObjectNode file){
    return file.path(ACCESS).textValue();
  }

  public static String getObjectId(@NonNull ObjectNode file) {
    return file.path(OBJECT_ID).textValue();
  }

  public static String getDataType(@NonNull ObjectNode file) {
    return getDataCategorization(file).path(DATA_TYPE).textValue();
  }

  public static String getExperimentalStrategy(@NonNull ObjectNode file) {
    return getDataCategorization(file).path(EXPERIMENTAL_STRATEGY).textValue();
  }

  public static String getFileId(@NonNull ObjectNode file) {
    return file.path(ID).textValue();
  }

  public static String getFileName(@NonNull ObjectNode file) {
    return getFirstFileCopy(file).path(FILE_NAME).textValue();
  }

  public static String getFileFormat(@NonNull ObjectNode file) {
    return getFirstFileCopy(file).path(FILE_FORMAT).textValue();
  }

  public static long getFileSize(@NonNull ObjectNode file) {
    return getFirstFileCopy(file).path(FILE_SIZE).asLong(-1);
  }

  public static long getFileLastModified(@NonNull ObjectNode file) {
    return getFirstFileCopy(file).path(LAST_MODIFIED).asLong(-1);
  }

  public static String getFileMd5sum(@NonNull ObjectNode file) {
    return getFirstFileCopy(file).path(FILE_MD5SUM).textValue();
  }

  public static String getProjectCode(@NonNull ObjectNode file) {
    return getFirstDonor(file).path(PROJECT_CODE).textValue();
  }

  public static String getDonorId(@NonNull ObjectNode file) {
    return getFirstDonor(file).path(DONOR_ID).textValue();
  }

  public static String getSubmittedDonorId(@NonNull ObjectNode file) {
    return getFirstDonor(file).path(SUBMITTED_DONOR_ID).textValue();
  }

  public static String getReferenceName(@NonNull ObjectNode file) {
    return getReferenceGenome(file).path(REFERENCE_NAME).textValue();
  }

  public static String getGenomeBuild(@NonNull ObjectNode file) {
    return getReferenceGenome(file).path(GENOME_BUILD).textValue();
  }

  private static JsonNode getReferenceGenome(@NonNull ObjectNode file) {
    return file.path(REFERENCE_GENOME);
  }

  public static String getRepoDataBundleId(@NonNull ObjectNode file){
    return getFirstFileCopy(file).path(REPO_DATA_BUNDLE_ID).textValue();
  }

  public static Optional<String> getIndexFileId(@NonNull ObjectNode file){
    return getIndexFile(file, x -> x.path(ID).textValue());
  }


  public static Optional<String> getIndexFileObjectId(@NonNull ObjectNode file){
    return getIndexFile(file, x -> x.path(OBJECT_ID).textValue());
  }

  public static Optional<String> getIndexFileFileName(@NonNull ObjectNode file){
    return getIndexFile(file, x -> x.path(FILE_NAME).textValue());
  }

  public static Optional<String> getIndexFileFileFormat(@NonNull ObjectNode file){
    return getIndexFile(file, x -> x.path(FILE_FORMAT).textValue());
  }

  public static Optional<String> getIndexFileFileMd5sum(@NonNull ObjectNode file){
    return getIndexFile(file, x -> x.path(FILE_MD5SUM).textValue());
  }

  public static Optional<Long> getIndexFileFileSize(@NonNull ObjectNode file){
    return getIndexFile(file, x -> x.path(FILE_MD5SUM).asLong(-1));
  }

  public static String getSoftware(@NonNull ObjectNode file){
    return getAnalysisMethod(file).path(SOFTWARE).textValue();
  }

  public static List<String> getSampleIds(@NonNull ObjectNode file) {
    val sampleIdNode = getFirstDonor(file).path(SAMPLE_ID);
    return getFirstLevelList(sampleIdNode);
  }

  public static List<String> getSubmittedSampleIds(@NonNull ObjectNode file) {
    val submittedSampleIdNode = getFirstDonor(file).path(SUBMITTED_SAMPLE_ID);
    return getFirstLevelList(submittedSampleIdNode);
  }

  public static List<String> getSpecimenIds(@NonNull ObjectNode file) {
    val specimenIdNode = getFirstDonor(file).path(SPECIMEN_ID);
    return getFirstLevelList(specimenIdNode);
  }

  public static List<String> getSpecimenTypes(@NonNull ObjectNode file) {
    val specimenIdNode = getFirstDonor(file).path(SPECIMEN_TYPE);
    return getFirstLevelList(specimenIdNode);
  }

  public static List<String> getSubmittedSpecimenIds(@NonNull ObjectNode file) {
    val submittedSpecimenIdNode = getFirstDonor(file).path(SUBMITTED_SPECIMEN_ID);
    return getFirstLevelList(submittedSpecimenIdNode);
  }

  private static List<String> getFirstLevelList(@NonNull JsonNode node){
    return stream(node)
        .map(JsonNode::textValue)
        .collect(toImmutableList());
  }

  private static JsonNode getFirstDonor(@NonNull ObjectNode file) {
    return getDonors(file).path(0);
  }

  private static JsonNode getFirstFileCopy(@NonNull ObjectNode file) {
    return getFileCopies(file).path(0);
  }

  private static JsonNode getDataCategorization(@NonNull ObjectNode file) {
    return file.path(DATA_CATEGORIZATION);
  }

  private static JsonNode getFileCopies(@NonNull ObjectNode file) {
    return file.path(FILE_COPIES);
  }

  public static JsonNode getDonors(@NonNull ObjectNode file) {
    return file.path(DONORS);
  }

  private static <T> Optional<T> getIndexFile(@NonNull ObjectNode file, Function<JsonNode, T> extractFunction){
    val opt = Optional.ofNullable(getFirstFileCopy(file).path(INDEX_FILE));
    return opt.map(extractFunction);
  }

  private static JsonNode getAnalysisMethod(@NonNull ObjectNode file){
    return file.path(ANALYSIS_METHOD);
  }
}
