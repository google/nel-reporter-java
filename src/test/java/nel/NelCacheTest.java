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

import static org.junit.Assert.assertEquals;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Test;

public class NelCacheTest {
  @Test
  public void canChoosePolicy() {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    final Origin origin = new Origin("https", "example.com", 443);
    final NelPolicy policy =
        new NelPolicy(origin, "nel", false, 0.05, 1.0, Duration.standardHours(1), I_1300);
    NelCache cache = new NelCache();
    cache.addPolicy(policy);
    // There's only one policy to choose from.
    assertEquals(policy, cache.choosePolicy(I_1301, origin));
  }

  @Test
  public void canChooseSuperdomainPolicy() {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    final Origin origin = new Origin("https", "foo.example.com", 443);
    final Origin superdomainOrigin = new Origin("https", "example.com", 443);
    final NelPolicy policy =
        new NelPolicy(superdomainOrigin, "nel", true, 0.05, 1.0, Duration.standardHours(1), I_1300);
    NelCache cache = new NelCache();
    cache.addPolicy(policy);
    // There's only one policy to choose from.
    assertEquals(policy, cache.choosePolicy(I_1301, origin));
  }

  @Test
  public void choosePolicyObeysIncludeSubdomains() {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    final Origin origin = new Origin("https", "foo.example.com", 443);
    final Origin superdomainOrigin = new Origin("https", "example.com", 443);
    final NelPolicy policy = new NelPolicy(
        superdomainOrigin, "nel", false, 0.05, 1.0, Duration.standardHours(1), I_1300);
    NelCache cache = new NelCache();
    cache.addPolicy(policy);
    // The superdomain policy has include-subdomains = false, so it's not eligible.
    assertEquals(null, cache.choosePolicy(I_1301, origin));
  }

}
