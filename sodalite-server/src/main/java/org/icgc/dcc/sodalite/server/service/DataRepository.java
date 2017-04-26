package org.icgc.dcc.sodalite.server.service;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.model.Specimen;

public interface DataRepository {
	// return donorId created/found
	String createDonorIfNotExist(String study_id, Donor donor);
	// return specimenId created/found
	String createSpecimenIfNotExist(String donorId, Specimen specimen);
	// return sampleId created/found
	String createSampleIfNotExist(String specimenId, Sample sample);
	// return fileId created/found
	String createFileIfNotExist(String sampleId, File file);
}
