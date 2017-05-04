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
import org.icgc.dcc.sodalite.server.repository.EntityRepository;


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
public class OldEntityService {
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
	
	Map<String, EntityRepository<?>> repositoryMap;
	
	void setupRepositoryMap() {
		repositoryMap=new HashMap<>();
		repositoryMap.put(DONOR_ID_PREFIX, donorRepository);
		repositoryMap.put(SPECIMEN_ID_PREFIX, specimenRepository);
		repositoryMap.put(SAMPLE_ID_PREFIX, sampleRepository);
		repositoryMap.put(FILE_ID_PREFIX, fileRepository);
	}
	
	
	private void info(String fmt, Object... args) {
		log.info(format(fmt, args));
	}
	
	EntityRepository<?> getRepository(String id) {
		if (repositoryMap == null) {
			setupRepositoryMap();
		}
		String key=id.substring(0,2);
		return repositoryMap.get(key);
	}
	
	@SneakyThrows
	public String create(String studyId, String json) {
		long start=System.currentTimeMillis();
		
		info("Creating a new entity for study '%s', with json '%s'",studyId,json);
		@NonNull
		Study study = studyRepository.get(studyId);
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
		fileRepository.save(fileId, sampleId, file.getFileName(), file.getFileSize(),
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
		sample.setFiles(fileRepository.findByParentId(sample.getSampleId()));
	}
	
	void setSamples(Specimen specimen) {
		specimen.setSamples(sampleRepository.findByParentId(specimen.getSpecimenId()));
		for(val sample: specimen.getSamples()) {
			setFiles(sample);
		}
	}
	
	void setSpecimens(Donor donor) {
		donor.setSpecimens(specimenRepository.findByParentId(donor.getDonorId()));
		for(val specimen: donor.getSpecimens()) {
			setSamples(specimen);
		}
	}
	
	void setDonors(Study study) {
		study.setDonors(donorRepository.getByParentId(study.getStudyId()));
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
		Study s= studyRepository.get(id);
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
	
	@SneakyThrows
	public String update(String studyId, String json) {
		info("Updating entities for study '%s', with json '%s'",studyId,json);
		
		@NonNull
		JsonNode root = mapper.readTree(json);
		assert(root.isContainerNode()); // array of entities
		
		int i=0;
		for(JsonNode n: root) {
			i = i+1;
			String id= findId(n);
			
			String type=id.substring(0,2);
			
			switch(type) {
				case DONOR_ID_PREFIX: return updateDonor(id, root);
				case SPECIMEN_ID_PREFIX: return updateSpecimen(id, root);
				case SAMPLE_ID_PREFIX: return updateSample(id, root);
				case FILE_ID_PREFIX: return updateFile(id,root);
				default: return "Error: Unknown ID type"+type;
			}
			
		}
		return "OK";
	}
	
	String findId(JsonNode n) {
		String[] names={"studyId", "donorId","specimenId","sampleId","fileId"};
		for(String name: names) {
			JsonNode idNode=n.path(name);
			if (idNode != null) {
				return idNode.asText();
			}
		}
		return "";
	}
	
	private String updateFile(String id, JsonNode root) {
		// TODO Auto-generated method stub
		return null;
	}

	private String updateSample(String id, JsonNode root) {
		// TODO Auto-generated method stub
		return null;
	}

	private String updateSpecimen(String id, JsonNode root) {
		// TODO Auto-generated method stub
		return null;
	}

	private String updateDonor(String id, JsonNode root) {
		// TODO Auto-generated method stub
		return null;
	}

	public String delete(List<String> ids) {
		Map<String,String> results = new HashMap<>();
		for (val id: ids) {
			val status=deleteId(id);
			results.put(id,  status);
		}
		return json(results);
		
	}
	
	String deleteFile(String id) {
		fileRepository.delete(id);
		return "ok";
	}
	
	String deleteSample(String id) {
		// TODO: Wrap these in transactions
		fileRepository.deleteBySampleId(id);
		sampleRepository.delete(id);
		return "ok";
	}
	
	String deleteSpecimen(String id) {
		List<String> ids = sampleRepository.getIds(id);
		for(val sampleId:ids) {
			deleteSample(sampleId);
		}
		specimenRepository.delete(id);
		return "ok";
	}
	
	String deleteDonor(String id) {
		List<String> ids=specimenRepository.getIds(id);
		for(val specimenId:ids) {
			deleteSpecimen(specimenId);
		}
		donorRepository.delete(id);
		return "ok";
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
			case DONOR_ID_PREFIX: return deleteDonor(id);
			case SPECIMEN_ID_PREFIX: return deleteSpecimen(id);
			case SAMPLE_ID_PREFIX: return deleteSample(id);
			case FILE_ID_PREFIX: return deleteFile(id);
			default: return "Error: Unknown ID type"+type;
		}
	}
}
