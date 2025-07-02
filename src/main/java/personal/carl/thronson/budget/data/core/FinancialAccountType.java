package personal.carl.thronson.budget.data.core;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.workflow.data.core.ProcessElement;

@MappedSuperclass
public class FinancialAccountType extends ProcessElement {

  @Getter @Setter private int startingBalance;
  @Getter @Setter private int dailySpending;
}
