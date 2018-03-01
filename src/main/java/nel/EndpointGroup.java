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

import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * A set of {@link Endpoint}s that will be used together for backup and failover purposes.
 */
public class EndpointGroup {
  /**
   * Creates a new endpoint group.  The <code>ttl</code> and <code>now</code> parameters are used to
   * calculate an expiration time for this group; after that point, we will no longer use this group
   * to upload reports for its origin.  (Presumably the configuration will be refreshed before then
   * by a newer successful response from the origin.)
   */
  public EndpointGroup(String name, boolean subdomains, Duration ttl, Instant now) {
    this.name = name;
    this.endpoints = new ArrayList<Endpoint>();
    this.subdomains = subdomains;
    this.ttl = ttl;
    this.creation = now;
    this.expiry = now.plus(ttl);
  }

  public String getName() {
    return name;
  }

  public boolean includeSubdomains() {
    return subdomains;
  }

  /**
   * Returns the minimum <code>priority</code> value of all of the non-pending endpoints in this
   * group.  Returns {@link Integer.MAX_VALUE} if all endpoints in the group are pending.
   */
  public int getMinimumPriority(Instant now) {
    int minPriority = Integer.MAX_VALUE;
    for (Endpoint endpoint : endpoints) {
      if (endpoint.isPending(now)) {
        continue;
      }
      int thisPriority = endpoint.getPriority();
      if (thisPriority < minPriority) {
        minPriority = thisPriority;
      }
    }
    return minPriority;
  }

  /**
   * Returns the total <code>weight</code> of all of the non-pending endpoints with the given
   * <code>priority</code>.
   */
  public int getTotalWeightForPriority(Instant now, int priority) {
    int totalWeight = 0;
    for (Endpoint endpoint : endpoints) {
      if (endpoint.isPending(now)) {
        continue;
      }
      if (endpoint.getPriority() != priority) {
        continue;
      }
      totalWeight += endpoint.getWeight();
    }
    return totalWeight;
  }

  /** Adds a new endpoint to this group. */
  public void addEndpoint(Endpoint endpoint) {
    endpoints.add(endpoint);
  }

  /** Returns whether this endpoint is expired as of <code>now</code>. */
  public boolean isExpired(Instant now) {
    return now.isAfter(expiry);
  }

  /**
   * Chooses an arbitrary endpoint from this group to upload reports to, using the <a
   * href="https://wicg.github.io/reporting/#choose-endpoint">"Choose an endpoint"</a> algorithm
   * from the Reporting spec.
   */
  public Endpoint chooseEndpoint(Instant now) {
    if (isExpired(now)) {
      return null;
    }

    int minPriority = getMinimumPriority(now);
    if (minPriority == Integer.MAX_VALUE) {
      return null;
    }

    int totalWeight = getTotalWeightForPriority(now, minPriority);
    if (totalWeight == 0) {
      return null;
    }

    int selectedWeight = RANDOM.nextInt(totalWeight);
    for (Endpoint endpoint : endpoints) {
      if (endpoint.isPending(now)) {
        continue;
      }
      if (endpoint.getPriority() != minPriority) {
        continue;
      }
      int thisWeight = endpoint.getWeight();
      if (selectedWeight < thisWeight) {
        return endpoint;
      }
      selectedWeight -= thisWeight;
    }
    return null;
  }

  private static Random RANDOM = new Random();

  private String name;
  private ArrayList<Endpoint> endpoints;
  private boolean subdomains;
  private Duration ttl;
  private Instant creation;
  private Instant expiry;
}
