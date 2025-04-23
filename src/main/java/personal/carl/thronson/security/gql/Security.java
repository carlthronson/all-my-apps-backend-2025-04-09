package personal.carl.thronson.security.gql;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import graphql.schema.DataFetchingEnvironment;
import personal.carl.thronson.security.AuthorizationService;
import personal.carl.thronson.security.data.entity.AccountEntity;

@RestController
@Transactional
public class Security {

  @Autowired
  private AuthorizationService authorizationService;

  @MutationMapping(name = "login")
  public Optional<AccountEntity> login(
      @Argument(name = "email") String email,
      @Argument(name = "password") String password,
      DataFetchingEnvironment environment) throws Exception {
    return authorizationService.login(email, password);
  }

  @QueryMapping(name = "VerifyAuthToken")
  public Optional<AccountEntity> verifyAuthToken(
      DataFetchingEnvironment environment) throws Exception {
    return authorizationService.getAccount().map(accountEntity -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      accountEntity.setAuthToken(auth.toString());
      return accountEntity;
    });
  }

  @QueryMapping(name = "getMyAccount")
  public Optional<AccountEntity> getMyAccount(
      DataFetchingEnvironment environment) throws Exception {
  return authorizationService.getAccount();
  }

  @MutationMapping(name = "resetPassword")
  public boolean resetPassword(
      @Argument(name = "email") String email,
      @Argument(name = "password") String password,
      @Argument(name = "token") String token,
      DataFetchingEnvironment environment) throws Exception {
    return authorizationService.resetPassword(email, password, token);
  }

  @MutationMapping(name = "generateResetPasswordToken")
  public String generateResetPasswordToken(
      @Argument(name = "email") String email,
      DataFetchingEnvironment environment) throws Exception {
    return authorizationService.generateResetPasswordToken(email);
  }

  @MutationMapping(name = "signup")
  public Optional<AccountEntity> signup(
      @Argument(name = "email") String email,
      @Argument(name = "password") String password,
      @Argument(name = "name") String name,
      DataFetchingEnvironment environment) throws Exception {
    return authorizationService.signup(email, password, name, environment);
  }
}
