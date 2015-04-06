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

package org.whispersystems.bithub.client;

import com.coinbase.api.Coinbase;
import com.coinbase.api.CoinbaseBuilder;
import com.coinbase.api.entity.Account;
import com.coinbase.api.entity.Transaction;
import com.coinbase.api.exception.CoinbaseException;
import org.joda.money.Money;
import org.whispersystems.bithub.entities.Author;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Handles interaction with the Coinbase API.
 *
 * @author Moxie Marlinspike
 */
public class CoinbaseClient {

  private final Coinbase coinbase;

  public CoinbaseClient(String apiKey, String apiSecret) {
    this.coinbase = new CoinbaseBuilder().withApiKey(apiKey, apiSecret).build();
  }

  public List<Transaction> getRecentTransactions()
      throws CoinbaseException, IOException
  {
    return coinbase.getTransactions().getTransactions();
  }

  public BigDecimal getExchangeRate() throws IOException, CoinbaseException {
    return coinbase.getExchangeRates().get("btc_to_usd");
  }

  public void sendPayment(Author author, BigDecimal amount, String url)
      throws TransferFailedException
  {
    try {
      String note = "Commit payment:\n__" + author.getUsername() + "__ " + url;

      Transaction transaction = new Transaction();
      transaction.setTo(author.getEmail());
      transaction.setAmount(Money.parse("BTC " + amount.toPlainString()));
      transaction.setNotes(note);

      Transaction response = coinbase.sendMoney(transaction);

      if (response.getStatus() != Transaction.Status.COMPLETE) {
        throw new TransferFailedException();
      }
    } catch (CoinbaseException | IOException e) {
      throw new TransferFailedException(e);
    }
  }

  public BigDecimal getAccountBalance() throws IOException, CoinbaseException {
    List<Account> accounts = coinbase.getAccounts().getAccounts();
    Account       primary  = null;

    for (Account account : accounts) {
      if (account.isPrimary()) {
        primary = account;
        break;
      }
    }

    if (primary != null) return coinbase.getBalance(primary.getId()).getAmount();
    else                 return new BigDecimal(0.0);
  }
}
