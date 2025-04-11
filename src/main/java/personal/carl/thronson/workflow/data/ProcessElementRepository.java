package personal.carl.thronson.workflow.data;

import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;

import personal.carl.thronson.core.BaseRepository;
import personal.carl.thronson.workflow.data.core.ProcessElement;

@NoRepositoryBean
public interface ProcessElementRepository<ENTITY extends ProcessElement> 
  extends BaseRepository<ENTITY> {

  Optional<ENTITY> findByName(String name);
}
