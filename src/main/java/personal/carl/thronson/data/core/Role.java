package personal.carl.thronson.data.core;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.data.BaseObject;

@MappedSuperclass
public class Role extends BaseObject {

  public static final String ADMIN_ROLE_NAME = "ADMIN";
  public static final String ADMIN_ROLE_LABEL = "Admin";

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String label;

  @Override
  public String toString() {
    return this.name;
  }
}
