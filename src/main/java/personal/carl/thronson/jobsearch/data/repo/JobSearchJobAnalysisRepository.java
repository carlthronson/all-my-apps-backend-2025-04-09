package personal.carl.thronson.jobsearch.data.repo;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchJobAnalysisEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobDescriptionEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchJobAnalysisRepository extends ProcessElementRepository<JobSearchJobAnalysisEntity> {

  Optional<JobSearchJobAnalysisEntity> findByDescription(JobSearchJobDescriptionEntity description);

}
