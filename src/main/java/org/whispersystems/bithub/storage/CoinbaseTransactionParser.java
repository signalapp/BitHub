package org.whispersystems.bithub.storage;

import com.coinbase.api.entity.Transaction;
import org.apache.commons.lang3.StringEscapeUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

public class CoinbaseTransactionParser {

  private final Transaction coinbaseTransaction;

  public CoinbaseTransactionParser(Transaction coinbaseTransaction) {
    this.coinbaseTransaction = coinbaseTransaction;
  }

  public String parseAmountInDollars(BigDecimal exchangeRate) {
    return coinbaseTransaction.getAmount().getAmount().abs()
                              .multiply(exchangeRate)
                              .setScale(2, RoundingMode.CEILING)
                              .toPlainString();
  }

  public String parseTimestamp() throws ParseException {
    DateTime          timestamp = coinbaseTransaction.getCreatedAt();
    DateTimeFormatter fmt       = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    return fmt.print(timestamp);
  }

  public String parseDestinationFromMessage() {
    String message = StringEscapeUtils.unescapeHtml4(coinbaseTransaction.getNotes());
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

  public String parseUrlFromMessage() throws ParseException {
    String message = StringEscapeUtils.unescapeHtml4(coinbaseTransaction.getNotes());
    int urlIndex = message.indexOf("https://");

    return message.substring(urlIndex).trim();
  }

  public String parseShaFromUrl(String url) throws ParseException {
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

}
