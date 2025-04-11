package personal.carl.thronson.security.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.security.data.core.ResetPasswordToken;

@Entity(name = "reset_password_token")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ResetPasswordTokenEntity extends ResetPasswordToken {

  @Getter
  @Setter
  @ManyToOne(optional = false)
  @JoinColumn(name = "account_id", unique = false)
  private AccountEntity account;
}
