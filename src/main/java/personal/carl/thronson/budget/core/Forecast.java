package personal.carl.thronson.budget.core;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.budget.data.core.DailyActivity;

public class Forecast {

  @Getter @Setter private int startingBalance;
  @Getter @Setter private int cash;
  @Getter @Setter private LocalDate firstNegativeBalance;
  @Getter @Setter private int maxDebt;
  @Getter @Setter private List<DailyActivity> dailyActivity;
  @Getter @Setter private LocalDate endingDate;
}
