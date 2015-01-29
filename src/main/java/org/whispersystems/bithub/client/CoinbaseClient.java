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
import org.apache.commons.codec.binary.Hex;
import org.codehaus.jackson.map.ObjectMapper;
import org.whispersystems.bithub.entities.Author;
import org.whispersystems.bithub.entities.BalanceResponse;
import org.whispersystems.bithub.entities.BitcoinTransaction;
import org.whispersystems.bithub.entities.BitcoinTransactionResponse;
import org.whispersystems.bithub.entities.CoinbaseTransaction;
import org.whispersystems.bithub.entities.CoinbseRecentTransactionsResponse;
import org.whispersystems.bithub.entities.ExchangeRate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Handles interaction with the Coinbase API.
 *
 * @author Moxie Marlinspike
 */
public class CoinbaseClient {

  private static final String COINBASE_URL             = "https://coinbase.com";
  private static final String BALANCE_PATH             = "/api/v1/account/balance";
  private static final String PAYMENT_PATH             = "/api/v1/transactions/send_money";
  private static final String EXCHANGE_PATH            = "/api/v1/currencies/exchange_rates";
  private static final String RECENT_TRANSACTIONS_PATH = "/api/v1/transactions";

  private final String apiKey;
  private final String apiSecret;
  private final Client client;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public CoinbaseClient(String apiKey, String apiSecret) {
    this.apiKey    = apiKey;
    this.apiSecret = apiSecret;
    this.client    = Client.create(getClientConfig());
  }

  public List<CoinbaseTransaction> getRecentTransactions()
      throws IOException, TransferFailedException
  {
    try {
      return getAuthenticatedWebResource(RECENT_TRANSACTIONS_PATH, null).get(CoinbseRecentTransactionsResponse.class)
                                                                        .getTransactions();
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
      String note = "Commit payment:\n__" + author.getUsername() + "__ " + url;

      BitcoinTransaction transaction = new BitcoinTransaction(author.getEmail(),
                                                              amount.toPlainString(),
                                                              note);

      WebResource.Builder resource = getAuthenticatedWebResource(PAYMENT_PATH, transaction);

      BitcoinTransactionResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE)
                                                    .entity(transaction)
                                                    .post(BitcoinTransactionResponse.class);

      if (!response.isSuccess()) {
        throw new TransferFailedException();
      }

    } catch (UniformInterfaceException | ClientHandlerException e) {
      throw new TransferFailedException(e);
    }
  }

  public BigDecimal getAccountBalance() throws IOException, TransferFailedException {
    try {
      WebResource.Builder resource = getAuthenticatedWebResource(BALANCE_PATH, null);
      String amount = resource.get(BalanceResponse.class)
                              .getAmount();
      if (amount == null) {
        throw new IOException("Empty amount in response!");
      }

      return new BigDecimal(amount);
    } catch (UniformInterfaceException | ClientHandlerException e) {
      throw new IOException(e);
    }
  }

  private WebResource.Builder getAuthenticatedWebResource(String path, Object body) throws TransferFailedException {
    try {
      String json    = body == null ? "" : objectMapper.writeValueAsString(body);
      String nonce   = String.valueOf(System.currentTimeMillis());
      String message = nonce + COINBASE_URL + path + json;
      Mac    mac     = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256"));

      String signature = new String(Hex.encodeHex(mac.doFinal(message.getBytes())));

      return client.resource(COINBASE_URL)
              .path(path)
              .accept(MediaType.APPLICATION_JSON)
              .header("ACCESS_SIGNATURE", signature)
              .header("ACCESS_NONCE", nonce)
              .header("ACCESS_KEY", apiKey);
    } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {
      throw new TransferFailedException();
    }
  }

  private ClientConfig getClientConfig() {
    ClientConfig config = new DefaultClientConfig();
    config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

    return config;
  }

}
