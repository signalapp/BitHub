package org.whispersystems.bithub.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DonationData {

  @JsonProperty
  private final String coinbase;

  public DonationData(String coinbaseDataCode) {
    this.coinbase = coinbaseDataCode;
  }
}
