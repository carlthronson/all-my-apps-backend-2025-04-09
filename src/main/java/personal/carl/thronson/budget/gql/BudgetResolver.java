package personal.carl.thronson.budget.gql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import personal.carl.thronson.security.AuthorizationService;
import personal.carl.thronson.security.data.entity.AccountEntity;

@RestController
@Transactional
public class BudgetResolver {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private BudgetService service;

  @Autowired
  private AuthorizationService authorizationService;

  @QueryMapping(name = "getTransactions")
  public List<TransactionEntity> getTransactions(
      DataFetchingEnvironment environment) throws Exception {
    return service.getTransactions(environment);
  }

  @MutationMapping(name = "createTransaction")
  public Optional<Long> createTransaction(
      @Argument(name = "name") String name,
      @Argument(name = "amount") BigDecimal amount,
      @Argument(name = "dayOfMonth") int dayOfMonth,
      @Argument(name = "transactionType") String transactionType,
      @Argument(name = "startDate") LocalDate startDate,
      @Argument(name = "endDate") LocalDate endDate,
      DataFetchingEnvironment environment) throws Exception {
    return authorizationService.getAccount().map(account -> {
      TransactionEntity entity = new TransactionEntity();
      entity.setAmount(amount);
      entity.setName(name);
      entity.setDayOfMonth(dayOfMonth);
      entity.setTransactionType(transactionType);
      if (startDate != null)
        entity.setStartDate(startDate);
      if (endDate != null)
        entity.setEndDate(endDate);
      entity.setPublisher(account);
      return transactionRepository.save(entity).getId();
    });
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
  public Optional<Long> deleteTransaction(
      @Argument(name = "id") Long id,
      DataFetchingEnvironment environment) throws Exception {
    Long result = 0L;
    AccountEntity account = authorizationService.getAccount().get();
    List<TransactionEntity> transactions = account.getPublishedTransactions();
    for (TransactionEntity transaction: transactions) {
      if (transaction.getId() == id) {
        transactionRepository.delete(transaction);
        result++;
      }
    }
    return Optional.of(result);
  }

  @QueryMapping(name = "getForecast")
  public Forecast getForecast(
      @Argument(name = "startBalance") int startBalance,
      @Argument(name = "cash") int cash,
      DataFetchingEnvironment environment) throws Exception {
    return service.getForecast(startBalance, cash, environment);
  }

}
