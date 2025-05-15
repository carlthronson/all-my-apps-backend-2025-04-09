package personal.carl.thronson.flashcards.data.core;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.workflow.data.core.ProcessElement;

@MappedSuperclass
public class Card extends ProcessElement {

  // Custom field
  @Getter
  @Setter
  private String question;

  // Custom field
  @Getter
  @Setter
  private String answer;
}
