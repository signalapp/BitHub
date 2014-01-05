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

package org.whispersystems.bithub;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.auth.basic.BasicAuthProvider;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;
import org.whispersystems.bithub.auth.GithubWebhookAuthenticator;
import org.whispersystems.bithub.client.CoinbaseClient;
import org.whispersystems.bithub.client.GithubClient;
import org.whispersystems.bithub.config.RepositoryConfiguration;
import org.whispersystems.bithub.controllers.GithubController;
import org.whispersystems.bithub.controllers.StatusController;
import org.whispersystems.bithub.filters.CorsHeaderFilter;
import org.whispersystems.bithub.mappers.IOExceptionMapper;
import org.whispersystems.bithub.mappers.UnauthorizedHookExceptionMapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * The main entry point for the service.
 *
 * @author Moxie Marlinspike
 */
public class BithubService extends Service<BithubServerConfiguration> {

  @Override
  public void initialize(Bootstrap<BithubServerConfiguration> bootstrap) {
    bootstrap.setName("bithub-server");
    bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
    bootstrap.addBundle(new ViewBundle());
  }

  @Override
  public void run(BithubServerConfiguration config, Environment environment)
      throws Exception
  {
    String                        githubUser         = config.getGithubConfiguration().getUser();
    String                        githubToken        = config.getGithubConfiguration().getToken();
    String                        githubWebhookUser  = config.getGithubConfiguration().getWebhookConfiguration().getUsername();
    String                        githubWebhookPwd   = config.getGithubConfiguration().getWebhookConfiguration().getPassword();
    List<RepositoryConfiguration> githubRepositories = config.getGithubConfiguration().getRepositories();
    BigDecimal                    payoutRate         = config.getBithubConfiguration().getPayoutRate();
    GithubClient                  githubClient       = new GithubClient(githubUser, githubToken);
    CoinbaseClient                coinbaseClient     = new CoinbaseClient(config.getCoinbaseConfiguration().getApiKey());

    environment.addFilter(new CorsHeaderFilter(), "/v1/status/*");
    environment.addResource(new GithubController(githubRepositories, githubClient, coinbaseClient, payoutRate));
    environment.addResource(new StatusController(coinbaseClient, payoutRate));
    environment.addProvider(new IOExceptionMapper());
    environment.addProvider(new UnauthorizedHookExceptionMapper());
    environment.addProvider(new BasicAuthProvider<>(
      new GithubWebhookAuthenticator(githubWebhookUser, githubWebhookPwd), GithubWebhookAuthenticator.REALM));
  }

  public static void main(String[] args) throws Exception {
    new BithubService().run(args);
  }

}
