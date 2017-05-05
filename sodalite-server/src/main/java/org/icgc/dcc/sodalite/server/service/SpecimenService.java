package org.icgc.dcc.sodalite.server.service;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.repository.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;
import lombok.val;

@Service
@NoArgsConstructor
public class SpecimenService extends AbstractEntityService<Specimen> {
	@Autowired
	IdService idService;
	@Autowired
	SpecimenRepository repository;
	@Autowired
	SampleService sampleService;
	
	@Override
	public String create(String parentId, Specimen s) {
		String id=idService.generateSpecimenId();
		s.setSpecimenId(id);
		int status=repository.save(id, parentId, s.getSpecimenSubmitterId(), s.getSpecimenClass().toString(), s.getSpecimenType().toString());
		if (status != 1) {
			return "error: Can't create" + s.toString();
		}
		
		for(Sample sample: s.getSamples()) {
			sampleService.create(id, sample);
		}
		
		return "ok:" + id;
	}
	
	@Override
	public String update(Specimen s) {
		repository.set(s.getSpecimenId(), s.getSpecimenSubmitterId(), s.getSpecimenClass().toString(), s.getSpecimenType().toString());
		return "ok";
	}
	
	@Override
	public String delete(String id) {
		sampleService.deleteByParentId(id);
		repository.delete(id);
		
		return "ok";
	}
	
	@Override
	public String deleteByParentId(String parentId) {
		List<String> ids = repository.getIds(parentId);
		for(val id: ids) {
			delete(id);
		}
		return "ok";
	}
	
	@Override
	public Specimen getById(String id) {	
		Specimen specimen = repository.getById(id);
		if (specimen == null) {
			return null;
		}
		specimen.setSamples(sampleService.findByParentId(id));
		return specimen;
	}
	
	@Override
	public List<Specimen> findByParentId(String parentId) {
		List<Specimen> specimens= repository.findByParentId(parentId);
		
		for(Specimen s: specimens) {
			s.setSamples(sampleService.findByParentId(s.getSpecimenId()));
		}
				
		return specimens;
	}
	
}
