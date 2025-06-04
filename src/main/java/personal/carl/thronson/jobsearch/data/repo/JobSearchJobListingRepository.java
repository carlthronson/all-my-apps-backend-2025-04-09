package personal.carl.thronson.jobsearch.data.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchJobListingRepository extends ProcessElementRepository<JobSearchJobListingEntity> {

  Optional<JobSearchJobListingEntity> findByLinkedinid(Long lid);

  @Query("""
      SELECT j
      FROM job_search_job_listing j
      LEFT JOIN FETCH j.company c
      LEFT JOIN FETCH j.task t
      LEFT JOIN FETCH t.status s
      LEFT JOIN FETCH s.phase p
      LEFT JOIN FETCH j.description d
      ORDER BY j.publishedAt DESC
      """)
  List<JobSearchJobListingEntity> findAllWithAllRelations();
}
