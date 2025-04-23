package personal.carl.thronson.security;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import graphql.schema.DataFetchingEnvironment;
import personal.carl.thronson.security.data.core.AccountAuthorizationPrincipal;
import personal.carl.thronson.security.data.entity.AccountEntity;
import personal.carl.thronson.security.data.entity.ResetPasswordTokenEntity;
import personal.carl.thronson.security.data.repo.AccountRepository;
import personal.carl.thronson.security.data.repo.ResetPasswordTokenRepository;
import personal.carl.thronson.security.data.repo.RoleRepository;

@Service
@Transactional
public class AuthorizationService {

  Logger logger = Logger.getLogger(getClass().getName());

  @Autowired
  AccountRepository accountRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ResetPasswordTokenRepository resetPasswordTokenRepository;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Autowired
  private AuthenticationManager authenticationManager;

//  @Autowired
//  private HttpServletResponse response;

  public boolean resetPassword(String email, String password, String token) {
    return accountRepository.findByEmail(email).map(accountEntity -> {
      return accountEntity.getTokens().stream().filter(t -> {
        logger.info("Expires at: " + t.getExpiresAt());
        boolean isExpired = t.getExpiresAt().isBefore(LocalDateTime.now());
        logger.info("Is used: " + t.isUsed());
        boolean matches = passwordEncoder.matches(token, t.getToken());
        logger.info("Matches: " + matches);
        return matches && !t.isUsed() && !isExpired;
      }).findFirst().map(tokenEntity -> {
        accountEntity.setPassword(passwordEncoder.encode(password));
        accountRepository.save(accountEntity);
        tokenEntity.setUsed(true);
        resetPasswordTokenRepository.save(tokenEntity);
        return true;
      }).orElseThrow(() -> {
        return new IllegalArgumentException("Token not found"); // Throw if no valid token
      });
    }).orElseThrow(() -> {
        return new IllegalArgumentException("Account not found"); // Throw if no account
    });
  }

  public Optional<AccountEntity> login(String email, String password) {
    // Create the authentication object
    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(email, password));
    logger.info("Authentication object: " + authentication.getClass().getName());
    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetails) {
      UserDetails userDetails = (UserDetails) principal;
      logger.info("Authentication principal username: " + userDetails.getUsername());
    }

    // Add the authentication object to the security context
    SecurityContextHolder.getContext().setAuthentication(authentication);
    // Generate a JWT token for future requests
    String token = jwtTokenUtil.generateToken(email, authentication.getAuthorities());
    return accountRepository.findByEmail(email).map(accountEntity -> {
      accountEntity.setAuthToken(token);
      return accountEntity;
    });
  }

  public String generateResetPasswordToken(String email) {
    return accountRepository.findByEmail(email).map(accountEntity -> {
      ResetPasswordTokenEntity entity = new ResetPasswordTokenEntity();
      entity.setAccount(accountEntity);
      String token = TokenGenerator.generateToken(16);
      String encryptedToken = passwordEncoder.encode(token);
      entity.setToken(encryptedToken);
      LocalDateTime expiresAt = LocalDateTime.now().plusDays(2);
      logger.info("Expires at: " + expiresAt);
      entity.setExpiresAt(expiresAt);
      entity = resetPasswordTokenRepository.save(entity);
      return token;
    }).orElseThrow(() -> {
      return new IllegalArgumentException("Account not found");
    });
  }

  public Optional<AccountEntity> getAccount() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    logger.info("getAccount: " + authentication);
    if (authentication != null && authentication.getPrincipal() != null) {
      Object principal = authentication.getPrincipal();
      if (principal instanceof AccountAuthorizationPrincipal) {
        String email = ((AccountAuthorizationPrincipal)principal).getUsername();
        logger.info("getAccount: email: " + email);
        return accountRepository.findByEmail(email);
      } else if (principal instanceof String) {
        String email = principal.toString();
        logger.info("getAccount: email: " + email);
        return accountRepository.findByEmail(email);
      }
    }
    return Optional.empty();
  }

  public Optional<AccountEntity> signup(
      @Argument(name = "email") String email,
      @Argument(name = "password") String password,
      @Argument(name = "name") String name,
      DataFetchingEnvironment environment) throws Exception {
    // TODO Auto-generated method stub
    logger.info(String.format("fellowSignup: email [%s] password [%s]", email, password));
    return accountRepository.findByEmail(email).map(accountEntity -> {
        logger.info("This account already exists, so make sure it's the same person");
        this.login(email, password);
        return accountEntity;
    }).or(() -> {
      return this.createNewAccount(email, password, "ADMIN").map(account -> {
        this.login(email, password);
//        FellowEntity newEntity = new FellowEntity();
//        newEntity.setAccount(account);
//        setFellowFields(newEntity, name, isBetaTester, isCollaborator, message, referralCode, isReferralPartner);
//        return fellowRepository.save(newEntity);
        return account;
      });
    });
  }

  private Optional<AccountEntity> createNewAccount(String email, String password, String role) {
    logger.info("This email has never been used");
    AccountEntity newAccountEntity = new AccountEntity();
    newAccountEntity.setEmail(email);
    if (password != null) {
      newAccountEntity.setPassword(passwordEncoder.encode(password));
    }
    newAccountEntity.setEnabled(true);
    return roleRepository.findByName(role).map(roleEntity -> {
      newAccountEntity.setRoles(Set.of(roleEntity));
      return accountRepository.save(newAccountEntity);
    });
  }
}
