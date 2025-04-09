package personal.carl.thronson;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import personal.carl.thronson.data.core.Role;
import personal.carl.thronson.data.entity.AccountEntity;
import personal.carl.thronson.data.entity.RoleEntity;
import personal.carl.thronson.data.repo.AccountRepository;
import personal.carl.thronson.data.repo.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Override
  public void run(String... args) throws Exception {
    roleRepository.findByName(Role.ADMIN_ROLE_NAME).ifPresentOrElse(roleEntity -> {
    }, () -> {
      RoleEntity admin = new RoleEntity();
      admin.setName(Role.ADMIN_ROLE_NAME);
      admin.setLabel(Role.ADMIN_ROLE_LABEL);
      roleRepository.save(admin);
    });
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
  }
}
