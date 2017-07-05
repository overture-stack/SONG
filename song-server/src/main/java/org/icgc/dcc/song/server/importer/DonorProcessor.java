package org.icgc.dcc.song.server.importer;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.icgc.dcc.song.server.importer.convert.Converters.convertToDonor;
import static org.icgc.dcc.song.server.importer.convert.Converters.convertToSample;
import static org.icgc.dcc.song.server.importer.convert.Converters.convertToSpecimen;

public class DonorProcessor implements Runnable {

  @NonNull private final List<PortalDonorMetadata> donors;
  @Autowired DonorRepository donorRepository;
  @Autowired SpecimenRepository specimenRepository;
  @Autowired SampleRepository sampleRepository;

  private DonorProcessor(List<PortalDonorMetadata> donors) {
    this.donors = donors;
  }

  @Override
  public void run() {
    for (val donorMetadata : donors){
      val donor = convertToDonor(donorMetadata);
      donorRepository.create(donor);
      for (val specimenMetadata : donorMetadata.getSpecimens()){
        val specimen = convertToSpecimen(specimenMetadata);
        specimenRepository.create(specimen);
        for (val sampleMetadata : specimenMetadata.getSamples()){
          val sample = convertToSample(sampleMetadata);
          sampleRepository.create(sample);
        }
      }
    }
  }

  public static DonorProcessor createDonorProcessor(List<PortalDonorMetadata> donors) {
    return new DonorProcessor(donors);
  }

}
