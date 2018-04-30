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

import org.joda.time.Instant;

/**
 * A cache of all of all NEL policies that we have received.
 */
public class NelCache {
  /** Creates a new, empty cache. */
  public NelCache() {
    this.policies = new OriginMap<NelPolicy>();
  }

  /** Adds a new policy to the cache, replacing any existing policy for the same origin. */
  public void addPolicy(NelPolicy policy) {
    policies.put(policy.getOrigin(), policy);
  }

  /**
   * Chooses a NEL policy for an origin, taking into account the <code>include-subdomains</code>,
   * property of the policies.
   *
   * <p>
   * Returns <code>null</code> if we cannot find any appropriate policy for the origin.
   * </p>
   */
  public NelPolicy choosePolicy(Instant now, Origin origin) {
    // Loop through all of the policies registered for origin, or any of its superdomains.
    for (NelPolicy policy : policies.getAll(origin)) {
      if (policy.isExpired(now)) {
        continue;
      }
      if (policy.getOrigin() == origin || policy.includeSubdomains()) {
        return policy;
      }
    }

    // Couldn't find any suitable policies!
    return null;
  }

  private OriginMap<NelPolicy> policies;
}
