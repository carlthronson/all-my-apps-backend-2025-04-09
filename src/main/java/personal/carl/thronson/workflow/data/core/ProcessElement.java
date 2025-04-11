package personal.carl.thronson.workflow.data.core;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.core.BaseObject;

@MappedSuperclass
public class ProcessElement extends BaseObject {

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
