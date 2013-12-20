package org.whispersystems.bithub.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class WebhookConfiguration {

  @JsonProperty
  @NotEmpty
  private String username = "bithub";

  @JsonProperty
  @NotEmpty
  private String password;

  public String getUsername() { return username; }

  public String getPassword() { return password; }
}
