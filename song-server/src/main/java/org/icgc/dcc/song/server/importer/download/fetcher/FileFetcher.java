package org.icgc.dcc.song.server.importer.download.fetcher;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Lombok;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.importer.download.PortalDownloadIterator;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getAccess;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getDataType;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getDonorId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getExperimentalStrategy;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getFileFormat;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getFileId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getFileLastModified;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getFileMd5sum;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getFileName;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getFileSize;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getGenomeBuild;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getIndexFileFileFormat;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getIndexFileFileMd5sum;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getIndexFileFileName;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getIndexFileFileSize;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getIndexFileId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getIndexFileObjectId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getObjectId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getProjectCode;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getRepoDataBundleId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getSampleIds;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getSoftware;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getSpecimenIds;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getSpecimenTypes;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getSubmittedDonorId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getSubmittedSampleIds;
import static org.icgc.dcc.song.server.importer.convert.PortalFileJsonParser.getSubmittedSpecimenIds;

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
      log.error("OBJECT_DATA:\n{}", JsonUtils.toPrettyJson(o));
      throw Lombok.sneakyThrow(t);
    }
  }

  public static FileFetcher createFileFetcher(PortalDownloadIterator portalDownloadIterator) {
    return new FileFetcher(portalDownloadIterator);
  }

}
