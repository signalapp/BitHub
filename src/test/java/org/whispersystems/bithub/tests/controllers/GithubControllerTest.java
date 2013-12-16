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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @Test
  public void testInvalidRepository() throws Exception {
    String payloadValue = "{\"ref\":\"refs/heads/master\",\"after\":\"100e9859651b35a3505cc278e9a98a076f79940b\",\"before\":\"6626766348ab245bdb3351989f753bd6e792524a\",\"created\":false,\"deleted\":false,\"forced\":false,\"compare\":\"https://github.com/moxie0/tempt/compare/6626766348ab...100e9859651b\",\"commits\":[{\"id\":\"fd7daeb1de6d72220b1313a7f1112d43885013aa\",\"distinct\":true,\"message\":\"Update foo\",\"timestamp\":\"2013-12-14T11:27:00-08:00\",\"url\":\"https://github.com/moxie0/tempt/commit/fd7daeb1de6d72220b1313a7f1112d43885013aa\",\"author\":{\"name\":\"WhisperBTC\",\"email\":\"info@whispersystems.org\",\"username\":\"WhisperBTC\"},\"committer\":{\"name\":\"WhisperBTC\",\"email\":\"info@whispersystems.org\",\"username\":\"WhisperBTC\"},\"added\":[],\"removed\":[],\"modified\":[\"foo\"]},{\"id\":\"100e9859651b35a3505cc278e9a98a076f79940b\",\"distinct\":true,\"message\":\"Merge pull request #2 from WhisperBTC/patch-2\\n\\nUpdate foo\",\"timestamp\":\"2013-12-14T11:27:28-08:00\",\"url\":\"https://github.com/moxie0/tempt/commit/100e9859651b35a3505cc278e9a98a076f79940b\",\"author\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"committer\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"added\":[],\"removed\":[],\"modified\":[\"foo\"]}],\"head_commit\":{\"id\":\"100e9859651b35a3505cc278e9a98a076f79940b\",\"distinct\":true,\"message\":\"Merge pull request #2 from WhisperBTC/patch-2\\n\\nUpdate foo\",\"timestamp\":\"2013-12-14T11:27:28-08:00\",\"url\":\"https://github.com/moxie0/tempt/commit/100e9859651b35a3505cc278e9a98a076f79940b\",\"author\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"committer\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"added\":[],\"removed\":[],\"modified\":[\"foo\"]},\"repository\":{\"id\":15141344,\"name\":\"tempt\",\"url\":\"https://github.com/moxie0/tempt\",\"description\":\"test\",\"watchers\":1,\"stargazers\":1,\"forks\":1,\"fork\":false,\"size\":216,\"owner\":{\"name\":\"moxie0\",\"email\":\"moxie@thoughtcrime.org\"},\"private\":false,\"open_issues\":0,\"has_issues\":true,\"has_downloads\":true,\"has_wiki\":true,\"created_at\":1386866024,\"pushed_at\":1387049248,\"master_branch\":\"master\"},\"pusher\":{\"name\":\"moxie0\",\"email\":\"moxie@thoughtcrime.org\"}}";
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
    String payloadValue = "{\"ref\":\"refs/heads/master\",\"after\":\"100e9859651b35a3505cc278e9a98a076f79940b\",\"before\":\"6626766348ab245bdb3351989f753bd6e792524a\",\"created\":false,\"deleted\":false,\"forced\":false,\"compare\":\"https://github.com/moxie0/tempt/compare/6626766348ab...100e9859651b\",\"commits\":[{\"id\":\"fd7daeb1de6d72220b1313a7f1112d43885013aa\",\"distinct\":true,\"message\":\"Update foo\",\"timestamp\":\"2013-12-14T11:27:00-08:00\",\"url\":\"https://github.com/moxie0/tempt/commit/fd7daeb1de6d72220b1313a7f1112d43885013aa\",\"author\":{\"name\":\"WhisperBTC\",\"email\":\"info@whispersystems.org\",\"username\":\"WhisperBTC\"},\"committer\":{\"name\":\"WhisperBTC\",\"email\":\"info@whispersystems.org\",\"username\":\"WhisperBTC\"},\"added\":[],\"removed\":[],\"modified\":[\"foo\"]},{\"id\":\"100e9859651b35a3505cc278e9a98a076f79940b\",\"distinct\":true,\"message\":\"Merge pull request #2 from WhisperBTC/patch-2\\n\\nUpdate foo\",\"timestamp\":\"2013-12-14T11:27:28-08:00\",\"url\":\"https://github.com/moxie0/tempt/commit/100e9859651b35a3505cc278e9a98a076f79940b\",\"author\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"committer\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"added\":[],\"removed\":[],\"modified\":[\"foo\"]}],\"head_commit\":{\"id\":\"100e9859651b35a3505cc278e9a98a076f79940b\",\"distinct\":true,\"message\":\"Merge pull request #2 from WhisperBTC/patch-2\\n\\nUpdate foo\",\"timestamp\":\"2013-12-14T11:27:28-08:00\",\"url\":\"https://github.com/moxie0/tempt/commit/100e9859651b35a3505cc278e9a98a076f79940b\",\"author\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"committer\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"added\":[],\"removed\":[],\"modified\":[\"foo\"]},\"repository\":{\"id\":15141344,\"name\":\"tempt\",\"url\":\"https://github.com/moxie0/test\",\"description\":\"test\",\"watchers\":1,\"stargazers\":1,\"forks\":1,\"fork\":false,\"size\":216,\"owner\":{\"name\":\"moxie0\",\"email\":\"moxie@thoughtcrime.org\"},\"private\":false,\"open_issues\":0,\"has_issues\":true,\"has_downloads\":true,\"has_wiki\":true,\"created_at\":1386866024,\"pushed_at\":1387049248,\"master_branch\":\"master\"},\"pusher\":{\"name\":\"moxie0\",\"email\":\"moxie@thoughtcrime.org\"}}";
    MultivaluedMapImpl post = new MultivaluedMapImpl();
    post.add("payload", payloadValue);
    ClientResponse response = client().resource("/v1/github/commits/")
        .header("X-Forwarded-For", "192.30.242.1")
        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class, post);

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void testValidCommit() throws Exception, TransferFailedException {
    String payloadValue = "{\"ref\":\"refs/heads/master\",\"after\":\"bcf09f8b4a32921114587e4814a3f0849aa9900f\",\"before\":\"1b141aa068165dd1ed376f483cd5fdc2c64f32b1\",\"created\":false,\"deleted\":false,\"forced\":false,\"compare\":\"https://github.com/moxie0/tempt/compare/1b141aa06816...bcf09f8b4a32\",\"commits\":[{\"id\":\"ba1b681c71db4fcd461954b1bf344bc6e29411e5\",\"distinct\":true,\"message\":\"Update path\",\"timestamp\":\"2013-12-14T11:42:28-08:00\",\"url\":\"https://github.com/moxie0/tempt/commit/ba1b681c71db4fcd461954b1bf344bc6e29411e5\",\"author\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"committer\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"added\":[],\"removed\":[],\"modified\":[\"README.md\"]},{\"id\":\"bcf09f8b4a32921114587e4814a3f0849aa9900f\",\"distinct\":true,\"message\":\"Merge branch 'master' of github.com:moxie0/tempt\",\"timestamp\":\"2013-12-14T11:42:44-08:00\",\"url\":\"https://github.com/moxie0/tempt/commit/bcf09f8b4a32921114587e4814a3f0849aa9900f\",\"author\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"committer\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"added\":[],\"removed\":[],\"modified\":[]}],\"head_commit\":{\"id\":\"bcf09f8b4a32921114587e4814a3f0849aa9900f\",\"distinct\":true,\"message\":\"Merge branch 'master' of github.com:moxie0/tempt\",\"timestamp\":\"2013-12-14T11:42:44-08:00\",\"url\":\"https://github.com/moxie0/tempt/commit/bcf09f8b4a32921114587e4814a3f0849aa9900f\",\"author\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"committer\":{\"name\":\"Moxie Marlinspike\",\"email\":\"moxie@thoughtcrime.org\",\"username\":\"moxie0\"},\"added\":[],\"removed\":[],\"modified\":[]},\"repository\":{\"id\":15141344,\"name\":\"tempt\",\"url\":\"https://github.com/moxie0/test\",\"description\":\"test\",\"watchers\":1,\"stargazers\":1,\"forks\":1,\"fork\":false,\"size\":216,\"owner\":{\"name\":\"moxie0\",\"email\":\"moxie@thoughtcrime.org\"},\"private\":false,\"open_issues\":0,\"has_issues\":true,\"has_downloads\":true,\"has_wiki\":true,\"created_at\":1386866024,\"pushed_at\":1387050173,\"master_branch\":\"master\"},\"pusher\":{\"name\":\"moxie0\",\"email\":\"moxie@thoughtcrime.org\"}}";
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

}
