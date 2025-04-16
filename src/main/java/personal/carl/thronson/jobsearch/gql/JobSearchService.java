package personal.carl.thronson.jobsearch.gql;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.http.HttpUtils;
import personal.carl.thronson.http.JobSummaryReader;
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
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@Service
@Transactional
public class JobSearchService {

  private static String FORMAT_LINKEDIN_SEARCH_PARAMETERS = "keywords=%s&f_TPR=r%d&origin=JOBS_HOME_SEARCH_BUTTON&refresh=true&start=%d";

  private static String PROTOCOL_HTTPS = "https";
  private static String HOST_LINKEDIN = "www.linkedin.com";
  private static String PATH_LINKEDIN_JOBSEARCH = "/jobs-guest/jobs/api/seeMoreJobPostings/search";

  Logger logger = Logger.getLogger(getClass().getName());

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

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importEngineerJobs() throws Exception {
    importJobs("engineer", 168, 35);
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importSoftwareJobs() throws Exception {
    importJobs("software", 168, 35);
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importDeveloperJobs() throws Exception {
    importJobs("developer", 168, 35);
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importFullstackJobs() throws Exception {
    importJobs("fullstack", 168, 35);
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importBackendJobs() throws Exception {
    importJobs("backend", 168, 35);
  }

  private void importJobs(String keyword, int hours, int max) throws Exception {
    System.out.println("***********************************");
    System.out.println("       " + MethodHandles.lookup().lookupClass().getName() + " - importJobs " + keyword);
    System.out.println("***********************************");
    int seconds = 60 * (60 * hours);
    int count = queryMap.getOrDefault(keyword, 0);
    queryMap.remove(keyword);
    if (count > max) {
      queryMap.put(keyword, 0);
      return;
    }
    // System.out.println("Query start: " + start);
    String query = String.format(FORMAT_LINKEDIN_SEARCH_PARAMETERS, keyword, seconds, count);
    URI uri = new URI(PROTOCOL_HTTPS, HOST_LINKEDIN, PATH_LINKEDIN_JOBSEARCH, query, null);
    List<JobSearchJobListingEntity> results = search(uri);
    System.out.println("Found jobs: " + results.size());
    queryMap.put(keyword, count + results.size());
    for (JobSearchJobListingEntity job: results) {
      if (JobSummaryReader.isBadLocation(job.getLocation())) {
//      System.out.println("bad location: " + job.getLocation());
        continue;
      }
      Optional<JobSearchJobListingEntity> possibleExistingJob = jobSearchJobListingRepository.findByLinkedinid(job.getLinkedinid());
      if (possibleExistingJob.isPresent()) {
        JobSearchJobListingEntity existingJob = possibleExistingJob.get();
        List<String> keywords = existingJob.getKeywords();
        if (keywords == null) {
          keywords = new ArrayList<>();
        }
        if (!keywords.contains(keyword)) {
          keywords.add(keyword);
          jobSearchJobListingRepository.save(existingJob);
        }
        continue;
      }
      job.setKeywords(List.of(keyword));
      JobSearchStatusEntity statusEntity = jobSearchStatusRepository.findByName(JobSearchStatusEntity.JOB_SEARCH_STATUS_NEW).get();
      JobSearchTaskEntity taskEntity = new JobSearchTaskEntity();
      job.setTask(taskEntity);
      taskEntity.setStatus(statusEntity);
      taskEntity.setName(job.getName());
      jobSearchTaskRepository.save(taskEntity);

      jobSearchCompanyRepository.findByName(job.getCompanyName()).ifPresentOrElse(company -> {
        job.setCompany(company);
      }, () -> {
        JobSearchCompanyEntity companyEntity = new JobSearchCompanyEntity();
        companyEntity.setName(job.getCompanyName());
        companyEntity.setLabel(job.getCompanyName());
        companyEntity.setLocation(job.getLocation());
        job.setCompany(companyEntity);
        jobSearchCompanyRepository.save(companyEntity);
      });

      jobSearchJobListingRepository.save(job);      

//      saveJob(job).flatMap(newEntity -> {
//        return saveCompany(newEntity).map(smth -> saveTask(newEntity, smth));
//      });
//      System.out.println(job);
    }
  }

  private JobSearchJobListingEntity createJob(JobSearchCompanyEntity companyEntity) {
    JobSearchJobListingEntity entity = new JobSearchJobListingEntity();
    entity.setCompany(companyEntity);
    return jobSearchJobListingRepository.save(entity);
  }

  private JobSearchCompanyEntity createCompany(JobSearchTaskEntity taskEntity) {
    // TODO Auto-generated method stub
    return null;
  }

  private JobSearchTaskEntity createTask(JobSearchJobListingEntity job) {
    // TODO Auto-generated method stub
    return null;
  }

  private Object saveTask(JobSearchJobListingEntity job, JobSearchCompanyEntity company) {
    return jobSearchStatusRepository.findByName(JobSearchStatusEntity.JOB_SEARCH_STATUS_NEW).map(status -> {
      JobSearchTaskEntity task = new JobSearchTaskEntity();
//      task.setCompany(company);
      task.setJob(job);
      task.setName(job.getName());
      task.setStatus(status);
      return jobSearchTaskRepository.save(task);
    });
  }

  private Optional<JobSearchCompanyEntity> saveCompany(JobSearchJobListingEntity job) {
    System.out.println("Create company for job: " + job.getCompanyName());
    return jobSearchCompanyRepository.findByName(job.getCompanyName())
        .map(existingCompany -> {
      System.out.println("Company found in DB: " + existingCompany);
      return Optional.of(existingCompany);
        }).orElseGet(() -> {
          System.out.println("Company NOT found in DB");
          return jobSearchPhaseRepository.findByName(JobSearchPhaseEntity.JOB_SEARCH_PHASE_NEW).map(phase -> {
            JobSearchCompanyEntity company = new JobSearchCompanyEntity();
            company.setName(job.getCompanyName());
            company.setLocation(job.getLocation());
//            company.setPhase(phase);
            return jobSearchCompanyRepository.save(company);            
          });
        });
  }

  private Optional<JobSearchJobListingEntity> saveJob(JobSearchJobListingEntity job) {
    return jobSearchJobListingRepository.findByLinkedinid(job.getLinkedinid()).map(existingJob -> {
      System.out.println("Job found in DB: " + existingJob);
      return Optional.<JobSearchJobListingEntity>empty();
    }).orElseGet(() -> Optional.of(jobSearchJobListingRepository.save(job)));
  }

//  public void importJobs() {
//    // Load initial data (use a scheduler to avoid blocking the main thread)
//      Schedulers.boundedElastic().schedule(() -> {
//          try {
//              importFromLinkedIn(168, "engineer", 10000L, 1000);
//              importFromLinkedIn(168, "software", 10000L, 1000);
//              importFromLinkedIn(168, "developer", 10000L, 1000);
//              importFromLinkedIn(168, "fullstack", 10000L, 1000);
//              importFromLinkedIn(168, "backend", 10000L, 1000);
//          } catch (Exception e) {
//              System.err.println("Error loading initial data: " + e.getMessage());
//              e.printStackTrace();
//          }
//      });
//  }

//  public Optional<JobSearchCompanyEntity> createCompany(@Argument(name = "name") String name,
//      @Argument(name = "label") String label, @Argument(name = "location") String location) {
//    return jobSearchPhaseRepository.findByName(JobSearchPhaseEntity.JOB_SEARCH_PHASE_SEARCH).map(phaseEntity -> {
//      JobSearchCompanyEntity entity = new JobSearchCompanyEntity();
//      entity.setName(name);
//      entity.setLabel(label);
//      entity.setLocation(location);
//      entity.setPhase(phaseEntity);
//      return jobSearchCompanyRepository.save(entity);
//    });
//  }

//  public Optional<JobSearchTaskEntity> createTask(@Argument(name = "name") String name,
//      @Argument(name = "label") String label, @Argument(name = "location") String location,
//      @Argument(name = "jobId") Long jobId, @Argument(name = "companyId") Long companyId) {
//    return jobSearchJobListingRepository.findById(jobId) // Returns Optional<RelatedEntity1>
//        .flatMap(jobListingEntity -> jobSearchCompanyRepository.findById(companyId) // Returns Optional<RelatedEntity2>
//            .flatMap(companyEntity -> jobSearchStatusRepository
//                .findByName(JobSearchStatusEntity.JOB_SEARCH_STATUS_FOUND).map(statusEntity -> {
//                  JobSearchTaskEntity newEntity = new JobSearchTaskEntity();
//                  newEntity.setStatus(statusEntity);
//                  newEntity.setName(name);
//                  newEntity.setLabel(label);
//                  newEntity.setCompany(companyEntity);
//                  newEntity.setJob(jobListingEntity);
//                  return jobSearchTaskRepository.save(newEntity);
//                })));
//  }

//  public Optional<JobSearchJobListingEntity> saveJobListing(JobSearchJobListingEntity entity) {
//    return jobSearchJobListingRepository.findByLinkedinid(entity.getLinkedinid()).map(existingEntity -> {
//      existingEntity.setCompanyName(entity.getCompanyName());
//      existingEntity.setContracttype(entity.getContracttype());
//      existingEntity.setExperiencelevel(entity.getExperiencelevel());
//      existingEntity.setLabel(entity.getLabel());
//      existingEntity.setLocation(entity.getLocation());
//      existingEntity.setName(entity.getName());
//      existingEntity.setPublishedAt(entity.getPublishedAt());
//      existingEntity.setSalary(entity.getSalary());
//      existingEntity.setSector(entity.getSector());
//      existingEntity.setWorktype(entity.getWorktype());
//      return jobSearchJobListingRepository.save(existingEntity);
//    }).or(() -> Optional.of(jobSearchJobListingRepository.save(entity)));
//  }

//private final Sinks.Many<JobSearchTaskEntity> jobSearchTaskSink = Sinks.many().multicast().onBackpressureBuffer();

//private final Sinks.Many<JobSearchTaskEntity> jobSearchTaskSink = Sinks.many().multicast().onBackpressureBuffer();

  private final Sinks.Many<JobSearchTaskEntity> jobSearchTaskSink = Sinks.many().multicast()
      .onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

////@SubscriptionMapping(name = "importFromLinkedIn")
//  public void importFromLinkedIn(
//      @Argument(name = "hours") Integer hours,
//      @Argument(name = "keyword") String keyword,
//      @Argument(name = "sleep") Long sleep,
//      @Argument(name = "max") Integer max) throws Exception {
//
//    System.out.println("***********************************");
//    System.out.println("       " + MethodHandles.lookup().lookupClass().getName() + " - main " + keyword);
//    System.out.println("***********************************");
//
//    // Return the hot publisher immediately
////    Flux<JobSearchTaskEntity> resultFlux = jobSearchTaskSink.asFlux()
////        .publish()
////        .autoConnect();
//
//    // Process batches in the background
//    Mono.fromRunnable(() -> {
//      int seconds = 60 * (60 * hours);
//
//      for (int index = 0; index < 41; index++) {
//        int start = index * 25;
//        if (start >= max) {
//          System.out.println("Reached " + max + " items");
//          break;
//        }
//
//        try {
//          String query = String.format(FORMAT_LINKEDIN_SEARCH_PARAMETERS, keyword, seconds, start);
//          URI uri = new URI(PROTOCOL_HTTPS, HOST_LINKEDIN, PATH_LINKEDIN_JOBSEARCH, query, null);
//          Iterable<JobSearchJobListingEntity> results = search(uri);
//
//          // Process each job in the current batch
//          for (JobSearchJobListingEntity job : results) {
//            try {
////                      System.out.println("Found job: " + job);
//              Optional<JobSearchJobListingEntity> savedJob = saveJobListing(job);
//              if (savedJob.isPresent()) {
//                Optional<JobSearchCompanyEntity> company = createCompany("", "", "");
//                if (company.isPresent()) {
//                  Optional<JobSearchTaskEntity> task = createTask(job.getName(), job.getLabel(),
//                      company.get().getLocation(), job.getId(), company.get().getId());
//
//                  task.ifPresent(entity -> {
//                    System.out.println("publish task: " + entity);
//                    jobSearchTaskSink.tryEmitNext(entity);
//                  });
//                }
//              }
//            } catch (Exception e) {
//              System.err.println("Error processing job: " + job + ", error: " + e);
////                        e.printStackTrace();
//            }
//          }
//
//          // Sleep between batches
//          if (sleep > 0) {
//            Thread.sleep(sleep);
//          }
//
//        } catch (Exception e) {
//          System.err.println("Error processing batch: " + e);
//          e.printStackTrace();
//        }
//      }
//    }).subscribeOn(Schedulers.boundedElastic()).subscribe();
//
////    return resultFlux;
//  }

  private static List<JobSearchJobListingEntity> search(URI uri) throws Exception {
    List<JobSearchJobListingEntity> results = new ArrayList<>();
    URL url = HttpUtils.connect(uri);
    Document doc = Jsoup.parse(url, 5000);
    JobSummaryReader jobSummaryReader = new JobSummaryReader();
    for (JobSearchJobListingEntity job : jobSummaryReader.readDoc(doc)) {
//      System.out.println(job);
      results.add(job);
    }
//  for (JobSearchJobListingEntity job: results) {
//      jobReader.addDetails(job);
//  }
    return results;
  }

  private Map<String, Integer> queryMap = new HashMap<>();
}
