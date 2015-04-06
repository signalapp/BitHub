package org.whispersystems.bithub.storage;

import com.coinbase.api.exception.CoinbaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.bithub.client.CoinbaseClient;
import org.whispersystems.bithub.client.GithubClient;
import org.whispersystems.bithub.config.RepositoryConfiguration;
import org.whispersystems.bithub.entities.Payment;
import org.whispersystems.bithub.entities.Repository;
import org.whispersystems.bithub.entities.Transaction;
import org.whispersystems.bithub.util.Badge;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.dropwizard.lifecycle.Managed;

public class CacheManager implements Managed {

  private static final int UPDATE_FREQUENCY_MILLIS = 60 * 1000;

  private final Logger                   logger   = LoggerFactory.getLogger(CacheManager.class);
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

  private final CoinbaseClient                coinbaseClient;
  private final GithubClient                  githubClient;
  private final BigDecimal                    payoutRate;
  private final List<RepositoryConfiguration> repositories;

  private AtomicReference<CurrentPayment>    cachedPaymentStatus;
  private AtomicReference<List<Transaction>> cachedTransactions;
  private AtomicReference<List<Repository>>  cachedRepositories;

  public CacheManager(CoinbaseClient coinbaseClient,
                      GithubClient githubClient,
                      List<RepositoryConfiguration> repositories,
                      BigDecimal payoutRate)
  {
    this.coinbaseClient = coinbaseClient;
    this.githubClient   = githubClient;
    this.payoutRate     = payoutRate;
    this.repositories   = repositories;
  }

  @Override
  public void start() throws Exception {
    this.cachedPaymentStatus = new AtomicReference<>(createCurrentPaymentForBalance(coinbaseClient));
    this.cachedTransactions  = new AtomicReference<>(createRecentTransactions(coinbaseClient));
    this.cachedRepositories  = new AtomicReference<>(createRepositories(githubClient, repositories));

    initializeUpdates(coinbaseClient, githubClient, repositories);
  }

  @Override
  public void stop() throws Exception {
    this.executor.shutdownNow();
  }

  public List<Transaction> getRecentTransactions() {
    return cachedTransactions.get();
  }

  public CurrentPayment getCurrentPaymentAmount() {
    return cachedPaymentStatus.get();
  }

  public List<Repository> getRepositories() {
    return cachedRepositories.get();
  }

  public void initializeUpdates(final CoinbaseClient coinbaseClient,
                                final GithubClient githubClient,
                                final List<RepositoryConfiguration> repoConfigs)
  {
    executor.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        logger.warn("Running cache update...");
        try {
          CurrentPayment    currentPayment = createCurrentPaymentForBalance(coinbaseClient);
          List<Transaction> transactions   = createRecentTransactions      (coinbaseClient);
          List<Repository>  repositories   = createRepositories(githubClient, repoConfigs);

          cachedPaymentStatus.set(currentPayment);
          cachedTransactions.set(transactions);
          cachedRepositories.set(repositories);

        } catch (IOException | CoinbaseException e) {
          logger.warn("Failed to update badge", e);
        }
      }
    }, UPDATE_FREQUENCY_MILLIS, UPDATE_FREQUENCY_MILLIS, TimeUnit.MILLISECONDS);
  }

  private List<Repository> createRepositories(GithubClient githubClient,
                                              List<RepositoryConfiguration> configured)
  {
    List<Repository> repositoryList = new LinkedList<>();

    for (RepositoryConfiguration repository : configured) {
      repositoryList.add(githubClient.getRepository(repository.getUrl()));
    }

    return repositoryList;
  }

  private CurrentPayment createCurrentPaymentForBalance(CoinbaseClient coinbaseClient)
      throws IOException, CoinbaseException
  {
    BigDecimal currentBalance = coinbaseClient.getAccountBalance();
    BigDecimal paymentBtc     = currentBalance.multiply(payoutRate);
    BigDecimal exchangeRate   = coinbaseClient.getExchangeRate();
    BigDecimal paymentUsd     = paymentBtc.multiply(exchangeRate);

    paymentUsd = paymentUsd.setScale(2, RoundingMode.CEILING);
    return new CurrentPayment(Badge.createFor(paymentUsd.toPlainString()),
                              Badge.createSmallFor(paymentUsd.toPlainString()),
                              new Payment(paymentUsd.toPlainString()));
  }

  private List<Transaction> createRecentTransactions(CoinbaseClient coinbaseClient)
          throws IOException, CoinbaseException
  {
    List<com.coinbase.api.entity.Transaction> recentTransactions = coinbaseClient.getRecentTransactions();
    BigDecimal                                exchangeRate       = coinbaseClient.getExchangeRate();
    List<Transaction>                         transactions       = new LinkedList<>();

    for (com.coinbase.api.entity.Transaction coinbaseTransaction : recentTransactions) {
      try {
        if (isSentTransaction(coinbaseTransaction)) {
          CoinbaseTransactionParser parser      = new CoinbaseTransactionParser(coinbaseTransaction);
          String                    url         = parser.parseUrlFromMessage();
          String                    sha         = parser.parseShaFromUrl(url);
          String                    description = githubClient.getCommitDescription(url);

          transactions.add(new Transaction(parser.parseDestinationFromMessage(),
                                           parser.parseAmountInDollars(exchangeRate),
                                           url, sha, parser.parseTimestamp(),
                                           description));

          if (transactions.size() >= 10)
            break;
        }
      } catch (ParseException e) {
        logger.warn("Parse", e);
      }
    }

    return transactions;
  }

  private boolean isSentTransaction(com.coinbase.api.entity.Transaction coinbaseTransaction) {
    BigDecimal amount = coinbaseTransaction.getAmount().getAmount();
    return amount.compareTo(new BigDecimal(0.0)) < 0;
  }


}
