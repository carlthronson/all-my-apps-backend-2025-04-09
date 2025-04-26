package personal.carl.thronson.budget.data.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.core.BaseObject;

@Entity(name = "budget_transaction")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TransactionEntity extends BaseObject {

  @Getter @Setter private String name;
  @Getter @Setter private BigDecimal amount;
  @Getter @Setter private int dayOfMonth;
  @Getter @Setter private String transactionType;
}
