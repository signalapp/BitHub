package org.whispersystems.bithub.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {

  @JsonProperty
  private String destination;

  @JsonProperty
  private String amount;
  
  @JsonProperty
  private String amountBtc;

  @JsonProperty
  private String commitUrl;

  @JsonProperty
  private String commitSha;

  @JsonProperty
  private String timestamp;

  @JsonProperty
  private String description;

  public Transaction() {}

  public Transaction(String destination, String amount, String amountBtc, String commitUrl,
                     String commitSha, String timestamp, String description)
  {
    this.destination = destination;
    this.amount      = amount;
    this.amountBtc   = amountBtc;
    this.commitUrl   = commitUrl;
    this.commitSha   = commitSha;
    this.timestamp   = timestamp;
    this.description = description;
  }

  public String getDestination() {
    return destination;
  }

  public String getAmount() {
    return amount;
  }
  
  public String getAmountBtc() {
    return amountBtc;
  }

  public String getCommitUrl() {
    return commitUrl;
  }

  public String getCommitSha() {
    return commitSha;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getDescription() {
    return description;
  }

}
