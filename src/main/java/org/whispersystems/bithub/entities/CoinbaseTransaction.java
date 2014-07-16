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
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinbaseTransaction {

  @JsonProperty(value = "created_at")
  @NotEmpty
  private String createdTime;

  @JsonProperty
  @NotNull
  private Amount amount;

  @JsonProperty(value = "recipient_address")
  private String recipientAddress;

  @JsonProperty
  private String notes;

  public String getCreatedTime() {
    return createdTime;
  }

  public String getAmount() {
    return amount.getAmount();
  }

  public String getRecipientAddress() {
    return recipientAddress;
  }

  public String getNotes() {
    return notes;
  }

  public boolean isSentTransaction() {
    BigDecimal amount = new BigDecimal(getAmount());
    return amount.compareTo(new BigDecimal(0.0)) < 0;
  }

}
