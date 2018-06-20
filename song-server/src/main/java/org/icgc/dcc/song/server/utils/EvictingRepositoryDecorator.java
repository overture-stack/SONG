package org.icgc.dcc.song.server.utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@NoRepositoryBean
public class EvictingRepositoryDecorator<T, ID> implements JpaRepository<T, ID> {

  /**
   * Dependencies
   */
  @NonNull private final Evictor evictor;
  @NonNull private final JpaRepository<T,ID> internalRepository;

  @Override public List<T> findAll() {
    return evictor.evictList(internalRepository.findAll());
  }

  @Override public List<T> findAll(Sort sort) {
    return evictor.evictList(internalRepository.findAll(sort));
  }

  @Override public List<T> findAllById(Iterable<ID> iterable) {
    return evictor.evictList(internalRepository.findAllById(iterable));
  }

  @Override public <S extends T> List<S> saveAll(Iterable<S> iterable) {
    return evictor.evictList(internalRepository.saveAll(iterable));
  }

  @Override public void flush() {
    internalRepository.flush();
  }

  @Override public <S extends T> S saveAndFlush(S s) {
    return evictor.evictObject(internalRepository.saveAndFlush(s));
  }

  @Override public void deleteInBatch(Iterable<T> iterable) {
    internalRepository.deleteInBatch(iterable);
  }

  @Override public void deleteAllInBatch() {
    internalRepository.deleteAllInBatch();
  }

  @Override public T getOne(ID id) {
    return evictor.evictObject(internalRepository.getOne(id));
  }

  @Override public <S extends T> List<S> findAll(Example<S> example) {
    return evictor.evictList(internalRepository.findAll(example));
  }

  @Override public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
    return evictor.evictList(internalRepository.findAll(example, sort));
  }

  @Override public Page<T> findAll(Pageable pageable) {
    return evictor.evictPage(internalRepository.findAll(pageable));
  }

  @Override public <S extends T> S save(S s) {
    return evictor.evictObject(internalRepository.save(s));
  }

  @Override public Optional<T> findById(ID id) {
    return evictor.evictOptional(internalRepository.findById(id));
  }

  @Override public boolean existsById(ID id) {
    return internalRepository.existsById(id);
  }

  @Override public long count() {
    return internalRepository.count();
  }

  @Override public void deleteById(ID id) {
    internalRepository.deleteById(id);
  }

  @Override public void delete(T t) {
    internalRepository.delete(t);
  }

  @Override public void deleteAll(Iterable<? extends T> iterable) {
    internalRepository.deleteAll(iterable);
  }

  @Override public void deleteAll() {
    internalRepository.deleteAll();
  }

  @Override public <S extends T> Optional<S> findOne(Example<S> example) {
    return evictor.evictOptional(internalRepository.findOne(example));
  }

  @Override public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
    return evictor.evictPage(internalRepository.findAll(example, pageable));
  }

  @Override public <S extends T> long count(Example<S> example) {
    return internalRepository.count(example);
  }

  @Override public <S extends T> boolean exists(Example<S> example) {
    return internalRepository.exists(example);
  }

  public static <T, ID> EvictingRepositoryDecorator<T, ID> createEvictingRepositoryDecorator(Evictor evictor,
      JpaRepository<T, ID> internalRepository) {
    return new EvictingRepositoryDecorator<T, ID>(evictor, internalRepository);
  }

}
