package org.whispersystems.bithub.tests.controllers;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.ClassRule;
import org.junit.Test;
import org.whispersystems.bithub.client.CoinbaseClient;
import org.whispersystems.bithub.client.GithubClient;
import org.whispersystems.bithub.config.RepositoryConfiguration;
import org.whispersystems.bithub.controllers.StatusController;
import org.whispersystems.bithub.entities.CoinbseRecentTransactionsResponse;
import org.whispersystems.bithub.storage.CacheManager;

import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.LinkedList;

import io.dropwizard.testing.junit.ResourceTestRule;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.whispersystems.bithub.tests.util.JsonHelper.fromJson;
import static org.whispersystems.bithub.tests.util.JsonHelper.jsonFixture;

public class StatusControllerTest {

  private static final BigDecimal PAYOUT_RATE   = new BigDecimal(0.02 );
  private static final BigDecimal BALANCE       = new BigDecimal(10.01);
  private static final BigDecimal EXCHANGE_RATE = new BigDecimal(1.0  );

  private static final CoinbaseClient coinbaseClient = mock(CoinbaseClient.class);
  private static final GithubClient   githubClient   = mock(GithubClient.class  );

  @ClassRule
  public static ResourceTestRule resources;

  static {
    try {
      when(coinbaseClient.getRecentTransactions()).thenReturn(fromJson(jsonFixture("payloads/transactions.json"), CoinbseRecentTransactionsResponse.class).getTransactions());
      when(coinbaseClient.getAccountBalance()).thenReturn(BALANCE);
      when(coinbaseClient.getExchangeRate()).thenReturn(EXCHANGE_RATE);

      CacheManager coinbaseManager = new CacheManager(coinbaseClient, githubClient,
                                                      new LinkedList<RepositoryConfiguration>(),
                                                      PAYOUT_RATE);
      coinbaseManager.start();

      resources = ResourceTestRule.builder()
                                  .addResource(new StatusController(coinbaseManager, null))
                                  .build();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

//  @Before
//  public void setup() throws Exception {
//    when(coinbaseClient.getRecentTransactions()).thenReturn(fromJson(jsonFixture("payloads/transactions.json"), RecentTransactionsResponse.class).getTransactions());
//    when(coinbaseClient.getAccountBalance()).thenReturn(BALANCE);
//    when(coinbaseClient.getExchangeRate()).thenReturn(EXCHANGE_RATE);
//
//  }

//  @Test
//  public void testTransactionsHtml() throws Exception {
//    ClientResponse response = resources.client().resource("/v1/status/transactions/")
//        .get(ClientResponse.class);
//
//    assertThat(response.getStatus()).isEqualTo(200);
//    assertThat(response.getType()).isEqualTo(MediaType.TEXT_HTML_TYPE);
//  }

  @Test
  public void testTransactionsJson() throws Exception {
    ClientResponse response = resources.client().resource("/v1/status/transactions/?format=json").accept(MediaType.APPLICATION_JSON_TYPE)
        .get(ClientResponse.class);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
  }

}
