package org.whispersystems.bithub.controllers;

import com.yammer.metrics.annotation.Timed;
import org.whispersystems.bithub.BithubServerConfiguration;
import org.whispersystems.bithub.config.RepositoryConfiguration;
import org.whispersystems.bithub.entities.DonationData;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles incoming API calls for BitHub instance configuration information.
 */
@Path("/v1/config")
public class ConfigController {

  private final BithubServerConfiguration configuration;

  public ConfigController(BithubServerConfiguration configuration) {
    this.configuration = configuration;
  }

  @Timed
  @GET
  @Path("/donations")
  public Response getDonations()
    throws IOException
  {
    DonationData info = new DonationData(configuration.getCoinbaseConfiguration().getDonationDataCode());
    return Response.ok(info, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Timed
  @GET
  @Path("/repositories")
  public Response getRepositories()
      throws IOException
  {
    List<RepositoryConfiguration> repoConfigs = configuration.getGithubConfiguration().getRepositories();
    List<String> repoUrls = new ArrayList<>(repoConfigs.size());
    for (RepositoryConfiguration repoConfig : repoConfigs) {
      repoUrls.add(repoConfig.getUrl());
    }
    return Response.ok(repoUrls, MediaType.APPLICATION_JSON_TYPE).build();
  }

}
