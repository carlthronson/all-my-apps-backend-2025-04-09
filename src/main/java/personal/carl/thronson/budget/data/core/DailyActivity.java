package personal.carl.thronson.budget.data.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class DailyActivity {

  @Getter @Setter private LocalDate date;
  @Getter @Setter private BigDecimal endingBalance;
  @Getter @Setter private BigDecimal startingBalance;
  @Getter @Setter private List<Transaction> transactions;
}
