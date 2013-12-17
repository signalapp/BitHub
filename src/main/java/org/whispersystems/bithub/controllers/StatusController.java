/**
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.whispersystems.bithub.controllers;

import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.bithub.client.CoinbaseClient;
import org.whispersystems.bithub.entities.Payment;
import org.whispersystems.bithub.entities.Transaction;
import org.whispersystems.bithub.util.Badge;
import org.whispersystems.bithub.views.RecentTransactionsView;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles incoming API calls for BitHub instance status information.
 *
 * @author Moxie Marlinspike
 */
@Path("/v1/status")
public class StatusController {

  private static final int UPDATE_FREQUENCY_MILLIS = 60 * 1000;

  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
  private final Logger                   logger   = LoggerFactory.getLogger(StatusController.class);

  private final AtomicReference<CurrentPayment>         cachedPaymentStatus;
  private final AtomicReference<RecentTransactionsView> cachedTransactions;
  private final AtomicReference<RecentTransactionsView> cachedTransactionsInBTC;
  private final BigDecimal                              payoutRate;

  public StatusController(CoinbaseClient coinbaseClient, BigDecimal payoutRate) throws IOException {
    this.payoutRate          = payoutRate;
    this.cachedPaymentStatus = new AtomicReference<>(createCurrentPaymentForBalance(coinbaseClient));
    this.cachedTransactions  = new AtomicReference<>(createRecentTransactionsView(coinbaseClient));
    this.cachedTransactionsInBTC = new AtomicReference<>(createRecentTransactionsViewInBTC());

    initializeUpdates(coinbaseClient);
  }

  @Timed
  @GET
  @Path("/transactions")
  public Response getTransactions(@QueryParam("format") @DefaultValue("html") String format)
      throws IOException
  {
    if (format.equals("json")) {
      return Response.ok(cachedTransactions.get(), MediaType.APPLICATION_JSON_TYPE).build();
    } else {
      return Response.ok(cachedTransactions.get(), MediaType.TEXT_HTML_TYPE).build();
    }
  }

  @Timed
  @GET
  @Path("/transactions/BTC")
  public Response getTransactions(@QueryParam("format") @DefaultValue("html") String format)
      throws IOException
  {
    if (format.equals("json")) {
      return Response.ok(cachedTransactionsInBTC.get(), MediaType.APPLICATION_JSON_TYPE).build();
    } else {
      return Response.ok(cachedTransactionsInBTC.get(), MediaType.TEXT_HTML_TYPE).build();
    }
  }

  @Timed
  @GET
  @Path("/payment/commit")
  public Response getCurrentCommitPrice(@QueryParam("format") @DefaultValue("png") String format)
      throws IOException
  {
    if (format.equals("json")) {
      return Response.ok(cachedPaymentStatus.get().getEntity(), MediaType.APPLICATION_JSON_TYPE).build();
    } else if (format.equals("png_small")) {
      return Response.ok(cachedPaymentStatus.get().getSmallBadge(), "image/png").build();
    } else {
      return Response.ok(cachedPaymentStatus.get().getBadge(), "image/png").build();
    }
  }

  private CurrentPayment createCurrentPaymentForBalance(CoinbaseClient coinbaseClient)
      throws IOException
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

  private RecentTransactionsView createRecentTransactionsView(CoinbaseClient coinbaseClient)
      throws IOException
  {
    List<Transaction> recentTransactions = coinbaseClient.getRecentTransactions();
    BigDecimal        exchangeRate       = coinbaseClient.getExchangeRate();

    return new RecentTransactionsView(recentTransactions, exchangeRate);
  }

  private RecentTransactionsView createRecentTransactionsViewInBTC()
      throws IOException
  {
    List<Transaction> recentTransactions = coinbaseClient.getRecentTransactions();
    BigDecimal        exchangeRate       = new BigDecimal(1.0);

    return new RecentTransactionsView(recentTransactions, exchangeRate);
  }

  public void initializeUpdates(final CoinbaseClient coinbaseClient) {
    executor.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          CurrentPayment currentPayment = createCurrentPaymentForBalance(coinbaseClient);
          cachedPaymentStatus.set(currentPayment);
        } catch (IOException e) {
          logger.warn("Failed to update badge", e);
        }
      }
    }, UPDATE_FREQUENCY_MILLIS, UPDATE_FREQUENCY_MILLIS, TimeUnit.MILLISECONDS);

    executor.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          RecentTransactionsView view = createRecentTransactionsView(coinbaseClient);
          cachedTransactions.set(view);
        } catch (IOException e) {
          logger.warn("Failed to update recent transactions", e);
        }
      }
    }, UPDATE_FREQUENCY_MILLIS, UPDATE_FREQUENCY_MILLIS, TimeUnit.MILLISECONDS);
  }

  private class CurrentPayment {
    private final byte[]  badge;
    private final byte[]  smallBadge;
    private final Payment entity;

    private CurrentPayment(byte[] badge, byte[] smallBadge, Payment entity) {
      this.badge      = badge;
      this.smallBadge = smallBadge;
      this.entity     = entity;
    }

    private byte[] getBadge() {
      return badge;
    }

    private byte[] getSmallBadge() {
      return smallBadge;
    }

    private Payment getEntity() {
      return entity;
    }
  }

}
