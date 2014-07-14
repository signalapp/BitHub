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

package org.whispersystems.bithub.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.bithub.entities.Transaction;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import io.dropwizard.views.View;

/**
 * A rendered HTML view of recent BitHub transactions.
 *
 * @author Moxie Marlinspike
 */
public class RecentTransactionsView extends View {

  private final Logger                logger       = LoggerFactory.getLogger(RecentTransactionsView.class);
  private final List<TransactionView> transactions = new LinkedList<>();

  public RecentTransactionsView(List<Transaction> recentTransactions, BigDecimal exchangeRate) {
    super("recent_transactions.mustache");

    for (Transaction transaction : recentTransactions) {
      try {
        if (isSentTransaction(transaction)) {
          transactions.add(new TransactionView(exchangeRate,
                                               transaction.getAmount(),
                                               transaction.getCreatedTime(),
                                               transaction.getNotes()));
        }
      } catch (ParseException e) {
        logger.warn("Error parsing: ", e);
      }
    }
  }

  private boolean isSentTransaction(Transaction transaction) {
    BigDecimal amount = new BigDecimal(transaction.getAmount());
    return amount.compareTo(new BigDecimal(0.0)) < 0;
  }

  public List<TransactionView> getTransactions() {
    return transactions;
  }
}
