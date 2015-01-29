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

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.whispersystems.bithub.auth.GithubWebhookAuthenticator;
import org.whispersystems.bithub.client.CoinbaseClient;
import org.whispersystems.bithub.client.GithubClient;
import org.whispersystems.bithub.config.CoinbaseConfiguration;
import org.whispersystems.bithub.config.RepositoryConfiguration;
import org.whispersystems.bithub.controllers.DashboardController;
import org.whispersystems.bithub.controllers.GithubController;
import org.whispersystems.bithub.controllers.StatusController;
import org.whispersystems.bithub.mappers.IOExceptionMapper;
import org.whispersystems.bithub.mappers.UnauthorizedHookExceptionMapper;
import org.whispersystems.bithub.storage.CacheManager;

import javax.servlet.DispatcherType;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

import io.dropwizard.Application;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

/**
 * The main entry point for the service.
 *
 * @author Moxie Marlinspike
 */
public class BithubService extends Application<BithubServerConfiguration> {

  @Override
  public void initialize(Bootstrap<BithubServerConfiguration> bootstrap) {
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
    String                        organizationName   = config.getOrganizationConfiguration().getName();
    String                        donationUrl        = config.getOrganizationConfiguration().getDonationUrl().toExternalForm();
    String                        coinbaseApiKey     = config.getCoinbaseConfiguration().getApiKey();
    String                        coinbaseApiSecret  = config.getCoinbaseConfiguration().getApiSecret();

    GithubClient   githubClient   = new GithubClient(githubUser, githubToken);
    CoinbaseClient coinbaseClient = new CoinbaseClient(coinbaseApiKey, coinbaseApiSecret);
    CacheManager   cacheManager   = new CacheManager(coinbaseClient, githubClient, githubRepositories, payoutRate);

    environment.servlets().addFilter("CORS", CrossOriginFilter.class)
               .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

    environment.lifecycle().manage(cacheManager);

    environment.jersey().register(new GithubController(githubRepositories, githubClient, coinbaseClient, payoutRate));
    environment.jersey().register(new StatusController(cacheManager, githubRepositories));
    environment.jersey().register(new DashboardController(organizationName, donationUrl, cacheManager));

    environment.jersey().register(new IOExceptionMapper());
    environment.jersey().register(new UnauthorizedHookExceptionMapper());
    environment.jersey().register(new BasicAuthProvider<>(new GithubWebhookAuthenticator(githubWebhookUser, githubWebhookPwd),
                                                          GithubWebhookAuthenticator.REALM));
  }

  public static void main(String[] args) throws Exception {
    new BithubService().run(args);
  }

}
