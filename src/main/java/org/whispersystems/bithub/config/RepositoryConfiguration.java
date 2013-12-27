package org.whispersystems.bithub.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class RepositoryConfiguration {

  @JsonProperty
  @NotEmpty
  private String url;

  @JsonProperty
  @NotEmpty
  private String mode = "MONEYMONEY";

  public RepositoryConfiguration(String url, String mode) {
    this.url  = url;
    this.mode = mode;
  }

  public RepositoryConfiguration(String url) {
    this.url = url;
  }

  public RepositoryConfiguration() {}

  public String getUrl() {
    return url;
  }

  public String getMode() {
    return mode;
  }
}
