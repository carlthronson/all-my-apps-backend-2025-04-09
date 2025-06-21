package personal.carl.thronson.jobsearch.data.repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchJobDescriptionEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchJobDescriptionRepository extends ProcessElementRepository<JobSearchJobDescriptionEntity> {

  Optional<JobSearchJobDescriptionEntity> findByListing(JobSearchJobListingEntity listing);

  @Query("SELECT j FROM job_search_job_description j WHERE j.analysis IS NULL")
  Page<JobSearchJobDescriptionEntity> findAllWhereAnalysisIsNull(Pageable pageable);
}
