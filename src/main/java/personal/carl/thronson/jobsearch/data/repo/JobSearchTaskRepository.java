package personal.carl.thronson.jobsearch.data.repo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchTaskEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchTaskRepository extends ProcessElementRepository<JobSearchTaskEntity>{

}
