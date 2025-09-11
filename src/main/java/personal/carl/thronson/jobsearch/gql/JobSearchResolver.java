package personal.carl.thronson.jobsearch.gql;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import graphql.schema.DataFetchingEnvironment;
import personal.carl.thronson.ai.data.JobVector;
import personal.carl.thronson.ai.svc.JobVectorService;
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

  protected static ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private JobSearchPhaseRepository jobSearchPhaseRepository;

  @Autowired
  private JobSearchStatusRepository jobSearchStatusRepository;

  @Autowired
  private JobSearchCompanyRepository jobSearchCompanyRepository;

  @Autowired
  private JobSearchTaskRepository jobSearchTaskRepository;

  @Autowired
  private JobSearchJobListingRepository jobSearchJobListingRepository;

  @Autowired
  private JobVectorService vectorEmbeddingService;

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
  public List<JobSearchTaskEntity> getJobSearchTasks(
      DataFetchingEnvironment environment) throws Exception {
    return jobSearchTaskRepository.findAllWithAllRelations();
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

  @QueryMapping(name = "findSimilarJobs")
  public List<JobVector> findSimilarJobs(
      @Argument(name = "query") String query,
      @Argument(name = "topK") int topK,
      DataFetchingEnvironment environment) throws Exception {
    return vectorEmbeddingService.findSimilarDocuments(query, topK).stream()
        .map(doc -> {
          JobVector vector = new JobVector();
          vector.setScore(doc.getScore());
          vector.setText(doc.getText());
          Map<String, Object> metadata = doc.getMetadata();
          vector.setMetadata(metadata.toString());
          Long jobId = Long.parseLong(metadata.get("entity_id").toString());
          jobSearchJobListingRepository.findById(jobId).ifPresent(jobListing -> {
            vector.setJobListing(jobListing);
          });
          return vector;
        })
        .sorted((a, b) -> b.getJobListing().getPublishedAt().compareTo(a.getJobListing().getPublishedAt()))
        .collect(Collectors.toList());
  }
//  @SubscriptionMapping(name = "newJobs")
//  public Flux<JobSearchTaskEntity> newJobs() {
//    return jobSearchTaskSink.asFlux().publish().autoConnect(0); // Allows new subscribers to get latest data
//  }
}
