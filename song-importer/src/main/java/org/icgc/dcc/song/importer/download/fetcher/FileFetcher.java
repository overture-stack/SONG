package org.icgc.dcc.song.importer.download.fetcher;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Lombok;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icgc.dcc.song.importer.download.PortalDownloadIterator;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.song.core.utils.JsonUtils.toPrettyJson;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getAccess;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getDataType;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getDonorId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getExperimentalStrategy;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileFormat;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileLastModified;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileMd5sum;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileName;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileSize;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getGenomeBuild;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileFileFormat;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileFileMd5sum;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileFileName;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileFileSize;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileObjectId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getObjectId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getProjectCode;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getRepoDataBundleId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSampleIds;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSoftware;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSpecimenIds;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSpecimenTypes;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSubmittedDonorId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSubmittedSampleIds;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSubmittedSpecimenIds;

@Slf4j
@RequiredArgsConstructor
public class FileFetcher implements Callable<ArrayList<PortalFileMetadata>> {

  private final PortalDownloadIterator portalDownloadIterator;

  @Override public ArrayList<PortalFileMetadata> call() throws Exception {
    return fetchPortalFileMetadatas();
  }

  public ArrayList<PortalFileMetadata> fetchPortalFileMetadatas(){
    return (ArrayList<PortalFileMetadata>)portalDownloadIterator.stream()
        .map(FileFetcher::convertToPortalFileMetadata)
        .collect(toList());
  }

  public static PortalFileMetadata convertToPortalFileMetadata(ObjectNode o){
    try {
      return PortalFileMetadata.builder()
          .access              (getAccess(o))
          .repoDataBundleId    (getRepoDataBundleId(o))
          .dataType            (getDataType(o))
          .donorId             (getDonorId(o))
          .experimentalStrategy(getExperimentalStrategy(o))
          .fileFormat          (getFileFormat(o))
          .fileId              (getFileId(o))
          .fileLastModified    (getFileLastModified(o))
          .fileMd5sum          (getFileMd5sum(o))
          .fileName            (getFileName(o))
          .fileSize            (getFileSize(o))
          .genomeBuild         (getGenomeBuild(o))
          .indexFileFileFormat (getIndexFileFileFormat(o).orElse(null))
          .indexFileFileMd5sum (getIndexFileFileMd5sum(o).orElse(null))
          .indexFileFileName   (getIndexFileFileName(o).orElse(null))
          .indexFileFileSize   (getIndexFileFileSize(o).orElse(null))
          .indexFileId         (getIndexFileId(o).orElse(null))
          .indexFileObjectId   (getIndexFileObjectId(o).orElse(null))
          .objectId            (getObjectId(o))
          .projectCode         (getProjectCode(o))
          .sampleIds           (getSampleIds(o))
          .specimenIds         (getSpecimenIds(o))
          .specimenTypes        (getSpecimenTypes(o))
          .submittedDonorId    (getSubmittedDonorId(o))
          .submittedSampleIds  (getSubmittedSampleIds(o))
          .submittedSpecimenIds(getSubmittedSpecimenIds(o))
          .software            (getSoftware(o))
          .build();
    } catch (Throwable t){
      log.error("OBJECT_DATA:\n{}", toPrettyJson(o));
      throw Lombok.sneakyThrow(t);
    }
  }

  public static FileFetcher createFileFetcher(PortalDownloadIterator portalDownloadIterator) {
    return new FileFetcher(portalDownloadIterator);
  }

}
