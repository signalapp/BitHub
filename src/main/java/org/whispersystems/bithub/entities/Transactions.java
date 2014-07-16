package org.whispersystems.bithub.entities;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Transactions {

  @JsonProperty
  private List<Transaction> transactions;

  public Transactions() {}

  public Transactions(List<Transaction> transactions) {
    this.transactions = transactions;
  }

}
