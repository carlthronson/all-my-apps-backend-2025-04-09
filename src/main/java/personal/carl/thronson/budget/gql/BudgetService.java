package personal.carl.thronson.budget.gql;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.budget.core.Forecast;
import personal.carl.thronson.budget.data.core.DailyBalance;
import personal.carl.thronson.budget.data.core.Transaction;
import personal.carl.thronson.budget.data.entity.TransactionEntity;
import personal.carl.thronson.budget.data.repo.TransactionRepository;

@Service
@Transactional
public class BudgetService {

  Logger logger = Logger.getLogger(getClass().getName());

  @Autowired
  private TransactionRepository transactionRepository;

  public Forecast getForecast(int startBalance, int cash) {
    List<TransactionEntity> payments = this.transactionRepository.findAll();

    List<DailyBalance> dailyBalances = new ArrayList<>();
    int runningBalance = startBalance;
    int maxDebt = runningBalance;

    OffsetDateTime startDate = OffsetDateTime.now();
    OffsetDateTime endDate = startDate.plusMonths(24); // Iterate for one month into the future

    OffsetDateTime firstNegativeDate = null;
    int firstNegativeBalance = 0;
    OffsetDateTime dateOfMaxDebt = startDate;
    for (OffsetDateTime date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
      BigDecimal startingBalance = new BigDecimal(runningBalance);
//      runningBalance -= cash;
      if (runningBalance < 0 && firstNegativeDate == null) {
        firstNegativeDate = date;
        firstNegativeBalance = runningBalance;
      }
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
        if (isDueOn(payment, date)) {
          todaysPayments.add(payment);
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
        DailyBalance balance = new DailyBalance();
        balance.setDate(date);
        balance.setStartingBalance(startingBalance);
        balance.setTransactions(todaysPayments);
        balance.setEndingBalance(new BigDecimal(runningBalance));
        dailyBalances.add(balance);
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
    result.setDailyBalances(dailyBalances);
    return result;
  }

  private static boolean isDueOn(TransactionEntity payment, OffsetDateTime date) {
    try {
      LocalDate paymentDueDate = getDueDate(payment, date);
      switch (date.getDayOfWeek()) {
      case SATURDAY:
      case SUNDAY:
        return false;
      case FRIDAY:
        switch (paymentDueDate.getDayOfWeek()) {
        case SATURDAY:
          paymentDueDate = paymentDueDate.minusDays(1);
          break;
        case SUNDAY:
          paymentDueDate = paymentDueDate.minusDays(2);
          break;
        default:
          break;
        }
      default:
        break;
      }
      return date.getMonth() == paymentDueDate.getMonth()
          && date.getDayOfMonth() == paymentDueDate.getDayOfMonth();
    } catch (Exception ex) {
      return false;
    }
  }

  public static LocalDate getDueDate(TransactionEntity transaction, OffsetDateTime date) {
    int currentYear = date.getYear();
    Month currentMonth = date.getMonth();
    Integer arbitraryDay = transaction.getDayOfMonth();
    try {
      return LocalDate.of(currentYear, currentMonth, arbitraryDay);
    } catch (DateTimeException ex) {
      try {
//        logger.info(ex.getLocalizedMessage());
        arbitraryDay--;
        LocalDate result = LocalDate.of(currentYear, currentMonth, arbitraryDay);
//        logger.info("New date: " + result);
        return result;
      } catch (DateTimeException ex2) {
//        ex2.printStackTrace();
        throw ex;
      }
    }
  }

}
