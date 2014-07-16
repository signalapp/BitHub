package org.whispersystems.bithub.controllers;

import com.codahale.metrics.annotation.Timed;
import org.whispersystems.bithub.storage.CacheManager;
import org.whispersystems.bithub.views.DashboardView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DashboardController {

  private final CacheManager cacheManager;
  private final String       organizationName;
  private final String       donationUrl;



  public DashboardController(String organizationName, String donationUrl,
                             CacheManager cacheManager)
  {
    this.organizationName = organizationName;
    this.donationUrl      = donationUrl;
    this.cacheManager     = cacheManager;
  }

  @Timed
  @GET
  @Produces(MediaType.TEXT_HTML)
  public DashboardView getDashboard() {
    return new DashboardView(organizationName, donationUrl,
                             cacheManager.getCurrentPaymentAmount(),
                             cacheManager.getRepositories(),
                             cacheManager.getRecentTransactions());
  }

}
