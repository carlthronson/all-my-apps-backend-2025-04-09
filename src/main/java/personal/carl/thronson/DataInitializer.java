package personal.carl.thronson;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import personal.carl.thronson.jobsearch.data.entity.JobSearchPhaseEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchStatusEntity;
import personal.carl.thronson.jobsearch.data.repo.JobSearchPhaseRepository;
import personal.carl.thronson.jobsearch.data.repo.JobSearchStatusRepository;
import personal.carl.thronson.security.data.core.Role;
import personal.carl.thronson.security.data.entity.AccountEntity;
import personal.carl.thronson.security.data.entity.RoleEntity;
import personal.carl.thronson.security.data.repo.AccountRepository;
import personal.carl.thronson.security.data.repo.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private JobSearchPhaseRepository jobSearchPhaseRepository;

  @Autowired
  private JobSearchStatusRepository jobSearchStatusRepository;

  @Override
  public void run(String... args) throws Exception {
    // Create Admin Role
    roleRepository.findByName(Role.ADMIN_ROLE_NAME).ifPresentOrElse(roleEntity -> {
    }, () -> {
      RoleEntity admin = new RoleEntity();
      admin.setName(Role.ADMIN_ROLE_NAME);
      admin.setLabel(Role.ADMIN_ROLE_LABEL);
      roleRepository.save(admin);
    });

    // Create my account
    accountRepository.findByEmail("carlthronson@gmail.com").ifPresentOrElse(roleEntity -> {
    }, () -> {
      AccountEntity carl = new AccountEntity();
      carl.setEmail("carlthronson@gmail.com");
      roleRepository.findByName(Role.ADMIN_ROLE_NAME).map(roleEntity -> {
        carl.setRoles(Set.of(roleEntity));
        return roleEntity;
      }).orElseThrow(() -> new RuntimeException("Database Initialization Error"));
      accountRepository.save(carl);
    });

    // Create Job Search Phases
    jobSearchPhaseRepository.findByName(JobSearchPhaseEntity.JOB_SEARCH_PHASE_SEARCH).ifPresentOrElse(jobSearchPhaseEntity -> {
    }, () -> {
      JobSearchPhaseEntity searchPhase = new JobSearchPhaseEntity();
      searchPhase.setName(JobSearchPhaseEntity.JOB_SEARCH_PHASE_SEARCH);
      searchPhase.setLabel("Search");
      jobSearchPhaseRepository.save(searchPhase);
    });

    // Create Job Search Phases
    jobSearchPhaseRepository.findByName(JobSearchPhaseEntity.JOB_SEARCH_PHASE_APPLY).ifPresentOrElse(jobSearchPhaseEntity -> {
    }, () -> {
      JobSearchPhaseEntity searchPhase = new JobSearchPhaseEntity();
      searchPhase.setName(JobSearchPhaseEntity.JOB_SEARCH_PHASE_APPLY);
      searchPhase.setLabel("Apply");
      jobSearchPhaseRepository.save(searchPhase);
    });
    
    // Create Job Search Statuses
    jobSearchStatusRepository.findByName(JobSearchStatusEntity.JOB_SEARCH_STATUS_FOUND).ifPresentOrElse(status -> {
    }, () -> {
      JobSearchStatusEntity foundStatus = new JobSearchStatusEntity();
      foundStatus.setName(JobSearchStatusEntity.JOB_SEARCH_STATUS_FOUND);
      foundStatus.setLabel("Found");
      jobSearchPhaseRepository.findByName(JobSearchPhaseEntity.JOB_SEARCH_PHASE_SEARCH).map(phaseEntity -> {
        System.out.println("Found start phase: " + phaseEntity);
        foundStatus.setPhase(phaseEntity);
        return phaseEntity;
      }).orElseThrow(() -> new RuntimeException("Database Initialization Error"));
      System.out.println("Save status: " + foundStatus);
      jobSearchStatusRepository.save(foundStatus);
    });

    // Create Job Search Statuses
    jobSearchStatusRepository.findByName(JobSearchStatusEntity.JOB_SEARCH_STATUS_APPLY).ifPresentOrElse(status -> {
    }, () -> {
      JobSearchStatusEntity applyStatus = new JobSearchStatusEntity();
      applyStatus.setName(JobSearchStatusEntity.JOB_SEARCH_STATUS_APPLY);
      applyStatus.setLabel("Apply");
      jobSearchPhaseRepository.findByName(JobSearchPhaseEntity.JOB_SEARCH_PHASE_APPLY).map(phaseEntity -> {
        System.out.println("Found start phase: " + phaseEntity);
        applyStatus.setPhase(phaseEntity);
        return phaseEntity;
      }).orElseThrow(() -> new RuntimeException("Database Initialization Error"));
      System.out.println("Save status: " + applyStatus);
      jobSearchStatusRepository.save(applyStatus);
    });
  }
}
