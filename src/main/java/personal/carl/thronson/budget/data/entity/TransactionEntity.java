package personal.carl.thronson.budget.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import personal.carl.thronson.budget.data.core.Transaction;

@Entity(name = "budget_transaction")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TransactionEntity extends Transaction {

}
