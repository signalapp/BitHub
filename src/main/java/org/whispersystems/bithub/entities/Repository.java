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

package org.whispersystems.bithub.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {

  @JsonProperty
  @NotEmpty
  private String url;

  @JsonProperty
  @NotEmpty
  private String html_url;

  @JsonProperty
  @NotNull
  private Author owner;

  @JsonProperty
  @NotEmpty
  private String name;

  @JsonProperty
  private String description;

  public Repository() {}

  public Repository(String url) {
    this.url = url;
  }

  public Author getOwner() {
    return owner;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public String getHtmlUrl() {
    return html_url;
  }

  public String getDescription() {
    return description;
  }
}
