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

package org.whispersystems.bithub.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class GithubConfiguration {

  private final Logger logger = LoggerFactory.getLogger(GithubConfiguration.class);

  @JsonProperty
  @NotEmpty
  private String user;

  @JsonProperty
  @NotEmpty
  private String token;

  @JsonProperty
  private List<RepositoryConfiguration> repositories;

  @JsonProperty
  private String repositories_heroku;

  @Valid
  @NotNull
  @JsonProperty
  private WebhookConfiguration webhook;

  public String getUser() {
    return user;
  }

  public String getToken() {
    return token;
  }

  public List<RepositoryConfiguration> getRepositories() {
    if (repositories != null) {
      return repositories;
    }

    if (repositories_heroku != null) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(repositories_heroku, new TypeReference<List<RepositoryConfiguration>>() {});
      } catch (IOException e) {
        logger.warn("Error deserializing", e);
      }
    }

    return new LinkedList<>();
  }

  public WebhookConfiguration getWebhookConfiguration() {
    return webhook;
  }
}
