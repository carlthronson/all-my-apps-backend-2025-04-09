package personal.carl.thronson.budget.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.budget.data.core.Transaction;
import personal.carl.thronson.security.data.entity.AccountEntity;

@Entity(name = "budget_transaction")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TransactionEntity extends Transaction {

  @ManyToOne
  @JoinColumn(name = "publisher_id") // This creates a publisher_id column in the transaction table
  @Getter
  @Setter
  private AccountEntity publisher;
}
