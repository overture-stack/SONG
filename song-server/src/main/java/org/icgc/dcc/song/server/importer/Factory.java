package org.icgc.dcc.song.server.importer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import org.icgc.dcc.song.server.importer.convert.DonorConverter;
import org.icgc.dcc.song.server.importer.convert.FileConverter;
import org.icgc.dcc.song.server.importer.convert.FileSetConverter;
import org.icgc.dcc.song.server.importer.convert.SampleSetConverter;
import org.icgc.dcc.song.server.importer.convert.SpecimenSampleConverter;
import org.icgc.dcc.song.server.importer.convert.StudyConverter;
import org.icgc.dcc.song.server.importer.download.PortalDownloadIterator;
import org.icgc.dcc.song.server.importer.download.fetcher.DataFetcher;
import org.icgc.dcc.song.server.importer.download.fetcher.DonorFetcher;
import org.icgc.dcc.song.server.importer.download.fetcher.FileFetcher;
import org.icgc.dcc.song.server.importer.model.DataContainer;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.persistence.filerestorer.impl.ObjectFileRestorer;

import java.util.ArrayList;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.song.server.importer.Config.PERSISTED_DIR_PATH;
import static org.icgc.dcc.song.server.importer.Config.PORTAL_API;
import static org.icgc.dcc.song.server.importer.convert.DonorConverter.createDonorConverter;
import static org.icgc.dcc.song.server.importer.convert.FileConverter.createFileConverter;
import static org.icgc.dcc.song.server.importer.convert.FileSetConverter.createFileSetConverter;
import static org.icgc.dcc.song.server.importer.convert.SampleSetConverter.createSampleSetConverter;
import static org.icgc.dcc.song.server.importer.convert.SpecimenSampleConverter.createSpecimenSampleConverter;
import static org.icgc.dcc.song.server.importer.convert.StudyConverter.createStudyConverter;
import static org.icgc.dcc.song.server.importer.download.PortalDonorIdFetcher.createPortalDonorIdFetcher;
import static org.icgc.dcc.song.server.importer.download.PortalDownloadIterator.createDefaultPortalDownloadIterator;
import static org.icgc.dcc.song.server.importer.download.fetcher.DataFetcher.createDataFetcher;
import static org.icgc.dcc.song.server.importer.download.fetcher.DonorFetcher.createDonorFetcher;
import static org.icgc.dcc.song.server.importer.download.fetcher.FileFetcher.createFileFetcher;
import static org.icgc.dcc.song.server.importer.download.urlgenerator.impl.FilePortalUrlGenerator.createFilePortalUrlGenerator;
import static org.icgc.dcc.song.server.importer.persistence.filerestorer.impl.ObjectFileRestorer.createObjectFileRestorer;

public class Factory {

  public static final DonorConverter DONOR_CONVERTER = createDonorConverter();
  public static final FileConverter FILE_CONVERTER = createFileConverter();
  public static final FileSetConverter FILE_SET_CONVERTER = createFileSetConverter();
  public static final SampleSetConverter SAMPLE_SET_CONVERTER = createSampleSetConverter();
  public static final SpecimenSampleConverter SPECIMEN_SAMPLE_CONVERTER = createSpecimenSampleConverter();
  public static final StudyConverter STUDY_CONVERTER = createStudyConverter();

  public static final ObjectFileRestorer<DataContainer> DATA_CONTAINER_FILE_RESTORER =
      createObjectFileRestorer (PERSISTED_DIR_PATH, DataContainer.class);

  private static PortalDownloadIterator buildFilePortalDownloader(){
    val urlGen = createFilePortalUrlGenerator(PORTAL_API);
    return createDefaultPortalDownloadIterator(urlGen);
  }

  private static ArrayList<PortalFileMetadata> downloadAndConvertPortalFiles(){
    val downloader = buildFilePortalDownloader();
    return (ArrayList<PortalFileMetadata>) downloader.stream()
        .map(FileFetcher::convertToPortalFileMetadata)
        .collect(toList());
  }

  private static <T> ArrayList<T> downloadAndConvert(PortalDownloadIterator downloadIterator,
      Function<ObjectNode, T> convertFunction){
    return (ArrayList<T>) downloadIterator.stream()
        .map(convertFunction)
        .collect(toList());
  }


  public static FileFetcher buildFileFetcher(){
    val urlGenerator = createFilePortalUrlGenerator(PORTAL_API);
    val portalDownloadIterator = createDefaultPortalDownloadIterator(urlGenerator);
    return createFileFetcher(portalDownloadIterator);
  }

  public static DonorFetcher buildDonorFetcher(){
    val portalDonorIdFetcher = createPortalDonorIdFetcher(PORTAL_API);
    return createDonorFetcher(portalDonorIdFetcher);
  }

  public static DataFetcher buildDataFetcher(){
    val fileFetcher = buildFileFetcher();
    val donorFetcher = buildDonorFetcher();
    return createDataFetcher(fileFetcher,donorFetcher);
  }

}
