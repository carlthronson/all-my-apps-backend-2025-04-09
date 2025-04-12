package personal.carl.thronson.jobsearch.data.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchCompanyEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchTaskEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchCompanyRepository extends ProcessElementRepository<JobSearchCompanyEntity> {

//  Page<JobSearchCompanyEntity> findAllByTasksIn(List<JobSearchTaskEntity> content, Pageable pageable);

}
