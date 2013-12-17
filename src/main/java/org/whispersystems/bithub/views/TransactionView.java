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

package org.whispersystems.bithub.views;

import org.apache.commons.lang3.StringEscapeUtils;
import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * A rendered HTML view of an individual BitHub transaction.
 *
 * @author Moxie Marlinspike
 */
public class TransactionView {

  private final Logger logger = LoggerFactory.getLogger(RecentTransactionsView.class);

  private final String destination;
  private final String amount;
  private final String amountInBTC;
  private final String commitUrl;
  private final String commitSha;
  private final String timestamp;

  public TransactionView(BigDecimal exchangeRate, String transactionAmount,
                         String timestamp, String message)
      throws ParseException
  {
    this.amount      = getAmountInDollars(exchangeRate, transactionAmount);
    this.amountInBTC   = getAmountInBTC(transactionAmount);
    this.destination = parseDestinationFromMessage(message);
    this.timestamp   = parseTimestamp(timestamp);
    this.commitUrl   = parseUrlFromMessage(message);
    this.commitSha   = parseShaFromUrl(commitUrl);
  }

  private String getAmountInDollars(BigDecimal exchangeRate, String amount) {
    return new BigDecimal(amount).abs()
                                 .multiply(exchangeRate)
                                 .setScale(2, RoundingMode.CEILING)
                                 .toPlainString();
  }

  private String getAmountInBTC(String amount) {
    return new BigDecimal(amount).abs()
                                 .setScale(4, RoundingMode.CEILING)
                                 .toPlainString();
  }

  private String parseTimestamp(String timestamp) throws ParseException {
    int    offendingColon = timestamp.lastIndexOf(':');
    String fixedTimestamp = timestamp.substring(0, offendingColon) + timestamp.substring(offendingColon + 1);
    return new PrettyTime().format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(fixedTimestamp));
  }

  private String parseDestinationFromMessage(String message) {
    message = StringEscapeUtils.unescapeHtml4(message);
    int startToken = message.indexOf("__");

    if (startToken == -1) {
      return "Unknown";
    }

    int endToken = message.indexOf("__", startToken + 1);

    if (endToken == -1) {
      return "Unknown";
    }

    return message.substring(startToken+2, endToken);
  }

  private String parseUrlFromMessage(String message) throws ParseException {
    message = StringEscapeUtils.unescapeHtml4(message);
    int urlIndex = message.indexOf("https://");

    return message.substring(urlIndex).trim();
  }

  private String parseShaFromUrl(String url) throws ParseException {
    if (url == null) {
      throw new ParseException("No url", 0);
    }

    String[] parts    = url.split("/");
    String   fullHash = parts[parts.length-1];

    if (fullHash.length() < 8) {
      throw new ParseException("Not long enough", 0);
    }

    return fullHash.substring(0, 8);
  }

  public String getDestination() {
    return destination;
  }

  public String getAmount() {
    return amount;
  }

  public String getAmountInBTC() {
    return amountInBTC;
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
}


