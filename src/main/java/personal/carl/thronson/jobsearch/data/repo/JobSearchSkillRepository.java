package personal.carl.thronson.jobsearch.data.repo;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchSkillEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchSkillRepository extends ProcessElementRepository<JobSearchSkillEntity>{

  List<JobSearchSkillEntity> findByAnalysis_Id(Long analysisId);
}
