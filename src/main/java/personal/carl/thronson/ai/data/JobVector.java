package personal.carl.thronson.ai.data;

import lombok.Getter;
import lombok.Setter;

public class JobVector {

  @Getter @Setter private String text;
  @Getter @Setter private Double score;
  @Getter @Setter private String metadata;
}
