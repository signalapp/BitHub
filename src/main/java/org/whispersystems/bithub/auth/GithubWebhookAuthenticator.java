package org.whispersystems.bithub.auth;

import com.google.common.base.Optional;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.basic.BasicCredentials;

/**
 * Accepts only one fixed username/password combination.
 */
public class GithubWebhookAuthenticator implements Authenticator<BasicCredentials, GithubWebhookAuthenticator.Authentication> {

  /**
   * Represents a successful basic HTTP authentication.
   */
  public static class Authentication {
  }

  public static final String REALM = "bithub";

  private final BasicCredentials correctCredentials;

  public GithubWebhookAuthenticator(String username, String password) {
    this.correctCredentials = new BasicCredentials(username, password);
  }

  @Override
  public Optional<Authentication> authenticate(BasicCredentials clientCredentials) {
    if (correctCredentials.equals(clientCredentials)) {
      return Optional.of(new Authentication());
    } else {
      return Optional.absent();
    }
  }
}
