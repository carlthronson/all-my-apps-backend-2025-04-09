package personal.carl.thronson.budget.gql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.RestController;

import graphql.schema.DataFetchingEnvironment;
import jakarta.transaction.Transactional;
import personal.carl.thronson.budget.core.Forecast;
import personal.carl.thronson.budget.data.entity.TransactionEntity;
import personal.carl.thronson.budget.data.repo.TransactionRepository;

@RestController
@Transactional
public class BudgetResolver {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private BudgetService service;

  @QueryMapping(name = "getTransactions")
  public List<TransactionEntity> getTransactions(
      DataFetchingEnvironment environment) throws Exception {
    return transactionRepository.findAll();
  }

  @MutationMapping(name = "createTransaction")
  public Long createTransaction(
      @Argument(name = "name") String name,
      @Argument(name = "amount") BigDecimal amount,
      @Argument(name = "dayOfMonth") int dayOfMonth,
      @Argument(name = "transactionType") String transactionType,
      @Argument(name = "startDate") LocalDate startDate,
      @Argument(name = "endDate") LocalDate endDate,
      DataFetchingEnvironment environment) throws Exception {
    TransactionEntity entity = new TransactionEntity();
    entity.setAmount(amount);
    entity.setName(name);
    entity.setDayOfMonth(dayOfMonth);
    entity.setTransactionType(transactionType);
    if (startDate != null)
      entity.setStartDate(startDate);
    if (endDate != null)
      entity.setEndDate(endDate);
    return transactionRepository.save(entity).getId();
  }

  @MutationMapping(name = "updateTransaction")
  public Boolean updateTransaction(
      @Argument(name = "id") Long id,
      @Argument(name = "name") String name,
      @Argument(name = "amount") BigDecimal amount,
      @Argument(name = "dayOfMonth") int dayOfMonth,
      @Argument(name = "transactionType") String transactionType,
      @Argument(name = "startDate") LocalDate startDate,
      @Argument(name = "endDate") LocalDate endDate,
      DataFetchingEnvironment environment) throws Exception {
    return transactionRepository.findById(id).map(entity -> {
      entity.setName(name);
      entity.setAmount(amount);
      entity.setDayOfMonth(dayOfMonth);
      entity.setTransactionType(transactionType);
      if (startDate != null)
        entity.setStartDate(startDate);
      if (endDate != null)
        entity.setEndDate(endDate);
      transactionRepository.save(entity);
      return true;
    }).orElse(false);
  }

  @MutationMapping(name = "deleteTransaction")
  public Boolean deleteTransaction(
      @Argument(name = "id") Long id,
      DataFetchingEnvironment environment) throws Exception {
    return transactionRepository.findById(id).map(entity -> {
      transactionRepository.deleteById(id);
      return true;
    }).orElse(false);
  }

  @QueryMapping(name = "getForecast")
  public Forecast getForecast(
      @Argument(name = "startBalance") int startBalance,
      @Argument(name = "cash") int cash,
      DataFetchingEnvironment environment) throws Exception {
    return service.getForecast(startBalance, cash);
  }

}
