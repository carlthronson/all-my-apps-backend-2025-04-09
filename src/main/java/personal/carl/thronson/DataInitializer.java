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

    createPhase(JobSearchPhaseEntity.JOB_SEARCH_PHASE_SEARCH, "Search");
    createPhase(JobSearchPhaseEntity.JOB_SEARCH_PHASE_ACCEPTED, "Accepted");
    createPhase(JobSearchPhaseEntity.JOB_SEARCH_PHASE_CLOSED, "Closed");
    
    // Create Job Search Statuses
    createStatus(JobSearchStatusEntity.JOB_SEARCH_STATUS_FOUND, "Found", JobSearchPhaseEntity.JOB_SEARCH_PHASE_SEARCH);
    createStatus(JobSearchStatusEntity.JOB_SEARCH_STATUS_ACCEPTED, "Accepted", JobSearchPhaseEntity.JOB_SEARCH_PHASE_ACCEPTED);
    createStatus(JobSearchStatusEntity.JOB_SEARCH_STATUS_CLOSED, "Closed", JobSearchPhaseEntity.JOB_SEARCH_PHASE_CLOSED);
  }

  private void createStatus(String statusName, String statusLabel, String phaseName) {
    jobSearchStatusRepository.findByName(statusName).ifPresentOrElse(status -> {
    }, () -> {
      JobSearchStatusEntity statusEntity = new JobSearchStatusEntity();
      statusEntity.setName(statusName);
      statusEntity.setLabel(statusLabel);
      jobSearchPhaseRepository.findByName(phaseName).map(phaseEntity -> {
        System.out.println("Found start phase: " + phaseEntity);
        statusEntity.setPhase(phaseEntity);
        return phaseEntity;
      }).orElseThrow(() -> new RuntimeException("Database Initialization Error"));
      System.out.println("Save status: " + statusEntity);
      jobSearchStatusRepository.save(statusEntity);
    });
  }

  private void createPhase(String phaseName, String phaseLabel) {
    // Create Job Search Phases
    jobSearchPhaseRepository.findByName(phaseName).ifPresentOrElse(jobSearchPhaseEntity -> {
    }, () -> {
      JobSearchPhaseEntity searchPhase = new JobSearchPhaseEntity();
      searchPhase.setName(phaseName);
      searchPhase.setLabel(phaseLabel);
      jobSearchPhaseRepository.save(searchPhase);
    });
  }
}
