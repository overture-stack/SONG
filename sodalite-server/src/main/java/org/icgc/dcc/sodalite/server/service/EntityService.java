package org.icgc.dcc.sodalite.server.service;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.model.Entity;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.repository.DonorRepository;
import org.icgc.dcc.sodalite.server.repository.FileRepository;
import org.icgc.dcc.sodalite.server.repository.SampleRepository;
import org.icgc.dcc.sodalite.server.repository.SpecimenRepository;
import org.icgc.dcc.sodalite.server.repository.StudyRepository;

import static com.google.common.base.Strings.isNullOrEmpty;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntityService {
	public static final String PROJECT_ID_PREFIX = "PR";
	public static final String DONOR_ID_PREFIX = "DO"; 
	public static final String SPECIMEN_ID_PREFIX = "SP";
	public static final String SAMPLE_ID_PREFIX = "SA"; 
	public static final String MUTATION_ID_PREFIX = "MU"; 
	public static final String FILE_ID_PREFIX = "FI";
	
	@Autowired
	IdService idService;
	@Autowired
	StudyRepository studyRepository;
	DonorRepository donorRepository;
	SpecimenRepository specimenRepository;
	SampleRepository sampleRepository;
	FileRepository fileRepository;
	
	private void info(String fmt, Object... args) {
		log.info(format(fmt, args));
	}
	
	@SneakyThrows
	public String create(String studyId, String json) {
		long start=System.currentTimeMillis();
		
		info("Creating a new entity for study '%s', with json '%s'",studyId,json);
		ObjectMapper mapper=new ObjectMapper();
		JsonNode node = mapper.readTree(json);
		JsonNode donors=node.path("createEntity/donors");
		assert(donors.isContainerNode());
		for(JsonNode n: donors) {
			info("Converting this JSON to a DONOR object '%s'",n.toString());
			Donor donor = mapper.readValue(n.toString(),Donor.class);
			createDonor(studyId, donor);
		}
		long end=System.currentTimeMillis();
		info("Elapsed time = %ld seconds",(end-start)/1000);
		return "Entities created";
	}
	
	
	String createDonor(String studyId, Donor donor) {
		String donorId = donor.getDonorId();
		if (isNullOrEmpty(donorId)) {
			donorId=idService.generateDonorId();
			donor.setDonorId(donorId);
		}
	
		donorRepository.save(donor.getDonorId(),studyId, donor.getDonorSubmitterId(), donor.getDonorGender());
		for(Specimen specimen: donor.getSpecimens()) {
			createSpecimen(donorId, specimen);
		}
		return donorId;
	}
	
	String createSpecimen(String donorId, Specimen specimen) {
		String specimenId = specimen.getSpecimenId();
		if(isNullOrEmpty(specimenId)) {
			specimenId=idService.generateSpecimenId();
		}
		
		specimenRepository.save(specimenId, donorId, specimen.getSpecimenSubmitterId(), specimen.getSpecimenClass(), 
				specimen.getSpecimenType());
		
		for(Sample sample: specimen.getSamples()) {
			createSample(specimenId, sample);
		}
		return specimenId;
	}
	
	String createSample(String specimenId, Sample sample) {
		String sampleId=sample.getSampleId();
		if(isNullOrEmpty(sampleId)) {
			sampleId=idService.generateSampleId();
		}
		sampleRepository.save(sampleId, specimenId, sample.getSampleSubmitterId(), sample.getSampleType());
		for(File file:sample.getFiles()) {
			createFile(sampleId, file);
		}
		return sampleId;
	}
	
	String createFile(String sampleId, File file) {
		String fileId=file.getObjectId();
		if(isNullOrEmpty(fileId)) {
			//fileId=idService.generateFileId();
			fileId="Stub";
		}
		fileRepository.save(fileId, sampleId, file.getFileName(), file.getFileSize(), file.getFileMd5(), 
				file.getFileType());
		return fileId;
	}
	
	
	public String getEntityById(String id) {
		String type=id.substring(2);
		switch(type) {
			case PROJECT_ID_PREFIX: return getStudyById(id);
			case DONOR_ID_PREFIX: return getDonorById(id);
			case SPECIMEN_ID_PREFIX: return getSpecimenById(id);
			case SAMPLE_ID_PREFIX: return getSampleById(id);
			case FILE_ID_PREFIX: return getFileBy(id);
			default: return "Error: Unknown ID type"+type;
		}
	}
	
	private String getFileBy(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getSampleById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getSpecimenById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getDonorById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	String getStudyById(String id) {
		return null;
		
	}
	
	
	// getEntityById
	// (1) Lookup entity in table
	// (2) Get parent ids,types
	// (3) Lookup children in table & add them.

//	public int deleteEntity(List<String> ids) {
//		for (val id:ids) {
//			val type=id.substring(2);
//			switch(type) {
//			case PROJECT_ID_PREFIX: return repository.deleteStudy(id);
//			case DONOR_ID_PREFIX: return repository.deleteDonor(id);
//			case SPECIMEN_ID_PREFIX: return repository.deleteSpecimen(id);
//			case SAMPLE_ID_PREFIX: return repository.deleteSample(id);
//			case FILE_ID_PREFIX: return repository.deleteFile(id);
//			}
//		}
//		return 0;
//	}

	public List<Entity> getEntities(Map<String, String> params) {
		info("Called getEntities with '%s'", params);
		return null;
	}
	
	public String update(String study_id, String json) {
		//TODO Stub
		return "Not implemented yet";
	}
	
}
