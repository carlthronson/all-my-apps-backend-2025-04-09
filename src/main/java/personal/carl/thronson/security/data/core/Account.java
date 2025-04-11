package personal.carl.thronson.security.data.core;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.core.BaseObject;

@MappedSuperclass
public class Account extends BaseObject {

  @Getter
  @Setter
  private String email;

  @Getter
  @Setter
  private String password;

  @Getter
  @Setter
  private boolean enabled;
}
