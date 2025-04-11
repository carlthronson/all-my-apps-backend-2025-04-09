package personal.carl.thronson.jobsearch.rest;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import personal.carl.thronson.jobsearch.data.entity.JobSearchPhaseEntity;
import personal.carl.thronson.jobsearch.data.repo.JobSearchPhaseRepository;

@RestController
@EnableWebMvc
@Transactional
public class PhaseController {

    @Autowired
    JobSearchPhaseRepository service;

    Logger logger = Logger.getLogger(JobController.class.getName());

    @RequestMapping(path = "/phase/findbyid/{id}", method = RequestMethod.GET)
    public Optional<JobSearchPhaseEntity> findById(@PathVariable("id") Long id) {
        logger.info("Path variable: " + id);
        return service.findById(id);
    }

    @RequestMapping(path = "/phase/findallbyid/{id}", method = RequestMethod.GET)
    public List<JobSearchPhaseEntity> findAllById(@PathVariable("id") Long id) {
        logger.info("Path variable: " + id);
        return service.findAllById(id);
    }

    @RequestMapping(path = "/phase/findbyname/{name}", method = RequestMethod.GET)
    public Optional<JobSearchPhaseEntity> findByName(@PathVariable("name") String name) {
        logger.info("Path variable: " + name);
        return service.findByName(name);
    }

    @RequestMapping(path = "/phase/findall", method = RequestMethod.GET)
    public Page<JobSearchPhaseEntity> findAll(
            @RequestParam("limit") Optional<Integer> limit,
            Principal principal) {
        logger.info("Request param: " + limit);
        int pageSize = limit.isPresent() ? limit.get() : 10;
        return service.findAll(PageRequest.of(0, pageSize));
    }

    @RequestMapping(path = "/phase/count", method = RequestMethod.GET)
    public Long count() {
        return service.count();
    }
}
