package org.icgc.dcc.song.importer.download.fetcher;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Lombok;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.importer.download.PortalDownloadIterator;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.parser.FilePortalJsonParser;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static java.util.stream.Collectors.toList;

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
          .access              (FilePortalJsonParser.getAccess(o))
          .repoDataBundleId    (FilePortalJsonParser.getRepoDataBundleId(o))
          .dataType            (FilePortalJsonParser.getDataType(o))
          .donorId             (FilePortalJsonParser.getDonorId(o))
          .experimentalStrategy(FilePortalJsonParser.getExperimentalStrategy(o))
          .fileFormat          (FilePortalJsonParser.getFileFormat(o))
          .fileId              (FilePortalJsonParser.getFileId(o))
          .fileLastModified    (FilePortalJsonParser.getFileLastModified(o))
          .fileMd5sum          (FilePortalJsonParser.getFileMd5sum(o))
          .fileName            (FilePortalJsonParser.getFileName(o))
          .fileSize            (FilePortalJsonParser.getFileSize(o))
          .genomeBuild         (FilePortalJsonParser.getGenomeBuild(o))
          .indexFileFileFormat (FilePortalJsonParser.getIndexFileFileFormat(o).orElse(null))
          .indexFileFileMd5sum (FilePortalJsonParser.getIndexFileFileMd5sum(o).orElse(null))
          .indexFileFileName   (FilePortalJsonParser.getIndexFileFileName(o).orElse(null))
          .indexFileFileSize   (FilePortalJsonParser.getIndexFileFileSize(o).orElse(null))
          .indexFileId         (FilePortalJsonParser.getIndexFileId(o).orElse(null))
          .indexFileObjectId   (FilePortalJsonParser.getIndexFileObjectId(o).orElse(null))
          .objectId            (FilePortalJsonParser.getObjectId(o))
          .projectCode         (FilePortalJsonParser.getProjectCode(o))
          .sampleIds           (FilePortalJsonParser.getSampleIds(o))
          .specimenIds         (FilePortalJsonParser.getSpecimenIds(o))
          .specimenTypes        (FilePortalJsonParser.getSpecimenTypes(o))
          .submittedDonorId    (FilePortalJsonParser.getSubmittedDonorId(o))
          .submittedSampleIds  (FilePortalJsonParser.getSubmittedSampleIds(o))
          .submittedSpecimenIds(FilePortalJsonParser.getSubmittedSpecimenIds(o))
          .software            (FilePortalJsonParser.getSoftware(o))
          .build();
    } catch (Throwable t){
      log.error("OBJECT_DATA:\n{}", JsonUtils.toPrettyJson(o));
      throw Lombok.sneakyThrow(t);
    }
  }

  public static FileFetcher createFileFetcher(PortalDownloadIterator portalDownloadIterator) {
    return new FileFetcher(portalDownloadIterator);
  }

}
