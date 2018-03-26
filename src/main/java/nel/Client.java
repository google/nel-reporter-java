/* Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nel;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.joda.time.Instant;

/**
 * A particular {@link Origin}'s relationship to a set of {@link Endpoint}s.
 */
public class Client {
  /** Creates a new client for a given <code>origin</code>. */
  public Client(Origin origin) {
    this.origin = origin;
    this.groups = new HashMap<String, EndpointGroup>();
  }

  /**
   * Parses a client from the contents of a <code>Report-To</code> header.
   *
   * @param headers A list of all values for the <code>Report-To</code> header from the response.
   * @param origin The origin of the response.
   * @param now The current timestamp.  Will be used to calculate expiry times for any endpoint
   *     groups in the new client.
   */
  public static Client parseFromReportToHeader(List<String> headers, Origin origin, Instant now)
      throws InvalidHeaderException {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(EndpointGroup.class, new EndpointGroupJsonAdapter(now));
    Gson gson = builder.create();
    Client client = new Client(origin);
    for (String header : headers) {
      try {
        client.addGroup(gson.fromJson(header, EndpointGroup.class));
      } catch (JsonSyntaxException e) {
        throw new InvalidHeaderException("Invalid \"Report-To\" header", e);
      }
    }
    return client;
  }

  public Origin getOrigin() {
    return origin;
  }

  /** Adds a new endpoint group to this client. */
  public void addGroup(EndpointGroup group) {
    groups.put(group.getName(), group);
  }

  /**
   * Returns the endpoint group with the given name, or <code>null</code> if there is no such group.
   */
  public EndpointGroup getGroup(String name) {
    return groups.get(name);
  }

  @Override
  public String toString() {
    return "Client(origin=" + origin + ", groups=" + groups + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Client)) {
      return false;
    }
    Client other = (Client) obj;
    return this.origin.equals(other.origin)
      && this.groups.equals(other.groups);
  }

  private Origin origin;
  private HashMap<String, EndpointGroup> groups;
}
