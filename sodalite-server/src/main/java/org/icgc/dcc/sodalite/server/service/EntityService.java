package org.icgc.dcc.sodalite.server.service;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.model.Entity;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.model.Specimen;

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
	@Autowired
	DataRepository repository;
	private void info(String fmt, Object... args) {
		log.info(format(fmt, args));
	}
	
	@SneakyThrows
	public String create(String study_id, String json) {
		ObjectMapper mapper=new ObjectMapper();
		JsonNode node = mapper.readTree(json);
		JsonNode donors=node.path("donors");
		assert(donors.isContainerNode());
		for(JsonNode n: donors) {
			Donor donor = mapper.readValue(n.toString(),Donor.class);
			createDonor(study_id, donor);
		}
		return "Entities created";
	}
	
	String createDonor(String studyId, Donor donor) {
		String donorId=repository.createDonorIfNotExist(studyId, donor);
		for(Specimen specimen: donor.getSpecimens()) {
			createSpecimen(donorId, specimen);
		}
		return donorId;
	}
	
	String createSpecimen(String donorId, Specimen specimen) {
		String specimenId=repository.createSpecimenIfNotExist(donorId, specimen);
		for(Sample sample: specimen.getSamples()) {
			createSample(specimenId, sample);
		}
		return specimenId;
	}
	
	String createSample(String specimenId, Sample sample) {
		String sampleId=repository.createSampleIfNotExist(specimenId, sample);
		for(File file:sample.getFiles()) {
			createFile(sampleId, file);
		}
		return sampleId;
	}
	
	String createFile(String sampleId, File file) {
		String fileId=repository.createFileIfNotExist(sampleId, file);
		return fileId;
	}
	
	public List<Entity> getEntityById(String id) {
		info("Called GetEntityById with id=%s\n", id);
		// TODO Auto-generated method stub
		return null;
	}

	public int deleteEntity(List<String> ids) {
		info("Called deleteEntity with '%s'", ids);
		info("id is a list of %d elements", ids.size());
		// TODO Auto-generated method stub
		return 0;
	}


	public List<Entity> getEntities(Map<String, String> params) {
		info("Called getEntities with '%s'", params);
		return null;
	}
	
	public String update(String study_id, String json) {
		//TODO Stub
		return "Not implemented yet";
	}
	
}
