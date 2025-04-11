package personal.carl.thronson.jobsearch.rest;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import personal.carl.thronson.jobsearch.data.entity.JobSearchCompanyEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchPhaseEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchStatusEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchTaskEntity;
import personal.carl.thronson.jobsearch.data.repo.JobSearchCompanyRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchPhaseRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchTaskRepository;

@RestController
@EnableWebMvc
@Transactional
public class CompanyController {

    @Autowired
    JobSearchCompanyRepository service;

    @Autowired
    JobSearchPhaseRepository phaseRepository;

    @Autowired
    JobSearchTaskRepository taskRepository;

    Logger logger = Logger.getLogger(JobController.class.getName());

    @RequestMapping(path = "/story/findbyid/{id}", method = RequestMethod.GET)
    public Optional<JobSearchCompanyEntity> findById(@PathVariable("id") Long id) {
        logger.info("Path variable: " + id);
        return service.findById(id);
    }

    @RequestMapping(path = "/story/findallbyid/{id}", method = RequestMethod.GET)
    public List<JobSearchCompanyEntity> findAllById(@PathVariable("id") Long id) {
        logger.info("Path variable: " + id);
        return service.findAllById(id);
    }

    @RequestMapping(path = "/story/findbyname/{name}", method = RequestMethod.GET)
    public Optional<JobSearchCompanyEntity> findByName(@PathVariable("name") String name) {
        logger.info("Path variable: " + name);
        return service.findByName(name);
    }

    @RequestMapping(path = "/story/findall", method = RequestMethod.GET)
    public Page<JobSearchCompanyEntity> findAll(
            @RequestParam("limit") Optional<Integer> limit,
            Principal principal) {
        logger.info("Request param: " + limit);
        int pageSize = limit.isPresent() ? limit.get() : 10;
        return service.findAll(PageRequest.of(0, pageSize));
    }

    @RequestMapping(path = "/story/count", method = RequestMethod.GET)
    public Long count() {
        return service.count();
    }

    @RequestMapping(path = "/story/phase/{phaseName}", method = RequestMethod.GET)
    public List<JobSearchCompanyEntity> findByPhase(
            @PathVariable("phaseName") String phaseName,
            @RequestParam("pageNumber") Optional<Integer> pageNumber,
            @RequestParam("pageSize") Optional<Integer> pageSize,
            Principal principal) {

        logger.info("Path variable phaseName: " + phaseName);
        logger.info("Request param pageNumber: " + pageNumber);
        logger.info("Request param pageSize: " + pageSize);

        Pageable pageable = PageRequest.of(
                pageNumber.isPresent() ? pageNumber.get() : 0,
                pageSize.isPresent() ? pageSize.get() : 1000);

        return findPageByPhase(phaseName, pageable);
    }

    public List<JobSearchCompanyEntity> findPageByPhase(String phaseName,
        Pageable pageable) {
    logger.info("findPageByPhase");
    JobSearchPhaseEntity phase = phaseRepository.findByName(phaseName).get();
//    logger.info("Phase entity: " + phase.getName());
    Page<JobSearchTaskEntity> taskPage = taskRepository
            .findAllByStatusIn(phase.getStatuses(), pageable);
//    logger.info("Tasks page: " + taskPage);
    Page<JobSearchCompanyEntity> storiesPage = service
            .findAllByTasksIn(taskPage.getContent(), pageable);
//    logger.info("Stories page: " + storiesPage);
    List<JobSearchCompanyEntity> list = new ArrayList<>();
    for (JobSearchCompanyEntity entity : storiesPage.getContent()) {
//        logger.info("Story entity: " + entity.getName());
        JobSearchCompanyEntity storyEntity = new JobSearchCompanyEntity();
        storyEntity.setId(entity.getId());
        storyEntity.setName(entity.getName());
        storyEntity.setLabel(entity.getLabel());
        storyEntity.setLocation(entity.getLocation());
        List<JobSearchTaskEntity> originalList = entity.getTasks();
        System.out.println("Original tasks: " + originalList.size());
        List<JobSearchTaskEntity> newList = originalList.stream()
                .filter(task -> filterTask(task, phaseName)).toList();
        System.out.println("New tasks: " + newList.size());
        storyEntity.setTasks(newList);
        storyEntity.setPhase(entity.getPhase());
        list.add(storyEntity);
    }
    list = list.stream().sorted(new Comparator<JobSearchCompanyEntity>() {

        // Reverse order, most recent first
        @Override
        public int compare(JobSearchCompanyEntity o1, JobSearchCompanyEntity o2) {
            JobSearchJobListingEntity job1 = sortTasks(o1);
            JobSearchJobListingEntity job2 = sortTasks(o2);
            return job2.getPublishedAt().compareTo(job1.getPublishedAt());
        }

        // Also in reverse order, most recent first
        private JobSearchJobListingEntity sortTasks(JobSearchCompanyEntity o1) {
            List<JobSearchTaskEntity> l1 = o1.getTasks().stream()
                    .sorted(new Comparator<JobSearchTaskEntity>() {

                        @Override
                        public int compare(JobSearchTaskEntity o1, JobSearchTaskEntity o2) {
                            return o1.getJob().getPublishedAt().compareTo(
                                    o2.getJob().getPublishedAt());
                        }
                    }).toList();
            o1.setTasks(l1);
            return o1.getTasks().get(0).getJob();
        }
    }).toList();
    return list;
}

    private Boolean filterTask(JobSearchTaskEntity task, String phaseName) {
      System.out.println("Filter task - task name: " + task.getName());
      System.out.println("Filter task - target phase name: " + phaseName);
      JobSearchStatusEntity statusEntity = task.getStatus();
      System.out.println(
              "Filter task - status name: " + statusEntity.getName());
      JobSearchPhaseEntity phaseEntity = statusEntity.getPhase();
      System.out.println("Filter task - status phase name: " + phaseName);
      boolean result = phaseEntity.getName().compareTo(phaseName) == 0;
      System.out.println("Filter task - result: " + result);
      return result;
  }
}
