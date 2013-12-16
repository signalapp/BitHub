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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import org.whispersystems.bithub.entities.Author;
import org.whispersystems.bithub.entities.BalanceResponse;
import org.whispersystems.bithub.entities.BitcoinTransaction;
import org.whispersystems.bithub.entities.BitcoinTransactionResponse;
import org.whispersystems.bithub.entities.ExchangeRate;
import org.whispersystems.bithub.entities.RecentTransactionsResponse;
import org.whispersystems.bithub.entities.Transaction;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Handles interaction with the Coinbase API.
 *
 * @author Moxie Marlinspike
 */
public class CoinbaseClient {

  private static final String COINBASE_URL             = "https://coinbase.com/";
  private static final String BALANCE_PATH             = "/api/v1/account/balance";
  private static final String PAYMENT_PATH             = "/api/v1/transactions/send_money";
  private static final String EXCHANGE_PATH            = "/api/v1/currencies/exchange_rates";
  private static final String RECENT_TRANSACTIONS_PATH = "/api/v1/transactions";

  private final String apiKey;
  private final Client client;

  public CoinbaseClient(String apiKey) {
    this.apiKey = apiKey;
    this.client = Client.create(getClientConfig());
  }

  public List<Transaction> getRecentTransactions() throws IOException {
    try {
      return client.resource(COINBASE_URL)
                   .path(RECENT_TRANSACTIONS_PATH)
                   .queryParam("api_key", apiKey)
                   .get(RecentTransactionsResponse.class).getTransactions();
    } catch (UniformInterfaceException | ClientHandlerException e) {
      throw new IOException(e);
    }
  }

  public BigDecimal getExchangeRate() throws IOException {
    try {
      WebResource resource = client.resource(COINBASE_URL)
                                   .path(EXCHANGE_PATH);

      String btcToUsd = resource.accept(MediaType.APPLICATION_JSON)
                                .get(ExchangeRate.class)
                                .getBtc_to_usd();

      return new BigDecimal(btcToUsd);
  } catch (UniformInterfaceException | ClientHandlerException e) {
    throw new IOException(e);
  }
}

  public void sendPayment(Author author, BigDecimal amount, String url)
      throws TransferFailedException
  {
    try {
      WebResource resource = client.resource(COINBASE_URL)
                                   .path(PAYMENT_PATH)
                                   .queryParam("api_key", apiKey);

      String note = "Commit payment:\n__" + author.getUsername() + "__ " + url;

      BitcoinTransaction transaction = new BitcoinTransaction(author.getEmail(),
                                                              amount.toPlainString(),
                                                              note);

      boolean success = resource.type(MediaType.APPLICATION_JSON_TYPE)
                                .accept(MediaType.APPLICATION_JSON)
                                .entity(transaction)
                                .post(BitcoinTransactionResponse.class)
                                .isSuccess();

      if (!success) {
        throw new TransferFailedException();
      }

    } catch (UniformInterfaceException | ClientHandlerException e) {
      throw new TransferFailedException(e);
    }
  }

  public BigDecimal getAccountBalance() throws IOException {
    try {
      WebResource resource = client.resource(COINBASE_URL)
                                   .path(BALANCE_PATH)
                                   .queryParam("api_key", apiKey);

      String amount = resource.accept(MediaType.APPLICATION_JSON)
                              .get(BalanceResponse.class)
                              .getAmount();

      if (amount == null) {
        throw new IOException("Empty amount in response!");
      }

      return new BigDecimal(amount);
    } catch (UniformInterfaceException | ClientHandlerException e) {
      throw new IOException(e);
    }
  }

  private ClientConfig getClientConfig() {
    ClientConfig config = new DefaultClientConfig();
    config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

    return config;
  }

}
