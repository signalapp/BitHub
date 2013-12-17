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
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;
import org.whispersystems.bithub.client.CoinbaseClient;
import org.whispersystems.bithub.client.GithubClient;
import org.whispersystems.bithub.client.TransferFailedException;
import org.whispersystems.bithub.controllers.GithubController;
import org.whispersystems.bithub.entities.Author;
import org.whispersystems.bithub.mappers.UnauthorizedHookExceptionMapper;

import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.io.InputStream;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class GithubControllerTest extends ResourceTest {

  private static final BigDecimal BALANCE = new BigDecimal(10.01);
  private static final BigDecimal EXCHANGE_RATE = new BigDecimal(1.0);

  private final CoinbaseClient coinbaseClient = mock(CoinbaseClient.class);
  private final GithubClient   githubClient   = mock(GithubClient.class);

  private final List<String> repositories = new LinkedList<String>() {{
    add("https://github.com/moxie0/test");
  }};

  @Override
  protected void setUpResources() throws Exception {
    when(coinbaseClient.getAccountBalance()).thenReturn(BALANCE);
    when(coinbaseClient.getExchangeRate()).thenReturn(EXCHANGE_RATE);
    addResource(new GithubController(repositories, githubClient, coinbaseClient, new BigDecimal(0.02)));
    addProvider(new UnauthorizedHookExceptionMapper());
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
    ClientResponse response = client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void testInvalidOrigin() throws Exception {
    String payloadValue = payload("/payloads/invalid_origin.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.242.1")
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void testOptOutCommit() throws Exception, TransferFailedException {
    String payloadValue = payload("/payloads/opt_out_commit.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    verify(coinbaseClient, never()).sendPayment(any(Author.class),
                                       any(BigDecimal.class),
                                       anyString());
  }

  @Test
  public void testValidCommit() throws Exception, TransferFailedException {
    String payloadValue = payload("/payloads/valid_commit.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.252.1")
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    verify(coinbaseClient).sendPayment(any(Author.class),
                                       eq(BALANCE.multiply(new BigDecimal(0.02))),
                                       anyString());
  }

  @Test
  public void testValidMultipleCommitsMultipleAuthors() throws Exception, TransferFailedException {
    String payloadValue = payload("/payloads/multiple_commits_authors.json");
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = client().resource("/v1/github/commits/").header("X-Forwarded-For", "192.30.252.1")
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, post);

    verify(coinbaseClient, times(1)).sendPayment(any(Author.class), eq(BALANCE.multiply(new BigDecimal(0.02))),
        anyString());
    verify(coinbaseClient, times(1)).sendPayment(any(Author.class), eq(BALANCE.subtract(BALANCE.multiply(new BigDecimal(0.02)))
        .multiply(new BigDecimal(0.02))), anyString());
  }

}
