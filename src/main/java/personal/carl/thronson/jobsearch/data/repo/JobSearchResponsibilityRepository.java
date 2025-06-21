package personal.carl.thronson.jobsearch.data.repo;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchResponsibilityEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchResponsibilityRepository extends ProcessElementRepository<JobSearchResponsibilityEntity>{

  List<JobSearchResponsibilityEntity> findByAnalysis_Id(Long id);
}
