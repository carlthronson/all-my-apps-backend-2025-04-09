package personal.carl.thronson.budget.data.core;

import java.math.BigDecimal;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.core.BaseObject;

@MappedSuperclass
public class Transaction extends BaseObject {

  @Getter @Setter private String name;
  @Getter @Setter private BigDecimal amount;
  @Getter @Setter private int dayOfMonth;
  @Getter @Setter private String transactionType;
}
