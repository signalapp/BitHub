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

package org.whispersystems.bithub.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.bithub.entities.Commit;
import org.whispersystems.bithub.entities.CommitComment;
import org.whispersystems.bithub.entities.Repository;

import javax.ws.rs.core.MediaType;

/**
 * Handles interaction with the GitHub API.
 *
 * @author Moxie Marlinspike
 */
public class GithubClient {

  private static final String GITHUB_URL      = "https://api.github.com/";
  private static final String COMMENT_PATH    = "/repos/%s/%s/commits/%s/comments";
  private static final String COMMIT_PATH     = "/repos/%s/%s/git/commits/%s";
  private static final String REPOSITORY_PATH = "/repos/%s/%s";

  private final Logger logger = LoggerFactory.getLogger(GithubClient.class);

  private final String authorizationHeader;
  private final Client client;

  public GithubClient(String user, String token) {
    this.authorizationHeader = getAuthorizationHeader(user, token);
    this.client              = Client.create(getClientConfig());
  }

  public String getCommitDescription(String commitUrl) {
    String[] commitUrlParts = commitUrl.split("/");
    String   owner          = commitUrlParts[commitUrlParts.length - 4];
    String   repository     = commitUrlParts[commitUrlParts.length - 3];
    String   commit         = commitUrlParts[commitUrlParts.length - 1];

    String      path     = String.format(COMMIT_PATH, owner, repository, commit);
    WebResource resource = client.resource(GITHUB_URL).path(path);
    Commit      response = resource.type(MediaType.APPLICATION_JSON_TYPE)
                                   .accept(MediaType.APPLICATION_JSON_TYPE)
                                   .header("Authorization", authorizationHeader)
                                   .get(Commit.class);

    return response.getMessage();
  }

  public Repository getRepository(String url) {
    String[] urlParts = url.split("/");
    String   owner    = urlParts[urlParts.length - 2];
    String   name     = urlParts[urlParts.length - 1];

    String      path     = String.format(REPOSITORY_PATH, owner, name);
    WebResource resource = client.resource(GITHUB_URL).path(path);

    return resource.type(MediaType.APPLICATION_JSON_TYPE)
                   .accept(MediaType.APPLICATION_JSON_TYPE)
                   .header("Authorization", authorizationHeader)
                   .get(Repository.class);

  }

  public void addCommitComment(Repository repository, Commit commit, String comment) {
    try {
      String path = String.format(COMMENT_PATH, repository.getOwner().getName(),
                                  repository.getName(), commit.getSha());

      WebResource    resource = client.resource(GITHUB_URL).path(path);
      ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .header("Authorization", authorizationHeader)
                                        .entity(new CommitComment(comment))
                                        .post(ClientResponse.class);

      if (response.getStatus() < 200 || response.getStatus() >=300) {
        logger.warn("Commit comment failed: " + response.getClientResponseStatus().getReasonPhrase());
      }

    } catch (UniformInterfaceException | ClientHandlerException e) {
      logger.warn("Comment failed", e);
    }
  }

  private ClientConfig getClientConfig() {
    ClientConfig config = new DefaultClientConfig();
    config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

    return config;
  }

  private String getAuthorizationHeader(String user, String token) {
    return "Basic " + new String(Base64.encode(user + ":" + token));
  }

}
