package org.whispersystems.bithub.views;

import org.whispersystems.bithub.entities.Repository;
import org.whispersystems.bithub.entities.Transaction;
import org.whispersystems.bithub.storage.CurrentPayment;

import java.util.List;

import io.dropwizard.views.View;

public class DashboardView extends View {

  private final String            organizationName;
  private final String            donationUrl;
  private final CurrentPayment    currentPayment;
  private final List<Repository>  repositories;
  private final List<Transaction> transactions;

  public DashboardView(String organizationName, String donationUrl,
                       CurrentPayment currentPayment,
                       List<Repository> repositories,
                       List<Transaction> transactions)
  {
    super("dashboard.mustache");
    this.organizationName = organizationName;
    this.donationUrl      = donationUrl;
    this.currentPayment   = currentPayment;
    this.repositories     = repositories;
    this.transactions     = transactions;
  }

  public String getPayment() {
    return currentPayment.getEntity().getPayment();
  }

  public String getOrganizationName() {
    return organizationName;
  }

  public String getDonationUrl() {
    return donationUrl;
  }

  public List<Repository> getRepositories() {
    return repositories;
  }

  public List<Transaction> getTransactions() {
    return transactions;
  }

  public String getRepositoriesCount() {
    return String.valueOf(repositories.size());
  }

}
