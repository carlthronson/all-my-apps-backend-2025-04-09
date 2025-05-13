package personal.carl.thronson.budget.gql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import graphql.schema.DataFetchingEnvironment;
import personal.carl.thronson.budget.core.Forecast;
import personal.carl.thronson.budget.data.core.DailyActivity;
import personal.carl.thronson.budget.data.core.Transaction;
import personal.carl.thronson.budget.data.entity.TransactionEntity;
import personal.carl.thronson.security.AuthorizationService;
import personal.carl.thronson.security.data.entity.AccountEntity;

@Service
@Transactional
public class BudgetService {

  Logger logger = Logger.getLogger(getClass().getName());

  @Autowired
  private AuthorizationService authorizationService;

  public Forecast getForecast(
      @Argument(name = "startBalance") int startBalance,
      @Argument(name = "cash") int cash,
      DataFetchingEnvironment environment) {
    List<TransactionEntity> payments = getTransactions(environment);

    List<DailyActivity> dailyActivity = new ArrayList<>();
    int runningBalance = startBalance;
    int maxDebt = runningBalance;

    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusMonths(24); // Iterate for one month into the future

    LocalDate firstNegativeDate = null;
    int firstNegativeBalance = 0;
    LocalDate dateOfMaxDebt = startDate;
    for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
      BigDecimal startingBalance = new BigDecimal(runningBalance);
//      runningBalance -= cash;
      if (runningBalance < maxDebt) {
        maxDebt = runningBalance;
        dateOfMaxDebt = date;
      }
      // Perform your operations with each date here
      List<Transaction> todaysPayments = new ArrayList<>();
      Transaction cashPayment = new Transaction();
      cashPayment.setAmount(new BigDecimal(cash));
      cashPayment.setName("Cash");
      cashPayment.setTransactionType("payment");
      todaysPayments.add(cashPayment);
      for (TransactionEntity payment : payments) {
        switch (date.getDayOfWeek()) {
        case SATURDAY:
        case SUNDAY:
          continue;
        default:
        }
        if (isDueOn(payment, date)) {
          todaysPayments.add(payment);
        } else {
          // if today is Friday, check if payment is due Saturday
          switch (date.getDayOfWeek()) {
          case FRIDAY:
            LocalDate saturday = date.plusDays(1);
            if (isDueOn(payment, saturday)) {
              todaysPayments.add(payment);
            } else {
              LocalDate sunday = saturday.plusDays(1);
              if (isDueOn(payment, sunday)) {
                todaysPayments.add(payment);
              } else {
              }
            }
          case MONDAY:
            break;
          case SATURDAY:
            break;
          case SUNDAY:
            break;
          case THURSDAY:
            break;
          case TUESDAY:
            break;
          case WEDNESDAY:
            break;
          default:
            break;
          }
        }
      }

      if (!todaysPayments.isEmpty()) {
        logger.info("");
        logger.info("******************************************");
        logger.info(date.toString());
        logger.info("Starting balance: " + runningBalance);
        logger.info("");
      }
      for (Transaction payment : todaysPayments) {
        String type = payment.getTransactionType();
        switch (type) {
        case "payment":
          runningBalance = runningBalance - payment.getAmount().intValue();
          break;
        case "deposit":
          runningBalance = runningBalance + payment.getAmount().intValue();
          break;
        }

        logger.info(String.format("%s\t%s\t%s", type, payment.getAmount(), payment.getName()));
      }
      if (!todaysPayments.isEmpty()) {
        logger.info("");
        logger.info("Ending balance: " + runningBalance);
        DailyActivity balance = new DailyActivity();
        balance.setDate(date);
        balance.setStartingBalance(startingBalance);
        balance.setTransactions(todaysPayments);
        balance.setEndingBalance(new BigDecimal(runningBalance));
        dailyActivity.add(balance);
      }
      if (runningBalance < 0 && firstNegativeDate == null) {
        firstNegativeDate = date;
        firstNegativeBalance = runningBalance;
      }
    }
    logger.info("Date of max debt: " + dateOfMaxDebt);
    logger.info("Max debt: " + maxDebt);
    logger.info("Date of first negative balance: " + firstNegativeDate);
    logger.info("First negative balance: " + firstNegativeBalance);
    Forecast result = new Forecast();
    result.setStartingBalance(startBalance);
    result.setCash(cash);
    result.setEndingDate(endDate);
    result.setFirstNegativeBalance(firstNegativeDate);
    result.setMaxDebt(maxDebt);
    result.setDailyActivity(dailyActivity);
    return result;
  }

  private static boolean isDueOn(TransactionEntity payment, LocalDate date) {
    // trim back transactions due at end of month for shorter months
    LocalDate target = getDueDate(date, payment.getDayOfMonth());
    boolean rightDay = target.getDayOfMonth() == date.getDayOfMonth();
    if (!rightDay) {
      return false;
    }
    boolean isPlanActive = planActive(date, payment);
    return isPlanActive;
  }

  private static LocalDate getDueDate(LocalDate date, int dayOfMonth) {
    LocalDate nextDueDate;
    try {
      nextDueDate = LocalDate.of(date.getYear(), date.getMonth(), dayOfMonth);
    } catch (Exception e) {
      nextDueDate = getDueDate(date, dayOfMonth - 1);
    }
    return nextDueDate;
  }

  private static boolean planActive(LocalDate date, TransactionEntity payment) {
    boolean hasStarted = payment.getStartDate() == null ||
        date.isAfter(payment.getStartDate());

    boolean hasEnded = payment.getEndDate() != null &&
        date.isAfter(payment.getEndDate());
    boolean result = hasStarted && !hasEnded;

    return result;
  }

  public List<TransactionEntity> getTransactions(
      DataFetchingEnvironment environment) {
    AccountEntity account = authorizationService.getAccount().get();
    List<TransactionEntity> transactions = account.getPublishedTransactions();
    return transactions;
  }

}
