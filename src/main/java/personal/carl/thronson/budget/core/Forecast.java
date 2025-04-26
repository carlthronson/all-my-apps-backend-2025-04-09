package personal.carl.thronson.budget.core;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.budget.data.core.DailyBalance;

public class Forecast {

  @Getter @Setter private int startingBalance;
  @Getter @Setter private int cash;
  @Getter @Setter private OffsetDateTime firstNegativeBalance;
  @Getter @Setter private int maxDebt;
  @Getter @Setter private List<DailyBalance> dailyBalances;
  @Getter @Setter private OffsetDateTime endingDate;
}
