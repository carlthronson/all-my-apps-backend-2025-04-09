package personal.carl.thronson.budget.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import personal.carl.thronson.budget.data.core.FinancialAccountType;

@Entity(name = "budget_account_type")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class FinancialAccountTypeEntity extends FinancialAccountType {

}
