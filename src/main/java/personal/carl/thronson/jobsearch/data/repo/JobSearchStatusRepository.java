package personal.carl.thronson.jobsearch.data.repo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchStatusEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchStatusRepository extends ProcessElementRepository<JobSearchStatusEntity>{

}
