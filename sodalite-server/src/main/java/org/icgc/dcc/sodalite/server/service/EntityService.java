package org.icgc.dcc.sodalite.server.service;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.Entity;
import org.icgc.dcc.sodalite.server.model.json.create.CreateEntity;
import org.icgc.dcc.sodalite.server.model.json.update.entity.EntityUpdate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor

public class EntityService {
	private void info(String fmt, Object... args) {
		log.info(format(fmt, args));
	}
	public int create(CreateEntity request) {
		// TODO Auto-generated method stub
		info("Called CreateOrUpdate with '%s'\n", request);
		return 0;
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
	public int modify(EntityUpdate entityUpdate) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
