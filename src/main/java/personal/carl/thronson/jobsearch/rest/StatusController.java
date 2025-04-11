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

import personal.carl.thronson.jobsearch.data.entity.JobSearchStatusEntity;
import personal.carl.thronson.jobsearch.data.repo.JobSearchStatusRepository;

@RestController
@EnableWebMvc
@Transactional
public class StatusController {

    @Autowired
    JobSearchStatusRepository service;

    Logger logger = Logger.getLogger(JobController.class.getName());

    @RequestMapping(path = "/status/findbyid/{id}", method = RequestMethod.GET)
    public Optional<JobSearchStatusEntity> findById(@PathVariable("id") Long id) {
        logger.info("Path variable: " + id);
        return service.findById(id);
    }

    @RequestMapping(path = "/status/findallbyid/{id}", method = RequestMethod.GET)
    public List<JobSearchStatusEntity> findAllById(@PathVariable("id") Long id) {
        logger.info("Path variable: " + id);
        return service.findAllById(id);
    }

    @RequestMapping(path = "/status/findbyname/{name}", method = RequestMethod.GET)
    public Optional<JobSearchStatusEntity> findByName(@PathVariable("name") String name) {
        logger.info("Path variable: " + name);
        return service.findByName(name);
    }

    @RequestMapping(path = "/status/findall", method = RequestMethod.GET)
    public Page<JobSearchStatusEntity> findAll(
            @RequestParam("limit") Optional<Integer> limit,
            Principal principal) {
        logger.info("Request param: " + limit);
        int pageSize = limit.isPresent() ? limit.get() : 10;
        return service.findAll(PageRequest.of(0, pageSize));
    }

    @RequestMapping(path = "/status/count", method = RequestMethod.GET)
    public Long count() {
        return service.count();
    }
}
