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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.metrics.annotation.Timed;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.bithub.client.CoinbaseClient;
import org.whispersystems.bithub.client.GithubClient;
import org.whispersystems.bithub.client.TransferFailedException;
import org.whispersystems.bithub.entities.Commit;
import org.whispersystems.bithub.entities.PushEvent;
import org.whispersystems.bithub.entities.Repository;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Handles incoming API calls from GitHub.  These are currently only
 * PushEvent webhooks.
 *
 * @author Moxie Marlinspike
 */
@Path("/v1/github")
public class GithubController {

  private static final String GITHUB_WEBOOK_CIDR = "192.30.252.0/22";

  private final Logger     logger         = LoggerFactory.getLogger(GithubController.class);
  private final SubnetInfo trustedNetwork = new SubnetUtils(GITHUB_WEBOOK_CIDR).getInfo();

  private final CoinbaseClient coinbaseClient;
  private final GithubClient   githubClient;
  private final Set<String>    repositories;
  private final BigDecimal     payoutRate;

  public GithubController(List<String> repositories,
                          GithubClient githubClient,
                          CoinbaseClient coinbaseClient,
                          BigDecimal payoutRate)
  {
    this.coinbaseClient = coinbaseClient;
    this.githubClient   = githubClient;
    this.repositories   = new HashSet<>();
    this.payoutRate     = payoutRate;

    for (String repository : repositories) {
      this.repositories.add(repository.toLowerCase());
    }
  }

  @Timed
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("/commits/")
  public void handleCommits(@HeaderParam("X-Forwarded-For") String clientIp,
                            @FormParam("payload")           String eventString)
      throws IOException, UnauthorizedHookException
  {
    authenticate(clientIp);
    PushEvent event = getEventFromPayload(eventString);

    if (!repositories.contains(event.getRepository().getUrl().toLowerCase())) {
      throw new UnauthorizedHookException("Not a valid repository: " + event.getRepository().getUrl());
    }

    Repository   repository   = event.getRepository();
    List<Commit> commits      = getQualifyingCommits(event);
    BigDecimal   balance      = coinbaseClient.getAccountBalance();
    BigDecimal   exchangeRate = coinbaseClient.getExchangeRate();

    logger.info("Retrieved balance: " + balance.toPlainString());

    sendPaymentsFor(repository, commits, balance, exchangeRate);
  }


  private void sendPaymentsFor(Repository repository, List<Commit> commits,
                               BigDecimal balance, BigDecimal exchangeRate)
  {
    for (Commit commit : commits) {
      try {
        BigDecimal payout = balance.multiply(payoutRate);

        if (isViablePaymentAmount(payout)) {
          coinbaseClient.sendPayment(commit.getAuthor(), payout, commit.getUrl());
        }

        balance = balance.subtract(payout);

        githubClient.addCommitComment(repository, commit,
                                      getCommitCommentStringForPayment(payout, exchangeRate));
      } catch (TransferFailedException e) {
        logger.warn("Transfer failed", e);
      }
    }
  }

  private PushEvent getEventFromPayload(String payload) throws IOException {
    ObjectMapper     objectMapper = new ObjectMapper();
    PushEvent        event        = objectMapper.readValue(payload, PushEvent.class);
    ValidatorFactory factory      = Validation.buildDefaultValidatorFactory();
    Validator        validator    = factory.getValidator();

    validator.validate(event);
    return event;
  }

  private List<Commit> getQualifyingCommits(PushEvent event) {
    List<Commit> commits = new LinkedList<>();
    Set<String>  emails  = new HashSet<>();

    for (Commit commit : event.getCommits()) {
      logger.info(commit.getUrl());
      if (!emails.contains(commit.getAuthor().getEmail())) {
        logger.info("Unique author: "+ commit.getAuthor().getEmail());
        if (commit.getMessage() == null || !commit.getMessage().startsWith("Merge")) {
          logger.info("Not a merge commit...");
          emails.add(commit.getAuthor().getEmail());
          commits.add(commit);
        }
      }
    }

    return commits;
  }

  private boolean isViablePaymentAmount(BigDecimal payment) {
    return payment.compareTo(new BigDecimal(0)) == 1;
  }

  private String getCommitCommentStringForPayment(BigDecimal payment, BigDecimal exchangeRate) {
    if (isViablePaymentAmount(payment)) {
      BigDecimal paymentUsd = payment.multiply(exchangeRate).setScale(2, RoundingMode.CEILING);
      return "Thanks! BitHub has sent payment of  $" + paymentUsd.toPlainString() + "USD for this commit.";
    } else {
      return "Thanks! Unfortunately our BitHub balance is $0.00, so no payout can be made.";
    }
  }

  private void authenticate(String clientIp) throws UnauthorizedHookException {
    if (clientIp == null) {
      throw new UnauthorizedHookException("No X-Forwarded-For!");
    }

    if (!trustedNetwork.isInRange(clientIp)) {
      throw new UnauthorizedHookException("Untrusted IP: " + clientIp);
    }
 }
}
