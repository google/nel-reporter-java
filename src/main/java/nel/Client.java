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

/**
 * A particular {@link Origin}'s relationship to a set of {@link Endpoint}s.
 */
public class Client {
  /** Creates a new client for a given <code>origin</code>. */
  public Client(Origin origin) {
    this.origin = origin;
    this.groups = new HashMap<String, EndpointGroup>();
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

  private Origin origin;
  private HashMap<String, EndpointGroup> groups;
}
