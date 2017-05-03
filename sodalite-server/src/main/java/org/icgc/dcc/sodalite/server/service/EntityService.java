package org.icgc.dcc.sodalite.server.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.model.Entity;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.model.Study;

import org.icgc.dcc.sodalite.server.repository.DonorRepository;
import org.icgc.dcc.sodalite.server.repository.FileRepository;
import org.icgc.dcc.sodalite.server.repository.SampleRepository;
import org.icgc.dcc.sodalite.server.repository.SpecimenRepository;
import org.icgc.dcc.sodalite.server.repository.StudyRepository;

import static com.google.common.base.Strings.isNullOrEmpty;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
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
	public static final String DONOR_ID_PREFIX = "DO"; 
	public static final String SPECIMEN_ID_PREFIX = "SP";
	public static final String SAMPLE_ID_PREFIX = "SA"; 
	public static final String MUTATION_ID_PREFIX = "MU"; 
	public static final String FILE_ID_PREFIX = "FI";
	public static final int SUCCESS=1; // sql functions return  1 for success, 0 for failure
	ObjectMapper mapper=new ObjectMapper();
	@Autowired
	IdService idService;
	@Autowired
	StudyRepository studyRepository;
	@Autowired
	DonorRepository donorRepository;
	@Autowired
	SpecimenRepository specimenRepository;
	@Autowired
	SampleRepository sampleRepository;
	@Autowired
	FileRepository fileRepository;
	
	
	private void info(String fmt, Object... args) {
		log.info(format(fmt, args));
	}
	
	@SneakyThrows
	public String create(String studyId, String json) {
		long start=System.currentTimeMillis();
		
		info("Creating a new entity for study '%s', with json '%s'",studyId,json);
		@NonNull
		Study study = studyRepository.getById(studyId);
		if (study == null) {
			return "{\"status\": \"Study " + studyId + " does not exist: please create it first.\"}";
		}
		info("Got study '%s",study.toString());
		study.setStudyId(studyId);
		
		@NonNull
		JsonNode node = mapper.readTree(json);

		@NonNull
		JsonNode create=node.path("createEntity");
		info("Got createEntity node '%s",create.toString());
		@NonNull
		JsonNode donors=create.path("donors");
		assert(donors.isContainerNode());
		info("donors is '%s",donors.toString());
		List<Donor> donorList = new ArrayList<Donor>();
		for(JsonNode n: donors) {
			info("Converting this JSON to a DONOR object '%s'",n.toString());
			Donor donor = mapper.treeToValue(n, Donor.class);
			donorList.add(donor);
			createDonor(studyId, donor);
		}
		study.setDonors(donorList);
		long end=System.currentTimeMillis();
		info("Elapsed time = %d seconds",(end-start)/1000);
		return mapper.writeValueAsString(study);
	}
	
	
	String createDonor(String studyId, Donor donor) {
		info("Called createDonor with studyId '%s', donor '%s'",studyId,donor.toString());
		String donorId = donor.getDonorId();
		if (isNullOrEmpty(donorId)) {
			donorId=idService.generateDonorId();
			info("Set null donorId to '%s",donorId.toString());
			donor.setDonorId(donorId);
		} else {
			info("Non-null donorId was '%s'", donor.toString());
		}
		info("Donor is now '%s'",donor.toString());
		donorRepository.save(donor.getDonorId(),studyId, donor.getDonorSubmitterId(), donor.getDonorGender().toString());
		for(Specimen specimen: donor.getSpecimens()) {
			createSpecimen(donorId, specimen);
		}
		return donorId;
	}
	
	String createSpecimen(String donorId, Specimen specimen) {
		info("Creating specimen with donorId=%s, specimen=%s",donorId.toString(), specimen.toString());
		String specimenId = specimen.getSpecimenId();
		if(isNullOrEmpty(specimenId)) {
			specimenId=idService.generateSpecimenId();
			specimen.setSpecimenId(specimenId);
		}
		info("Specimen id is '%s",specimenId);
		specimenRepository.save(specimenId, donorId, specimen.getSpecimenSubmitterId(), specimen.getSpecimenClass().toString(), 
				specimen.getSpecimenType().toString());
		
		for(Sample sample: specimen.getSamples()) {
			createSample(specimenId, sample);
		}
		return specimenId;
	}
	
	String createSample(String specimenId, Sample sample) {
		info("Creating sample with specimenId '%s', sample='%s'",specimenId.toString(), sample.toString());
		String sampleId=sample.getSampleId();
		if(isNullOrEmpty(sampleId)) {
			sampleId=idService.generateSampleId();
		}
		sample.setSampleId(sampleId);
		info("Sample id is '%s'",sampleId);
		sampleRepository.save(sampleId, specimenId, sample.getSampleSubmitterId(), sample.getSampleType().toString());
		for(File file:sample.getFiles()) {
			createFile(sampleId, file);
		}
		return sampleId;
	}
	
	String createFile(String sampleId, File file) {
		String fileId=file.getObjectId();
		if(isNullOrEmpty(fileId)) {
			fileId=idService.generateFileId();
		}
		file.setObjectId(fileId);;
		info("Saving to file repository with fileId=%s,file='%s'",fileId,file.toString());
		fileRepository.save(fileId, sampleId, file.getFileName(), file.getFileSize(), file.getFileMd5(), 
				file.getFileType().toString());
		return fileId;
	}
		
	public String getEntityById(String id) {
		String type=id.substring(0,2);
		
		switch(type) {
			case DONOR_ID_PREFIX: return json(getDonorById(id));
			case SPECIMEN_ID_PREFIX: return json(getSpecimenById(id));
			case SAMPLE_ID_PREFIX: return json(getSampleById(id));
			case FILE_ID_PREFIX: return json(getFileById(id));
			default: return "Error: Unknown ID type"+type;
		}
	}

	@SneakyThrows
	private String json(Object o) {
		return mapper.writeValueAsString(o);
	}
	
	void setFiles(Sample sample) {
		sample.setFiles(fileRepository.getBySampleId(sample.getSampleId()));
	}
	
	void setSamples(Specimen specimen) {
		specimen.setSamples(sampleRepository.getBySpecimenId(specimen.getSpecimenId()));
		for(val sample: specimen.getSamples()) {
			setFiles(sample);
		}
	}
	
	void setSpecimens(Donor donor) {
		donor.setSpecimens(specimenRepository.getByDonorId(donor.getDonorId()));
		for(val specimen: donor.getSpecimens()) {
			setSamples(specimen);
		}
	}
	
	void setDonors(Study study) {
		study.setDonors(donorRepository.getDonorsByStudyId(study.getStudyId()));
		for(val donor: study.getDonors()) {
			setSpecimens(donor);
		}
	}

	@SneakyThrows
	File getFileById(String id) {
		File file = fileRepository.getById(id);
		return file;
	}
	
	@SneakyThrows
	Sample getSampleById(String id) {
		Sample sample = sampleRepository.getById(id);
		setFiles(sample);
		return sample;
	}
	
	@SneakyThrows
	Specimen getSpecimenById(String id) {
		Specimen specimen = specimenRepository.getById(id);
		setSamples(specimen);
		return specimen;
	}
	
	@SneakyThrows
	Donor getDonorById(String id) {
		Donor donor = donorRepository.getById(id);
		setSpecimens(donor);
		return donor;
	}
	
	@SneakyThrows
	public String getStudyById(String id) {
		Study s= studyRepository.getById(id);
		if (s == null) {
			return "{ \"msg\": \"Study with id '" + id +"' does not exist\"}";
		}
		info(mapper.writeValueAsString(s));
		setDonors(s);

		return mapper.writeValueAsString(s);
	}

	public List<Entity> getEntities(Map<String, String> params) {
		info("Called getEntities with '%s'", params);
		return null;
	}
	
	public String update(String study_id, String json) {
		//TODO Stub
		return "Not implemented yet";
	}
	
	public String delete(List<String> ids) {
		Map<String,String> results = new HashMap<>();
		for (val id: ids) {
			val status=deleteId(id);
			results.put(id,  status);
		}
		return json(results);
		
	}
	
	int deleteFile(String id) {
		return fileRepository.delete(id);
	}
	
	int deleteSample(String id) {
		// TODO: Wrap these in transactions
		fileRepository.deleteBySampleId(id);
		return sampleRepository.delete(id);
	}
	
	int deleteSpecimen(String id) {
		List<String> ids = sampleRepository.getSampleIdsBySpecimenId(id);
		for(val sampleId:ids) {
			deleteSample(sampleId);
		}
		return specimenRepository.delete(id);
	}
	
	int deleteDonor(String id) {
		List<String> ids=specimenRepository.getSpecimenIdsByDonorId(id);
		for(val specimenId:ids) {
			deleteSpecimen(specimenId);
		}
		return donorRepository.delete(id);
	}
	
	String statusMsg(int status) {
		if (status == SUCCESS) {
			return "OK";
		} else {
			return "FAILED";
		}
	}
	
	String deleteId(String id) {
		String type=id.substring(0,2);
		
		switch(type) {
			case DONOR_ID_PREFIX: return statusMsg(deleteDonor(id));
			case SPECIMEN_ID_PREFIX: return statusMsg(deleteSpecimen(id));
			case SAMPLE_ID_PREFIX: return statusMsg(deleteSample(id));
			case FILE_ID_PREFIX: return statusMsg(deleteFile(id));
			default: return "Error: Unknown ID type"+type;
		}
	}
}
