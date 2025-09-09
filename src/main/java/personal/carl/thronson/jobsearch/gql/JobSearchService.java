package personal.carl.thronson.jobsearch.gql;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import personal.carl.thronson.ai.svc.JobAIAnalysisCallback;
import personal.carl.thronson.ai.svc.JobVectorService;
import personal.carl.thronson.http.JobSummaryReader;
import personal.carl.thronson.jobsearch.data.entity.JobSearchCompanyEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobAnalysisEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobDescriptionEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchPhaseEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchStatusEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchTaskEntity;
import personal.carl.thronson.jobsearch.data.repo.JobSearchCompanyRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchJobAnalysisRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchJobDescriptionRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchJobListingRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchPhaseRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchStatusRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchTaskRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

@Service
@Transactional
public class JobSearchService {

  private static final int MAX_JOB_SEARCH_RESULTS = 1000;

  private static final int PAGE_SIZE = 10;

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
  private JobVectorService jobVectorService;

  @Autowired
  private JobSearchJobAnalysisRepository jobSearchJobAnalysisRepository;

  @Autowired
  private OkHttpClient okHttpClient;

  // TODO
  // Find all where there is a description and there is no description vector

  @Scheduled(fixedRate = 60 * 60 * 1000) // Executes every 60 minutes
  public void createJobTitleVectors() {
    System.out.println("***********************************");
    System.out.println("Scheduled task: " + MethodHandles.lookup().lookupClass().getName() + " - createJobTitleVectors ");
    System.out.println("***********************************");
    AtomicLong actualCount = new AtomicLong(0);
    AtomicLong errorCount = new AtomicLong(0);
    int pageSize = PAGE_SIZE;

    // Fetch first page synchronously to get total pages
    PageRequest firstPageRequest = PageRequest.of(0, pageSize);
    Page<JobSearchJobListingEntity> firstPage = jobSearchJobListingRepository.findAllByHasTitleVectorFalse(firstPageRequest);
    int totalPages = firstPage.getTotalPages();
    System.out.println("Total pages: " + totalPages);
    System.out.println("Total listings to process: " + firstPage.getTotalElements());

    Flux.range(0, totalPages) // Emit page numbers
      .concatMap(pageNumber -> {
        logger.info("createJobTitleVectors page number: " + pageNumber);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        // Wrap blocking call in Mono.fromCallable and offload to boundedElastic scheduler
        return Mono.fromCallable(() -> jobSearchJobListingRepository.findAllByHasTitleVectorFalse(pageRequest))
          .subscribeOn(Schedulers.boundedElastic())
          .doOnNext(page -> actualCount.addAndGet(page.getNumberOfElements()))
          .onErrorResume(e -> {
            errorCount.incrementAndGet();
            logger.log(Level.WARNING, "Error fetching page: " + pageNumber, e);
            // Return empty page to continue with next pages
            return Mono.just(Page.empty());
          })
          .flatMapMany(page -> {
            logger.info("One page of content: " + page.getContent().size());
            return Flux.fromIterable(page.getContent());
          });
      })
      .concatMap(jobListing ->
        // Combine vector creation and save into one async chain
        Mono.fromCallable(() -> {
          jobVectorService.addJobDescription(jobListing);
          return jobListing;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(updatedJobListing -> {
          updatedJobListing.setHasTitleVector(true);
          return Mono.fromCallable(() -> jobSearchJobListingRepository.save(updatedJobListing))
                     .subscribeOn(Schedulers.boundedElastic());
        })
        .onErrorResume(e -> {
          errorCount.incrementAndGet();
          logger.log(Level.WARNING, "Error processing job listing " + jobListing.getLinkedinurl(), e);
          return Mono.empty(); // skip this listing and continue processing others
        })
      )
      .doOnError(error -> {
        logger.log(Level.SEVERE, "Error occurred during processing", error);
      })
      .subscribe(result -> {
        // no-op onNext
      }, error -> {
        logger.log(Level.WARNING, "Error in scheduled vector creation", error);
      }, () -> {
        logger.info("Total listings processed: " + actualCount.get());
        logger.info("Total errors encountered: " + errorCount.get());
      });
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importEngineerJobs() {
    experimental("engineer", 168, MAX_JOB_SEARCH_RESULTS);
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importSoftwareJobs() {
    experimental("software", 168, MAX_JOB_SEARCH_RESULTS);
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importDeveloperJobs() {
    experimental("developer", 168, MAX_JOB_SEARCH_RESULTS);
  }

  @Scheduled(fixedRate = 15000 * 60) // Executes every 15 minutes
  public void importFullstackJobs() {
    experimental("fullstack", 168, MAX_JOB_SEARCH_RESULTS);
  }

  @Scheduled(fixedRate = 15 * 60 * 1000) // Executes every 15 minutes
  public void importBackendJobs() {
    experimental("backend", 168, MAX_JOB_SEARCH_RESULTS);
  }

  JobSummaryReader jobSummaryReader = new JobSummaryReader();

  private void experimental(String keyword, int hours, int max) {
    System.out.println("***********************************");
    System.out.println("Scheduled task: " + MethodHandles.lookup().lookupClass().getName() + " - importJobs " + keyword);
    System.out.println("***********************************");
    getUrl(keyword, hours, max)
    .flatMap(url -> {
      WebClient webClient = WebClient.create(); // No base URL
      return webClient.get()
          .uri(url.toString())
          .retrieve()
          .bodyToMono(String.class);
    })
    .doOnNext(html -> System.out.println("Recieved html for job listings"))
    .map(Jsoup::parse)
    .doOnNext(doc -> System.out.println("Parsed html"))
    .map(jobSummaryReader::readDoc)
    .doOnNext(list -> System.out.println("Found jobs: " + list.size()))
    .subscribe(jobs -> {
      this.importJobs(keyword, hours, max, jobs);
    }, this::handleError);
  }

//  @Scheduled(fixedRate = 15 * 60 * 1000) // Executes every 15 minutes
//  public void importJobDescriptions() {
//    importJobDescriptionPipeline();
//  }

  private void importJobDescriptionPipeline() {
    System.out.println("***********************************");
    System.out.println("Scheduled task: " + MethodHandles.lookup().lookupClass().getName() + " - importJobDescriptions ");
    System.out.println("***********************************");
    AtomicLong actualCount = new AtomicLong(0);
    AtomicLong errorCount = new AtomicLong(0);
    int pageSize = PAGE_SIZE;

    // Fetch first page synchronously to get total pages
    PageRequest firstPageRequest = PageRequest.of(0, pageSize);
    Page<JobSearchJobListingEntity> firstPage = jobSearchJobListingRepository.findAllWhereDescriptionIsNull(firstPageRequest);
    int totalPages = firstPage.getTotalPages();
    System.out.println("Total pages: " + totalPages);
    System.out.println("Total listings to process: " + firstPage.getTotalElements());

    Flux.range(0, totalPages) // Emit page numbers
      .concatMap(pageNumber -> {
        logger.info("Import job descriptions page number: " + pageNumber);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        // Wrap blocking call in Mono.fromCallable and offload to boundedElastic scheduler
        return Mono.fromCallable(() -> jobSearchJobListingRepository.findAllWhereDescriptionIsNull(pageRequest))
          .subscribeOn(Schedulers.boundedElastic())
          .doOnNext(page -> actualCount.addAndGet(page.getNumberOfElements()))
          .onErrorResume(e -> {
            errorCount.incrementAndGet();
            logger.log(Level.WARNING, "Error fetching page: " + pageNumber, e);
            // Return empty page to continue with next pages
            return Mono.just(Page.empty());
          })
          .flatMapMany(page -> {
            logger.info("One page of content: " + page.getContent().size());
            return Flux.fromIterable(page.getContent());
          });
      })
      .concatMap(jobListing -> {
        // Process entities sequentially
        return getDescription(jobListing)
          .onErrorResume(e -> {
            errorCount.incrementAndGet();
            logger.log(Level.WARNING, "Error getting description for listing " + jobListing.getLinkedinurl(), e);
            return Mono.empty(); // Skip this listing, continue with others
          });
      })
      .concatMap(jobListing -> {
        // persist updated entity asynchronously
        return saveListingAndDescription(jobListing)
          .onErrorResume(e -> {
            errorCount.incrementAndGet();
            if (!(e instanceof ObjectOptimisticLockingFailureException))
            logger.log(Level.WARNING, "Error saving job metadata for listing " + jobListing.getLinkedinurl(), e);
            return Mono.empty(); // Skip saving this item, continue with others
          });
      })
      .doOnError(error -> {
        logger.log(Level.SEVERE, "Error occurred during processing", error);
      })
      .subscribe(result -> {
      }, error -> {
        logger.log(Level.WARNING, "Error", error);
      }, () -> {
        logger.info("Total listings processed: " + actualCount.get());
        logger.info("Total errors encountered: " + errorCount.get());
      });
  }

  private Mono<JobSearchJobListingEntity> getDescription(JobSearchJobListingEntity job) {
    WebClient webClient = WebClient.builder()
      .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16 MB buffer
      .build();
    return webClient.get()
      .uri(job.getLinkedinurl())
      .retrieve()
      .bodyToMono(String.class)
//      .doOnSubscribe(sub -> System.out.println("Fetching description for job: " + job.getLinkedinurl()))
//      .doOnNext(body -> System.out.println("Received response for job: " + job.getLinkedinurl()))
//      .doOnError(error -> logger.log(Level.WARNING, "Error fetching job description for " + job.getLinkedinurl(), error))
      .map(Jsoup::parse)
      .map(doc -> parseDescription(doc, job))
//      .doOnSuccess(metaData -> System.out.println("Parsed description successfully for job: " + job.getLinkedinurl()))
      .doOnError(error -> logger.log(Level.WARNING, "Error parsing job description for " + job.getLinkedinurl(), error));
  }

  private Mono<JobSearchJobListingEntity> saveListingAndDescription(JobSearchJobListingEntity listing) {
    JobSearchJobDescriptionEntity description = listing.getDescription();

    if (description == null) {
      return Mono.just(listing);
    }

    // Save description first
    return Mono.fromCallable(() -> jobSearchJobDescriptionRepository.save(description))
      .subscribeOn(Schedulers.boundedElastic()).flatMap(savedDescription -> {
        // Set the saved description back to listing
        listing.setDescription(savedDescription);
        // Save listing next
        return Mono.fromCallable(() -> jobSearchJobListingRepository.save(listing))
          .subscribeOn(Schedulers.boundedElastic());
      });
  }

  private JobSearchJobListingEntity parseDescription(Document doc, JobSearchJobListingEntity jobListing) {
    JobSearchJobDescriptionEntity descriptionEntity = new JobSearchJobDescriptionEntity();
    descriptionEntity.setListing(jobListing);
    jobListing.setDescription(descriptionEntity);

    Elements jobCriteriaItems = doc.getElementsByClass("description__job-criteria-item");
    jobCriteriaItems.forEach(jobCriteriaItem -> {
      Elements subheaders = jobCriteriaItem.getElementsByClass("description__job-criteria-subheader");
      subheaders.forEach(subheader -> {
        if (subheader.text().contains("Employment type")) {
          Elements subText = jobCriteriaItem.getElementsByClass("description__job-criteria-text");
          String employmentType = subText.text();
          descriptionEntity.setEmploymentType(employmentType);
        }
      });
    });
    Elements hiddenElements = doc.getElementsByClass("show-more-less-html__markup");
    String description = hiddenElements.text();
    if (description != null && description.trim().length() > 0) {
      String cleanedText = cleanText(description);
      descriptionEntity.setDescription(cleanedText);
    }
    return jobListing;
  }

  public static String cleanText(String description) {
    String cleanedText =  description.replaceAll("\\s+", " ").trim();
    cleanedText = cleanedText.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    cleanedText = Normalizer.normalize(cleanedText, Form.NFC);
    cleanedText = Parser.unescapeEntities(cleanedText, false);
    return cleanedText;
  }

  private void handleError(Throwable error) {
//    logger.log(Level.SEVERE, "Pipeline finished with error", error);
    System.out.println("Severe error: pipeline finished with error: " + error);
//    error.printStackTrace();
  }

  @Scheduled(fixedRate = 15 * 60 * 1000) // Executes every 15 minutes
  public void analyzeJobDescriptions() {
    System.out.println("***********************************");
    System.out.println("Scheduled task: " + MethodHandles.lookup().lookupClass().getName() + " - importJobDescriptions ");
    System.out.println("***********************************");
    AtomicLong actualCount = new AtomicLong(0);
    AtomicLong errorCount = new AtomicLong(0);
    int pageSize = PAGE_SIZE;

    // Fetch first page synchronously to get total pages
    PageRequest firstPageRequest = PageRequest.of(0, pageSize);
    Page<JobSearchJobDescriptionEntity> firstPage = jobSearchJobDescriptionRepository.findAllWhereAnalysisIsNull(firstPageRequest);
    int totalPages = firstPage.getTotalPages();
    System.out.println("Total pages of descriptions: " + totalPages);
    System.out.println("Total listings of descriptions to process: " + firstPage.getTotalElements());

    Flux.range(0, totalPages) // Emit page numbers
    .concatMap(pageNumber -> {
      logger.info("Analyze job descriptions page number: " + pageNumber);
      PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
      // Wrap blocking call in Mono.fromCallable and offload to boundedElastic scheduler
      return Mono.fromCallable(() -> jobSearchJobDescriptionRepository.findAllWhereAnalysisIsNull(pageRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .doOnNext(page -> actualCount.addAndGet(page.getNumberOfElements()))
        .onErrorResume(e -> {
          errorCount.incrementAndGet();
          logger.log(Level.WARNING, "Error fetching page: " + pageNumber, e);
          // Return empty page to continue with next pages
          return Mono.just(Page.empty());
        })
        .flatMapMany(page -> {
          logger.info("One page of content: " + page.getContent().size());
          return Flux.fromIterable(page.getContent());
        });
    })
    .concatMap(jobDescription -> {
      // Process entities sequentially
      return analyze(jobDescription)
        .onErrorResume(e -> {
          errorCount.incrementAndGet();
          logger.log(Level.WARNING, "Error analyzing description for listing " + jobDescription.getListing().getLinkedinurl(), e);
          return Mono.empty(); // Skip this listing, continue with others
        });
    })
    .concatMap(jobDescription -> {
      // persist updated entity asynchronously
      return saveDescriptionAndAnalysis(jobDescription)
        .onErrorResume(e -> {
          errorCount.incrementAndGet();
          logger.log(Level.WARNING, "Error saving job metadata for listing " + jobDescription.getListing().getLinkedinurl(), e);
          return Mono.empty(); // Skip saving this item, continue with others
        });
    })
    .doOnError(error -> {
      logger.log(Level.SEVERE, "Error occurred during processing", error);
    })
    .subscribe(result -> {
    }, error -> {
      logger.log(Level.WARNING, "Error", error);
    }, () -> {
      logger.info("Total descriptions processed: " + actualCount.get());
      logger.info("Total errors encountered: " + errorCount.get());
    });
  }

  private Mono<JobSearchJobDescriptionEntity> saveDescriptionAndAnalysis(JobSearchJobDescriptionEntity jobDescription) {
    JobSearchJobAnalysisEntity analysis = jobDescription.getAnalysis();

    if (analysis == null) {
      return Mono.just(jobDescription);
    }

    // Save description first
    return Mono.fromCallable(() -> jobSearchJobAnalysisRepository.save(analysis))
      .subscribeOn(Schedulers.boundedElastic()).flatMap(savedAnalysis -> {
        // Set the saved description back to listing
        jobDescription.setAnalysis(savedAnalysis);
        // Save listing next
        return Mono.fromCallable(() -> jobSearchJobDescriptionRepository.save(jobDescription))
          .subscribeOn(Schedulers.boundedElastic());
      });
  }

  private Mono<JobSearchJobDescriptionEntity> analyze(JobSearchJobDescriptionEntity entity) {
    return Mono.<JobSearchJobDescriptionEntity>create(sink -> {
      Request aiRequest = buildAIRequest(entity);
      okHttpClient.newCall(aiRequest).enqueue(new JobAIAnalysisCallback(sink, entity));
    })
    // Step 3: If AI analysis fails, emit job with description only
    .onErrorResume(e -> {
      System.out.println("Error during AI analysis for job: " + entity);
      System.out.println("Error during AI analysis for job: " + e);
      return Mono.just(entity);
    });
//    return Mono.just(entity);
  }

  //Given a list of tokens, create a bag-of-words vector
  public double[] createBagOfWordsVector(List<String> tokens, Set<String> vocabulary) {
     double[] vector = new double[vocabulary.size()];
     int i = 0;
     for (String word : vocabulary) {
         vector[i++] = Collections.frequency(tokens, word);
     }
     return vector;
  }

  private Request buildAIRequest(JobSearchJobDescriptionEntity job) {
    String description = job.getDescription();

    String prompt = String.format(
        "The entire text of your response must be valid JSON that I can parse without any other pre processing.  Extract the title, location, required skills, and responsibilities from this job description: %s Return the result as JSON.Return only the extracted information as a valid JSON object. Do not include any explanation, comments, or extraneous formatting besides making valid JSON. Label the json fields as either title, location, skills or responsibilties.  Do not inject any creativity in your reponse.",
        cleanText(description)
    );

    String json = """
    {
      "model": "llama3",
      "messages": [
        {"role": "user", "content": "%s"}
      ],
      "stream": false,
      "temperature": 0.0
    }
    """.formatted(prompt);

    return new Request.Builder()
        .url("http://localhost:11434/api/chat")
        .addHeader("Content-Type", "application/json")
        .post(RequestBody.create(json, MediaType.parse("application/json; charset=utf-8")))
        .build();
  }

  public Mono<URL> getUrl(String keyword, int hours, int max) {
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

  int savedListings = 0;
  int savedDescriptions = 0;

  private void importJobs(String keyword, int hours, int max, List<JobSearchJobListingEntity> jobs) {
//    int seconds = 60 * (60 * hours);
    int count = queryMap.getOrDefault(keyword, 0);
    queryMap.remove(keyword);
    if (count > max) {
      queryMap.put(keyword, 0);
      System.out.println("Reset counter for keyword and abort");
      return;
    }
//    System.out.println("Found jobs: " + jobs.size());
    queryMap.put(keyword, count + jobs.size());
    for (JobSearchJobListingEntity job: jobs) {
//      System.out.println("Save job: " + job);
//      if (JobSummaryReader.isBadLocation(job.getLocation())) {
////        System.out.println("bad location: " + job.getLocation());
//        continue;
//      }
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
//        System.out.println("Skip further processing for existing job");
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
