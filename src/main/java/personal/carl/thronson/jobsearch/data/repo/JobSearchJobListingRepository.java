package personal.carl.thronson.jobsearch.data.repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchJobListingRepository extends ProcessElementRepository<JobSearchJobListingEntity> {

  Optional<JobSearchJobListingEntity> findByLinkedinid(Long lid);

  @Query(
      value = """
          SELECT j
          FROM job_search_job_listing j
          LEFT JOIN FETCH j.company c
          LEFT JOIN FETCH j.task t
          LEFT JOIN FETCH t.status s
          LEFT JOIN FETCH s.phase p
          LEFT JOIN FETCH j.description d
          LEFT JOIN FETCH d.analysis a
          """,
      countQuery = "SELECT COUNT(j) FROM job_search_job_listing j"
  )
  Page<JobSearchJobListingEntity> findAllWithAllRelations(Pageable pageable);

  @Query("SELECT j FROM job_search_job_listing j WHERE j.description IS NULL")
  Page<JobSearchJobListingEntity> findAllWhereDescriptionIsNull(Pageable pageable);
}
