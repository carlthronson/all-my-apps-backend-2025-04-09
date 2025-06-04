package personal.carl.thronson.jobsearch.data.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.jobsearch.data.entity.JobSearchStatusEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchTaskEntity;
import personal.carl.thronson.workflow.data.ProcessElementRepository;

@Repository
@Transactional
public interface JobSearchTaskRepository extends ProcessElementRepository<JobSearchTaskEntity>{

  Page<JobSearchTaskEntity> findAllByStatusIn(List<JobSearchStatusEntity> statuses, Pageable pageable);

  @Query("""
      SELECT t
      FROM job_search_task t
      LEFT JOIN FETCH t.status s
      LEFT JOIN FETCH s.phase p
      LEFT JOIN FETCH t.job j
      LEFT JOIN FETCH j.company c
      LEFT JOIN FETCH j.description d
      """)
  List<JobSearchTaskEntity> findAllWithAllRelations();

}
