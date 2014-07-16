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

package org.whispersystems.bithub.controllers;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.bithub.config.RepositoryConfiguration;
import org.whispersystems.bithub.entities.Repositories;
import org.whispersystems.bithub.entities.Repository;
import org.whispersystems.bithub.entities.Transaction;
import org.whispersystems.bithub.entities.Transactions;
import org.whispersystems.bithub.storage.CacheManager;
import org.whispersystems.bithub.storage.CurrentPayment;
import org.whispersystems.bithub.views.TransactionsView;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.dropwizard.jersey.caching.CacheControl;

/**
 * Handles incoming API calls for BitHub instance status information.
 *
 * @author Moxie Marlinspike
 */
@Path("/v1/status")
public class StatusController {

  private final Logger logger = LoggerFactory.getLogger(StatusController.class);

  private final List<RepositoryConfiguration> repositoryConfiguration;
  private final CacheManager coinbaseManager;

  public StatusController(CacheManager coinbaseManager,
                          List<RepositoryConfiguration> repositoryConfiguration)
      throws IOException
  {
    this.coinbaseManager         = coinbaseManager;
    this.repositoryConfiguration = repositoryConfiguration;
  }

  @Timed
  @GET
  @Path("/transactions")
  public Response getTransactions(@QueryParam("format") @DefaultValue("html") String format)
        throws IOException
  {
    List<Transaction> recentTransactions = coinbaseManager.getRecentTransactions();

    switch (format) {
      case "html": return Response.ok(new TransactionsView(recentTransactions), MediaType.TEXT_HTML_TYPE).build();
      case "json":
      default:     return Response.ok(new Transactions(recentTransactions), MediaType.APPLICATION_JSON_TYPE).build();
    }
  }

  @Timed
  @GET
  @Path("/repositories")
  @Produces(MediaType.APPLICATION_JSON)
  public Repositories getRepositories() {
    List<Repository> repositories = new LinkedList<>();

    for (RepositoryConfiguration configuration : repositoryConfiguration) {
      repositories.add(new Repository(configuration.getUrl()));
    }

    return new Repositories(repositories);
  }


  @Timed
  @GET
  @Path("/payment/commit")
  @CacheControl(noCache = true)
  public Response getCurrentCommitPrice(@QueryParam("format") @DefaultValue("png") String format)
      throws IOException
  {
    CurrentPayment currentPayment = coinbaseManager.getCurrentPaymentAmount();

    switch (format) {
      case "json":
        return Response.ok(currentPayment.getEntity(), MediaType.APPLICATION_JSON_TYPE).build();
      case "png_small":
        return Response.ok(currentPayment.getSmallBadge(), "image/png").build();
      default:
        return Response.ok(currentPayment.getBadge(), "image/png").build();
    }
  }
}
