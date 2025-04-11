package personal.carl.thronson.jobsearch.data.repo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchPhaseEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchPhaseRepository extends ProcessElementRepository<JobSearchPhaseEntity>{

}
