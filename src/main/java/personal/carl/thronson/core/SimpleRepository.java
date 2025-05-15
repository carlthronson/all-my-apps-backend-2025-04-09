package personal.carl.thronson.core;

import java.util.List;

import personal.carl.thronson.workflow.data.core.ProcessElement;

public interface SimpleRepository<E extends ProcessElement> {

//    E getById(Long id);

//    Optional<E> findById(Long id);

  List<E> findAllById(Long id);

  boolean existsByName(String name);

  E findByName(String name);

  List<E> findAllByName(String name);

  boolean existsByLabel(String label);

  E findByLabel(String label);

  List<E> findAllByLabel(String label);

  List<E> findAll();
}
