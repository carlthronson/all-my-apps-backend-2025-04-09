package personal.carl.thronson.security.data.core;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.core.BaseObject;

@MappedSuperclass
public class ResetPasswordToken extends BaseObject {

  @Getter
  @Setter
  private String token;

  @Getter
  @Setter
  @Column(name = "expires_at", nullable = false, updatable = false)
  private LocalDateTime expiresAt;

  @Getter
  @Setter
  private boolean used;
}
