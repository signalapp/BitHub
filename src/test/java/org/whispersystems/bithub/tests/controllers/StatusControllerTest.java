package org.whispersystems.bithub.tests.controllers;

import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.whispersystems.bithub.client.CoinbaseClient;
import org.whispersystems.bithub.controllers.StatusController;
import org.whispersystems.bithub.entities.RecentTransactionsResponse;

import com.sun.jersey.api.client.ClientResponse;
import com.yammer.dropwizard.testing.ResourceTest;
import com.yammer.dropwizard.views.ViewMessageBodyWriter;

public class StatusControllerTest extends ResourceTest {

  private static final BigDecimal PAYOUT_RATE = new BigDecimal(0.02);
  private static final BigDecimal BALANCE = new BigDecimal(10.01);
  private static final BigDecimal EXCHANGE_RATE = new BigDecimal(1.0);

  private final CoinbaseClient coinbaseClient = mock(CoinbaseClient.class);

  @Override
  protected void setUpResources() throws Exception {

    when(coinbaseClient.getRecentTransactions()).thenReturn(fromJson(jsonFixture("payloads/transactions.json"), RecentTransactionsResponse.class).getTransactions());
    when(coinbaseClient.getAccountBalance()).thenReturn(BALANCE);
    when(coinbaseClient.getExchangeRate()).thenReturn(EXCHANGE_RATE);
    addResource(new StatusController(coinbaseClient, PAYOUT_RATE));

    addProvider(ViewMessageBodyWriter.class);
  }

  @Test
  public void testTransactionsHtml() throws Exception {
    ClientResponse response = client().resource("/v1/status/transactions/")
        .get(ClientResponse.class);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getType()).isEqualTo(MediaType.TEXT_HTML_TYPE);
    assertThat(response.getEntity(String.class)).contains("<li>Sent $1.10 USD (1.1000 BTC)");
  }

  @Test
  public void testTransactionsJson() throws Exception {
    ClientResponse response = client().resource("/v1/status/transactions/").queryParam("format", "json")
        .get(ClientResponse.class);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    assertThat(response.getEntity(String.class)).contains("\"amount\":\"1.10\",\"amountInBTC\":\"1.1000\"");
  }

}
