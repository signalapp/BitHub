package org.whispersystems.bithub.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.net.URL;

public class OrganizationConfiguration {

  @JsonProperty
  @NotEmpty
  private String name;

  @JsonProperty
  @Valid
  private URL donationUrl;

  public String getName() {
    return name;
  }

  public URL getDonationUrl() {
    return donationUrl;
  }
}
