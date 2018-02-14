package org.icgc.dcc.song.importer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.convert.DonorConverter;
import org.icgc.dcc.song.importer.convert.FileConverter;
import org.icgc.dcc.song.importer.convert.FileSetConverter;
import org.icgc.dcc.song.importer.convert.SampleSetConverter;
import org.icgc.dcc.song.importer.convert.SpecimenSampleConverter;
import org.icgc.dcc.song.importer.convert.StudyConverter;
import org.icgc.dcc.song.importer.download.DownloadIterator;
import org.icgc.dcc.song.importer.download.fetcher.DataFetcher;
import org.icgc.dcc.song.importer.download.fetcher.DonorFetcher;
import org.icgc.dcc.song.importer.filters.impl.SpecimenFileFilter;
import org.icgc.dcc.song.importer.model.DataContainer;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.persistence.PersistenceFactory;
import org.icgc.dcc.song.importer.persistence.filerestorer.impl.ObjectFileRestorer;
import org.icgc.dcc.song.importer.storage.SimpleDccStorageClient;

import java.util.function.Supplier;

import static org.icgc.dcc.song.importer.Config.PERSISTED_DIR_PATH;
import static org.icgc.dcc.song.importer.Config.PORTAL_API;
import static org.icgc.dcc.song.importer.convert.SpecimenSampleConverter.createSpecimenSampleConverter;
import static org.icgc.dcc.song.importer.download.PortalDonorIdFetcher.createPortalDonorIdFetcher;
import static org.icgc.dcc.song.importer.download.fetcher.DataFetcher.createDataFetcher;
import static org.icgc.dcc.song.importer.download.fetcher.DonorFetcher.createDonorFetcher;
import static org.icgc.dcc.song.importer.persistence.PersistenceFactory.createPersistenceFactory;
import static org.icgc.dcc.song.importer.persistence.filerestorer.impl.ObjectFileRestorer.createObjectFileRestorer;

@Slf4j
@RequiredArgsConstructor
public class Factory {

  public static final DonorConverter DONOR_CONVERTER = DonorConverter.createDonorConverter();
  public static final FileConverter FILE_CONVERTER = FileConverter.createFileConverter();
  public static final FileSetConverter FILE_SET_CONVERTER = FileSetConverter.createFileSetConverter();
  public static final SampleSetConverter SAMPLE_SET_CONVERTER = SampleSetConverter.createSampleSetConverter();
  public static final SpecimenSampleConverter SPECIMEN_SAMPLE_CONVERTER = createSpecimenSampleConverter();
  public static final StudyConverter STUDY_CONVERTER = StudyConverter.createStudyConverter();

  private final SimpleDccStorageClient simpleDccStorageClient;
  private final DownloadIterator<DccMetadata> dccMetadataDownloadIterator;
  private final DownloadIterator<PortalFileMetadata> portalFileMetadataDownloadIterator;
  private final SpecimenFileFilter specimenFileFilter;

  public static final ObjectFileRestorer<DataContainer> DATA_CONTAINER_FILE_RESTORER =
      createObjectFileRestorer (PERSISTED_DIR_PATH, DataContainer.class);

  public static DonorFetcher buildDonorFetcher(){
    log.info("Creating PortalDonorIdFetcher for url: {}", PORTAL_API);
    val portalDonorIdFetcher = createPortalDonorIdFetcher(PORTAL_API);

    log.info("Creating DonorFetcher");
    return createDonorFetcher(portalDonorIdFetcher);
  }

  public DataFetcher buildDataFetcher(){

    log.info("Building DonorFetcher");
    val donorFetcher = buildDonorFetcher();

    log.info("Creating DataFetcher");
    return createDataFetcher(donorFetcher, specimenFileFilter, dccMetadataDownloadIterator,
        simpleDccStorageClient, portalFileMetadataDownloadIterator );
  }

  public static PersistenceFactory<DataContainer, String>  buildPersistenceFactory(Supplier<DataContainer> supplier){
    log.info("Creating PersistenceFactory for DataContainer FileRestorer: {}", DATA_CONTAINER_FILE_RESTORER);
    return createPersistenceFactory(DATA_CONTAINER_FILE_RESTORER, supplier);
  }

}
