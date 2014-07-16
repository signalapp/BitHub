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

package org.whispersystems.bithub.tests.controllers;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.whispersystems.bithub.auth.GithubWebhookAuthenticator;
import org.whispersystems.bithub.client.CoinbaseClient;
import org.whispersystems.bithub.client.GithubClient;
import org.whispersystems.bithub.client.TransferFailedException;
import org.whispersystems.bithub.config.RepositoryConfiguration;
import org.whispersystems.bithub.controllers.GithubController;
import org.whispersystems.bithub.entities.Author;
import org.whispersystems.bithub.mappers.UnauthorizedHookExceptionMapper;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class GithubControllerTest {

  private static final BigDecimal BALANCE = new BigDecimal(10.01);
  private static final BigDecimal EXCHANGE_RATE = new BigDecimal(1.0);

  private final CoinbaseClient coinbaseClient = mock(CoinbaseClient.class);
  private final GithubClient   githubClient   = mock(GithubClient.class);

  // HTTP Basic Authentication data
  private final String authUsername = "TestUser";
  private final String authPassword = "TestPassword";
  private final String authRealm = GithubWebhookAuthenticator.REALM;
  private final String authString = "Basic " + Base64.encodeBase64String((authUsername + ":" + authPassword).getBytes());
  private final String invalidUserAuthString = "Basic " + Base64.encodeBase64(("wrong:" + authPassword).getBytes());
  private final String invalidPasswordAuthString = "Basic " + Base64.encodeBase64((authUsername + ":wrong").getBytes());

  private final List<RepositoryConfiguration> repositories = new LinkedList<RepositoryConfiguration>() {{
    add(new RepositoryConfiguration("https://github.com/moxie0/test"));
    add(new RepositoryConfiguration("https://github.com/moxie0/optin", "FREEBIE"));
  }};

  @Rule
  public final ResourceTestRule resources = ResourceTestRule.builder()
                                                            .addProvider(new UnauthorizedHookExceptionMapper())
                                                            .addProvider(new BasicAuthProvider<>(new GithubWebhookAuthenticator(authUsername, authPassword), authRealm))
                                                            .addResource(new GithubController(repositories, githubClient, coinbaseClient, new BigDecimal(0.02)))
                                                            .build();


  @Before
  public void setup() throws Exception {
    when(coinbaseClient.getAccountBalance()).thenReturn(BALANCE);
    when(coinbaseClient.getExchangeRate()).thenReturn(EXCHANGE_RATE);
  }

  protected String payload(String path) {
    InputStream is = this.getClass().getResourceAsStream(path);
    Scanner s = new Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  @Test
  public void testInvalidRepository() throws Exception {
    String payloadValue = payload("/payloads/invalid_repo.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = resources.client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .header("Authorization", authString)
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void testInvalidOrigin() throws Exception {
    String payloadValue = payload("/payloads/invalid_origin.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = resources.client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.242.1")
        .header("Authorization", authString)
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void testMissingAuth() throws Exception, TransferFailedException {
      String payloadValue = payload("/payloads/valid_commit.json");
      MultivaluedMapImpl post = new MultivaluedMapImpl();
      post.add("payload", payloadValue);
      ClientResponse response = resources.client().resource("/v1/github/commits/")
              .header("X-Forwarded-For", "192.30.252.1")
              .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
              .post(ClientResponse.class, post);

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void testInvalidAuthUser() throws Exception, TransferFailedException {
      String payloadValue = payload("/payloads/valid_commit.json");
      MultivaluedMapImpl post = new MultivaluedMapImpl();
      post.add("payload", payloadValue);
      ClientResponse response = resources.client().resource("/v1/github/commits/")
              .header("X-Forwarded-For", "192.30.252.1")
              .header("Authorization", invalidUserAuthString)
              .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
              .post(ClientResponse.class, post);

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void testInvalidAuthPassword() throws Exception, TransferFailedException {
      String payloadValue = payload("/payloads/valid_commit.json");
      MultivaluedMapImpl post = new MultivaluedMapImpl();
      post.add("payload", payloadValue);
      ClientResponse response = resources.client().resource("/v1/github/commits/")
              .header("X-Forwarded-For", "192.30.252.1")
              .header("Authorization", invalidPasswordAuthString)
              .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
              .post(ClientResponse.class, post);

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void testOptOutCommit() throws Exception, TransferFailedException {
    String payloadValue = payload("/payloads/opt_out_commit.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = resources.client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .header("Authorization", authString)
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    verify(coinbaseClient, never()).sendPayment(any(Author.class),
                                       anyString(),
                                       any(BigDecimal.class),
                                       anyString());
  }

  @Test
  public void testCommitSendToLine() throws Exception, TransferFailedException {
    String payloadValue = payload("/payloads/commit_send_to_line.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .header("Authorization", authString)
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    verify(coinbaseClient).sendPayment(any(Author.class),
                                       eq("1PRmBDjTcgjR13FPMQ3m4fLhTxo3s4tCkg"),
                                       any(BigDecimal.class),
                                       anyString());
  }

  @Test
  public void testValidCommit() throws Exception, TransferFailedException {
    String payloadValue = payload("/payloads/valid_commit.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = resources.client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .header("Authorization", authString)
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    verify(coinbaseClient).sendPayment(any(Author.class),
                                       anyString(),
                                       eq(BALANCE.multiply(new BigDecimal(0.02))),
                                       anyString());
  }

  @Test
  public void testNonMaster() throws Exception, TransferFailedException {
    String payloadValue = payload("/payloads/non_master_push.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = resources.client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .header("Authorization", authString)
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    verify(coinbaseClient, never()).sendPayment(any(Author.class),
                                       anyString(),
                                       eq(BALANCE.multiply(new BigDecimal(0.02))),
                                       anyString());
  }

  @Test
  public void testValidMultipleCommitsMultipleAuthors() throws Exception, TransferFailedException {
    String payloadValue = payload("/payloads/multiple_commits_authors.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = resources.client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .header("Authorization", authString)
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    verify(coinbaseClient, times(1)).sendPayment(any(Author.class), anyString(), eq(BALANCE.multiply(new BigDecimal(0.02))),
        anyString());
    verify(coinbaseClient, times(1)).sendPayment(any(Author.class), anyString(), eq(BALANCE.subtract(BALANCE.multiply(new BigDecimal(0.02)))
        .multiply(new BigDecimal(0.02))), anyString());
  }

  @Test
  public void testOptInCommit() throws Exception, TransferFailedException {
    String payloadValue = payload("/payloads/opt_in_commit.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = resources.client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .header("Authorization", authString)
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    verify(coinbaseClient).sendPayment(any(Author.class),
                                       anyString(),
                                       eq(BALANCE.multiply(new BigDecimal(0.02))),
                                       anyString());
  }

  @Test
  public void testNoOptInCommit() throws Exception, TransferFailedException {
    String payloadValue = payload("/payloads/no_opt_in_commit.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = resources.client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .header("Authorization", authString)
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    verify(coinbaseClient, never()).sendPayment(any(Author.class),
                                                anyString(),
                                                any(BigDecimal.class),
                                                anyString());
  }


}
