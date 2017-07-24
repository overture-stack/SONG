package org.icgc.dcc.song.importer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.convert.DonorConverter;
import org.icgc.dcc.song.importer.convert.FileConverter;
import org.icgc.dcc.song.importer.convert.FileSetConverter;
import org.icgc.dcc.song.importer.convert.SampleSetConverter;
import org.icgc.dcc.song.importer.convert.SpecimenSampleConverter;
import org.icgc.dcc.song.importer.convert.StudyConverter;
import org.icgc.dcc.song.importer.download.PortalDownloadIterator;
import org.icgc.dcc.song.importer.download.fetcher.DataFetcher;
import org.icgc.dcc.song.importer.download.fetcher.DonorFetcher;
import org.icgc.dcc.song.importer.download.fetcher.FileFetcher;
import org.icgc.dcc.song.importer.filters.FileFilter;
import org.icgc.dcc.song.importer.model.DataContainer;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.persistence.PersistenceFactory;
import org.icgc.dcc.song.importer.persistence.filerestorer.impl.ObjectFileRestorer;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.song.importer.Config.PERSISTED_DIR_PATH;
import static org.icgc.dcc.song.importer.Config.PORTAL_API;
import static org.icgc.dcc.song.importer.Config.PROBLEMATIC_SPECIMEN_IDS;
import static org.icgc.dcc.song.importer.convert.SpecimenSampleConverter.createSpecimenSampleConverter;
import static org.icgc.dcc.song.importer.download.PortalDonorIdFetcher.createPortalDonorIdFetcher;
import static org.icgc.dcc.song.importer.download.PortalDownloadIterator.createDefaultPortalDownloadIterator;
import static org.icgc.dcc.song.importer.download.fetcher.DataFetcher.createDataFetcher;
import static org.icgc.dcc.song.importer.download.fetcher.DonorFetcher.createDonorFetcher;
import static org.icgc.dcc.song.importer.download.fetcher.FileFetcher.createFileFetcher;
import static org.icgc.dcc.song.importer.download.urlgenerator.impl.FilePortalUrlGenerator.createFilePortalUrlGenerator;
import static org.icgc.dcc.song.importer.filters.FileFilter.createFileFilter;
import static org.icgc.dcc.song.importer.filters.IdFilter.createIdFilter;
import static org.icgc.dcc.song.importer.persistence.PersistenceFactory.createPersistenceFactory;
import static org.icgc.dcc.song.importer.persistence.filerestorer.impl.ObjectFileRestorer.createObjectFileRestorer;

@Slf4j
public class Factory {

  public static final DonorConverter DONOR_CONVERTER = DonorConverter.createDonorConverter();
  public static final FileConverter FILE_CONVERTER = FileConverter.createFileConverter();
  public static final FileSetConverter FILE_SET_CONVERTER = FileSetConverter.createFileSetConverter();
  public static final SampleSetConverter SAMPLE_SET_CONVERTER = SampleSetConverter.createSampleSetConverter();
  public static final SpecimenSampleConverter SPECIMEN_SAMPLE_CONVERTER = createSpecimenSampleConverter();
  public static final StudyConverter STUDY_CONVERTER = StudyConverter.createStudyConverter();

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
    log.info("Creating FilePortalUrlGenerator for url: {}", PORTAL_API);
    val urlGenerator = createFilePortalUrlGenerator(PORTAL_API);

    log.info("Creating PortalDownloadIterator for FilePortalUrlGenerator");
    val portalDownloadIterator = createDefaultPortalDownloadIterator(urlGenerator);

    log.info("Creating FileFetcher");
    return createFileFetcher(portalDownloadIterator);
  }

  public static DonorFetcher buildDonorFetcher(){
    log.info("Creating PortalDonorIdFetcher for url: {}", PORTAL_API);
    val portalDonorIdFetcher = createPortalDonorIdFetcher(PORTAL_API);

    log.info("Creating DonorFetcher");
    return createDonorFetcher(portalDonorIdFetcher);
  }

  public static DataFetcher buildDataFetcher(){

    log.info("Building FileFetcher");
    val fileFetcher = buildFileFetcher();

    log.info("Building DonorFetcher");
    val donorFetcher = buildDonorFetcher();

    log.info("Creating DataFetcher");
    return createDataFetcher(fileFetcher,donorFetcher);
  }

  public static FileFilter buildFileFilter(){
    log.info("Creating specimenIdFilter for problematic specimenIDs: {}", PROBLEMATIC_SPECIMEN_IDS);
    val specimenIdFilter = createIdFilter(PROBLEMATIC_SPECIMEN_IDS, false);
    log.info("Creating FileFilter using specimenIdFilter");
    return createFileFilter(specimenIdFilter);
  }
  public static PersistenceFactory<DataContainer, String>  buildPersistenceFactory(Supplier<DataContainer> supplier){
    log.info("Creating PersistenceFactory for DataContainer FileRestorer: {}", DATA_CONTAINER_FILE_RESTORER);
    return createPersistenceFactory(DATA_CONTAINER_FILE_RESTORER, supplier);
  }

}
