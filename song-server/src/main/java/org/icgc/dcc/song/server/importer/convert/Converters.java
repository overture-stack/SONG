package org.icgc.dcc.song.server.importer.convert;

import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.model.PortalSampleData;
import org.icgc.dcc.song.server.importer.model.PortalSpecimenData;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;

public class Converters {

  public static Donor convertToDonor(PortalDonorMetadata portalDonorMetadata){
    return null;
  }

  public static Specimen convertToSpecimen(PortalSpecimenData portalSpecimenData){
    return null;
  }

  public static Sample convertToSample(PortalSampleData portalSampleData){
    return null;
  }

  public static File convertToFile(PortalFileMetadata portalFileMetadata){
    return null;
  }

  public static Analysis convertToAnalysis(PortalFileMetadata portalFileMetadata){
    return null;
  }

  public static SequencingRead convertToSequencingRead(PortalFileMetadata portalFileMetadata){
    return null;
  }

  public static VariantCall convertToVariantCall(PortalFileMetadata portalFileMetadata){
    return null;

  }

  public static Upload convertToUpload(PortalFileMetadata portalFileMetadata){

  }

  public static Study convertToStudy(PortalDonorMetadata portalDonorMetadata){

  }
}
