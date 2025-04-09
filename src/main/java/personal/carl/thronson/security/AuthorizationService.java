package personal.carl.thronson.security;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import personal.carl.thronson.data.core.AccountAuthorizationPrincipal;
import personal.carl.thronson.data.entity.AccountEntity;
import personal.carl.thronson.data.entity.ResetPasswordTokenEntity;
import personal.carl.thronson.data.repo.AccountRepository;
import personal.carl.thronson.data.repo.ResetPasswordTokenRepository;

@Service
@Transactional
public class AuthorizationService {

  Logger logger = Logger.getLogger(getClass().getName());

  @Autowired
  AccountRepository accountRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ResetPasswordTokenRepository resetPasswordTokenRepository;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private HttpServletResponse response;

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
    response.setHeader("Authorization", "Bearer " + token);
    return accountRepository.findByEmail(email);
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
}
