package personal.carl.thronson.jobsearch.gql;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import personal.carl.thronson.http.JobSummaryReader;
import personal.carl.thronson.jobsearch.data.entity.JobSearchCompanyEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobDescriptionEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchPhaseEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchStatusEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchTaskEntity;
import personal.carl.thronson.jobsearch.data.repo.JobSearchCompanyRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchJobDescriptionRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchJobListingRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchPhaseRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchStatusRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchTaskRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@Service
@Transactional
public class JobSearchService {

  private static final int MAX_JOB_SEARCH_RESULTS = 1000;

  private static String FORMAT_LINKEDIN_SEARCH_PARAMETERS = "keywords=%s&f_TPR=r%d&origin=JOBS_HOME_SEARCH_BUTTON&refresh=true&start=%d";

  private static String PROTOCOL_HTTPS = "https";
  private static String HOST_LINKEDIN = "www.linkedin.com";
  private static String PATH_LINKEDIN_JOBSEARCH = "/jobs-guest/jobs/api/seeMoreJobPostings/search";

  Logger logger = Logger.getLogger(getClass().getName());
  ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

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

  @Autowired
  private JobSearchJobDescriptionRepository jobSearchJobDescriptionRepository;

  @Autowired
  private OkHttpClient okHttpClient;

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importEngineerJobs() {
    experimental("engineer", 168, MAX_JOB_SEARCH_RESULTS);
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importSoftwareJobs() {
//    experimental("software", 168, MAX_JOB_SEARCH_RESULTS);
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importDeveloperJobs() {
//    experimental("developer", 168, MAX_JOB_SEARCH_RESULTS);
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importFullstackJobs() {
//    experimental("fullstack", 168, MAX_JOB_SEARCH_RESULTS);
  }

  @Scheduled(fixedRate = 15 * 60 * 1000) // Executes every 15 minutes
  public void importBackendJobs() {
//    experimental("backend", 168, MAX_JOB_SEARCH_RESULTS);
  }

//  private URL buildUrl(String keyword, int hours, int max) {
//    return null;
//  }

  private void experimental(String keyword, int hours, int max) {
    newFunction(keyword, hours, max);
  }

  public Flux<JobSearchJobListingEntity> scrapeJobs(String keyword, int hours, int max) {
    // Step 1. format url
    return getUrl(keyword, hours, max)
    // Step 2. map to list of job meta data
    .flatMap(url -> {
//      System.out.println("flatMap URL: " + url);
      return Mono.<List<JobSearchJobListingEntity>>create(sink -> {
        Request httpRequest = new Request.Builder().url(url).get().build();
        okHttpClient.newCall(httpRequest).enqueue(new JobSearchListCallback(sink));
      });
    })
    // Step 3. map to flux of individual job meta data
    .flatMapMany(Flux::fromIterable);
  }

  public Mono<List<JobSearchJobListingEntity>> pipeline (Flux<JobSearchJobListingEntity> flux) {
    return flux
        .flatMap(smth -> {
          return Mono.<JobSearchJobListingEntity>create(sink -> {
            Request httpRequest = buildJobDescriptionRequest(smth);
            okHttpClient.newCall(httpRequest).enqueue(
              new JobSearchDescriptionCallback(sink, smth));
          })
              .onErrorReturn(smth)
              ;
        })
        .collectList();
  }

  private Request buildJobDescriptionRequest(JobSearchJobListingEntity job) {
    return new Request.Builder().url(job.getLinkedinurl()).get().build();
  }

  private void newFunction(String keyword, int hours, int max) {
    Flux<JobSearchJobListingEntity> source = scrapeJobs(keyword, hours, max);
    pipeline(source)
      .subscribe(
      jobs -> {
    //      System.out.println("Imported jobs: " + jobs.size());
          importJobs(keyword, hours, max, jobs);
      },
      error -> {
    //    System.err.println("Error importing jobs: " + error);
        }
    );
  }

  public Mono<URL> getUrl(String keyword, int hours, int max) {
      System.out.println("***********************************");
      System.out.println("       " + MethodHandles.lookup().lookupClass().getName() + " - importJobs " + keyword);
      System.out.println("***********************************");
      int seconds = 60 * (60 * hours);
      int count = queryMap.getOrDefault(keyword, 0);
      queryMap.remove(keyword);
      if (count > max) {
        queryMap.put(keyword, 0);
        return Mono.empty();
      }
      String query = String.format(FORMAT_LINKEDIN_SEARCH_PARAMETERS, keyword, seconds, count);
      try {
        URI uri = new URI(PROTOCOL_HTTPS, HOST_LINKEDIN, PATH_LINKEDIN_JOBSEARCH, query, null);
        URL url = uri.toURL();
        return Mono.just(url);
      } catch (URISyntaxException | MalformedURLException e) {
        return Mono.error(e);
      }
  }

  private void importJobs(String keyword, int hours, int max, List<JobSearchJobListingEntity> results) {
    int seconds = 60 * (60 * hours);
    int count = queryMap.getOrDefault(keyword, 0);
    queryMap.remove(keyword);
    if (count > max) {
      queryMap.put(keyword, 0);
      return;
    }
    System.out.println("Found jobs: " + results.size());
    queryMap.put(keyword, count + results.size());
    for (JobSearchJobListingEntity job: results) {
      if (JobSummaryReader.isBadLocation(job.getLocation())) {
//        System.out.println("bad location: " + job.getLocation());
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

  private Map<String, Integer> queryMap = new HashMap<>();

}
