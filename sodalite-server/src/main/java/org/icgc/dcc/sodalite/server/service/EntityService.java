package org.icgc.dcc.sodalite.server.service;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Entity;
import org.icgc.dcc.sodalite.server.repository.EntityRepository;

public abstract class EntityService<T extends Entity>{
	/***
	 * This abstract class doesn't work when using jdbi created interface objects as repositories.
	 * They fail with a null pointer exception when called by this class, but they don't fail
	 * when called directly.
	 * 
	 * This class is still slightly useful for keeping the other Entity related service classes 
	 * organized; they all implement the same methods with their own repositories.
	 */
	EntityRepository<T> repository;
	
	void setRepository(EntityRepository<T> repository) {
		this.repository=repository;
	}
	
	public String create(String parentid, T e) {
		return repository.add(parentid, e);
	}
	
	public String update(T e) {
		return repository.update(e);
	}
	
	public String delete(String id) {
		repository.delete(id);
		return "ok";
	}
	
	public T getById(String id) {
		if (repository == null) {
			System.out.println("REPOSITORY IS NULL");
		}
		System.out.println("Calling getById(" + id + ") on repository '" + repository+"'");
		return repository.getById(id);
	}
	public List<T> findByParentId(String parentId) {
		return repository.findByParentId(parentId);
	}

	public String deleteByParentId(String parentId) {
		return repository.deleteByParentId(parentId);
	}
	
}

