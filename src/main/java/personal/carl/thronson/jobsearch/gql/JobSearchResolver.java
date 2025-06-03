package personal.carl.thronson.jobsearch.gql;

import java.awt.print.Pageable;
import java.time.Duration;
import java.util.List;

import org.hibernate.query.SortDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import graphql.schema.DataFetchingEnvironment;
import personal.carl.thronson.jobsearch.data.entity.JobSearchCompanyEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchPhaseEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchStatusEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchTaskEntity;
import personal.carl.thronson.jobsearch.data.repo.JobSearchCompanyRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchJobListingRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchPhaseRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchStatusRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchTaskRepository;
import reactor.core.publisher.Flux;

@RestController
@Transactional
public class JobSearchResolver {

  @Autowired
  private JobSearchPhaseRepository jobSearchPhaseRepository;

  @Autowired
  private JobSearchStatusRepository jobSearchStatusRepository;

  @Autowired
  private JobSearchJobListingRepository jobSearchJobListingRepository;

  @Autowired
  private JobSearchCompanyRepository jobSearchCompanyRepository;

  @Autowired
  private JobSearchTaskRepository jobSearchTaskRepository;

//  @Autowired
//  private JobSearchService service;

  @QueryMapping(name = "getJobSearchPhases")
  public List<JobSearchPhaseEntity> getJobSearchPhases(
      DataFetchingEnvironment environment) throws Exception {
    return jobSearchPhaseRepository.findAll();
  }

  @QueryMapping(name = "getJobSearchStatuses")
  public List<JobSearchStatusEntity> getJobSearchStatuses(
      DataFetchingEnvironment environment) throws Exception {
    return jobSearchStatusRepository.findAll();
  }

  @QueryMapping(name = "getJobSearchTasks")
  public Page<JobSearchTaskEntity> getJobSearchTasks(
      DataFetchingEnvironment environment) throws Exception {
    int pageNumber = 0;
    int pageSize = 100;
    String sortDirection = "ASC";
    List<String> sortProperties = List.of("id");
    String[] properties = sortProperties.toArray(new String[0]);
    Direction direction = Direction.valueOf(sortDirection);
    PageRequest pageable = PageRequest.of(pageNumber, pageSize, direction, properties);
    return jobSearchTaskRepository.findAll(pageable);
  }

  @QueryMapping(name = "getJobSearchJobListings")
  public Page<JobSearchJobListingEntity> getJobSearchJobListings(
      @Argument(name = "pageNumber") int pageNumber,
      @Argument(name = "pageSize") int pageSize,
      @Argument(name = "sortDirection") String sortDirection,
      @Argument(name = "sortProperties") List<String> sortProperties,
      DataFetchingEnvironment environment) throws Exception {

    String[] properties = sortProperties.toArray(new String[0]);
    Direction direction = Direction.valueOf(sortDirection);
    PageRequest pageable = PageRequest.of(pageNumber, pageSize, direction, properties);
    return jobSearchJobListingRepository.findAll(pageable);
  }

  @QueryMapping(name = "getJobSearchCompanies")
  public List<JobSearchCompanyEntity> getJobSearchCompanies(
      DataFetchingEnvironment environment) throws Exception {
    return jobSearchCompanyRepository.findAll();
  }

//  @MutationMapping(name = "createJobListing")
//  public Optional<JobSearchJobListingEntity> createJobListing(
//      @Argument(name = "name") String name,
//      @Argument(name = "label") String label,
//      @Argument(name = "companyName") String companyName,
//      @Argument(name = "location") String location,
//      @Argument(name = "linkedinid") long linkedinid,
//      @Argument(name = "linkedinurl") String linkedinurl,
//      @Argument(name = "contracttype") String contracttype,
//      @Argument(name = "experiencelevel") String experiencelevel,
//      @Argument(name = "salary") String salary,
//      @Argument(name = "sector") String sector,
//      @Argument(name = "worktype") String worktype,
//      @Argument(name = "publishedAt") OffsetDateTime publishedAt,
//      DataFetchingEnvironment environment) throws Exception {
//    JobSearchJobListingEntity entity = new JobSearchJobListingEntity();
//    entity.setName(name);
//    entity.setLabel(label);
//    entity.setLinkedinid(linkedinid);
//    entity.setLinkedinurl(linkedinurl);
//    entity.setContracttype(contracttype);
//    entity.setExperiencelevel(experiencelevel);
//    entity.setPublishedAt(publishedAt);
//    entity.setSalary(salary);
//    entity.setSector(sector);
//    entity.setWorktype(worktype);
//    return saveJobListing(entity);
//  }

//  private Optional<JobSearchJobListingEntity> saveJobListing(JobSearchJobListingEntity entity) {
//    return service.saveJobListing(entity);
////    return Optional.of(jobSearchJobListingRepository.save(entity));
//  }

//  @MutationMapping(name = "createCompany")
//  public Optional<JobSearchCompanyEntity> createCompany(
//      @Argument(name = "name") String name,
//      @Argument(name = "label") String label,
//      @Argument(name = "location") String location,
//      DataFetchingEnvironment environment) throws Exception {
//    return service.createCompany(name, label, location);
//  }

//  @MutationMapping(name = "createTask")
//  public Optional<JobSearchTaskEntity> createTask(
//      @Argument(name = "name") String name,
//      @Argument(name = "label") String label,
//      @Argument(name = "location") String location,
//      @Argument(name = "jobId") Long jobId,
//      @Argument(name = "companyId") Long companyId,
//      DataFetchingEnvironment environment) throws Exception {
//    return jobSearchJobListingRepository.findById(jobId) // Returns Optional<RelatedEntity1>
//      .flatMap(jobListingEntity -> jobSearchCompanyRepository.findById(companyId) // Returns Optional<RelatedEntity2>
//        .flatMap(companyEntity -> jobSearchStatusRepository.findByName(JobSearchStatusEntity.JOB_SEARCH_STATUS_FOUND)
//          .map(statusEntity -> {
//            JobSearchTaskEntity newEntity = new JobSearchTaskEntity();
//            newEntity.setStatus(statusEntity);
//            newEntity.setName(name);
//            newEntity.setLabel(label);
//            newEntity.setCompany(companyEntity);
//            newEntity.setJob(jobListingEntity);
//            return jobSearchTaskRepository.save(newEntity);
//          })));
//  }

  @SubscriptionMapping(name = "ping")
  public Flux<String> ping() {
      return Flux.interval(Duration.ofSeconds(1))
          .map(i -> "ping " + i);
  }

//  @SubscriptionMapping(name = "newJobs")
//  public Flux<JobSearchTaskEntity> newJobs() {
//    return jobSearchTaskSink.asFlux().publish().autoConnect(0); // Allows new subscribers to get latest data
//  }
}
