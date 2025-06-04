package personal.carl.thronson;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import personal.carl.thronson.budget.data.entity.TransactionEntity;
import personal.carl.thronson.budget.data.repo.TransactionRepository;
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
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JobSearchPhaseRepository jobSearchPhaseRepository;

  @Autowired
  private JobSearchStatusRepository jobSearchStatusRepository;

  @Autowired
  private TransactionRepository transactionRepository;

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
    AccountEntity carl = createAccount("carlthronson@gmail.com", "8Xbk2334$&@");

    // Create Krupa's account
    AccountEntity krupa = createAccount("krupa52012@gmail.com", "2wta018");

    if (carl.getDelegates().isEmpty()) {
      carl.setDelegates(List.of(krupa));
      accountRepository.save(carl);
    }

    createPhase(JobSearchPhaseEntity.JOB_SEARCH_PHASE_NEW, "New");
    createPhase(JobSearchPhaseEntity.JOB_SEARCH_PHASE_MAYBE, "Maybe");
    createPhase(JobSearchPhaseEntity.JOB_SEARCH_PHASE_MATCH, "Match");
    createPhase(JobSearchPhaseEntity.JOB_SEARCH_PHASE_CLOSED, "Closed");

    // Create Job Search Statuses
    createStatus(JobSearchStatusEntity.JOB_SEARCH_STATUS_NEW, "New", JobSearchPhaseEntity.JOB_SEARCH_PHASE_NEW);
    createStatus(JobSearchStatusEntity.JOB_SEARCH_STATUS_MAYBE, "Maybe", JobSearchPhaseEntity.JOB_SEARCH_PHASE_MAYBE);
    createStatus(JobSearchStatusEntity.JOB_SEARCH_STATUS_MATCH, "Match", JobSearchPhaseEntity.JOB_SEARCH_PHASE_MATCH);
    createStatus(JobSearchStatusEntity.JOB_SEARCH_STATUS_CLOSED, "Closed",
        JobSearchPhaseEntity.JOB_SEARCH_PHASE_CLOSED);

    initTransactions();
  }

  private AccountEntity createAccount(String email, String password) {
    return accountRepository.findByEmail(email).orElseGet(() -> {
      AccountEntity carl = new AccountEntity();
      carl.setEmail(email);
      carl.setPassword(passwordEncoder.encode(password));
      roleRepository.findByName(Role.ADMIN_ROLE_NAME).map(roleEntity -> {
        carl.setRoles(Set.of(roleEntity));
        return roleEntity;
      }).orElseThrow(() -> new RuntimeException("Database Initialization Error"));
      return accountRepository.save(carl);
    });
  }

  private void initTransactions() {
//    transactionRepository.deleteAll();
    if (transactionRepository.count() == 0) {
      createTransaction("IRS", 520, 10, "payment");
      createTransaction("MB Car Loan", 680.73, 10, "payment");
      createTransaction("Mortgage", 5086, 1, "payment");
      createTransaction("Home Equity", 4326, 19, "payment");
      createTransaction("Bank of America", 1000, 17, "payment");
      createTransaction("DSC Card Carl", 440, 19, "payment");
      createTransaction("DSC Card Krupa", 400, 31, "payment");
      createTransaction("WF Visa", 200, 12, "payment");
      createTransaction("DSC Loan Carl", 788, 4, "payment");
      createTransaction("DSC Loan Krupa", 552.6, 6, "payment");
      createTransaction("Fidelity Visa", 650, 18, "payment");
      createTransaction("AX - Plans", 1344, 2, "payment");
      createTransaction("AX - Subscriptions", 606, 2, "payment");

      createTransaction("First Paycheck", 4986, 15, "deposit");
      createTransaction("Second Paycheck", 4986, 31, "deposit");
      createTransaction("Carl Social Security", 2600, 23, "deposit");
      createTransaction("Rent", 4600, 10, "deposit");
      createTransaction("RSU", 8000, 23, "deposit");

//      createTransaction("Discover loan", 5600, 28, "deposit");

//      createTransaction("Safeco", 0, 3, "payment");
//      createTransaction("Liberty Mutual", 354.76, 18, "payment");
//      createTransaction("Garbage", 100, 20, "payment");
//      createTransaction("PG&E", 1000, 31, "payment");
//      createTransaction("Water", 100, 6, "payment");
//      createTransaction("Xfinity", 300, 31, "payment");
//      createTransaction("Jason Rent", 1920, 4, "payment");
//      createTransaction("Jason MG&E", 0, 1, "payment");
//      createTransaction("Jason Rent insurance", 0, 1, "payment");
    }
  }

  private void createTransaction(String name, double d, int dayOfMonth, String transactionType) {
    this.createTransaction(name, new BigDecimal(d), dayOfMonth, transactionType);
  }

  private void createTransaction(String name, int i, int dayOfMonth, String transactionType) {
    this.createTransaction(name, new BigDecimal(i), dayOfMonth, transactionType);
  }

  private void createTransaction(String name, BigDecimal amount, int dayOfMonth, String transactionType) {
    TransactionEntity entity = new TransactionEntity();
    entity.setAmount(amount);
    entity.setName(name);
    entity.setDayOfMonth(dayOfMonth);
    entity.setTransactionType(transactionType);
    accountRepository.findByEmail("carlthronson@gmail.com").map(account -> {
      entity.setPublisher(account);
      return true;
    });
    transactionRepository.save(entity);
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
