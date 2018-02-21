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
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Test;

public class EndpointGroupTest {
  @Test
  public void canGetMinPriority() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/1"), 1, 1));
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/2"), 2, 1));
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/3"), 3, 1));
    assertEquals(1, group.getMinimumPriority(I_1301));
  }

  @Test
  public void cannotGetMinPriorityWhenNoEndpoints() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    assertEquals(Integer.MAX_VALUE, group.getMinimumPriority(I_1301));
  }

  @Test
  public void minPriorityIgnoresPendingEndpoints() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    final Instant I_1401 = Instant.parse("2018-02-20T14:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    Endpoint endpoint = new Endpoint(new URL("https://example.com/upload/1"), 1, 1);
    group.addEndpoint(endpoint);
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/2"), 2, 1));
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/3"), 3, 1));
    endpoint.recordFailure(I_1401);
    // Endpoint 1 is ignored since it's pending.
    assertEquals(2, group.getMinimumPriority(I_1301));
  }

  @Test
  public void canGetTotalWeight() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/1"), 1, 1));
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/2"), 1, 2));
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/3"), 1, 3));
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/4"), 2, 4));
    assertEquals(6, group.getTotalWeightForPriority(I_1301, 1));
    assertEquals(4, group.getTotalWeightForPriority(I_1301, 2));
  }

  @Test
  public void canGetTotalWeightWhenNoEndpoints() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    assertEquals(0, group.getTotalWeightForPriority(I_1301, 1));
  }

  @Test
  public void totalWeightIgnoresPendingEndpoints() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    final Instant I_1401 = Instant.parse("2018-02-20T14:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    Endpoint endpoint = new Endpoint(new URL("https://example.com/upload/1"), 1, 1);
    group.addEndpoint(endpoint);
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/2"), 1, 2));
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload/3"), 1, 3));
    endpoint.recordFailure(I_1401);
    // Endpoint 1 is ignored since it's pending.
    assertEquals(5, group.getTotalWeightForPriority(I_1301, 1));
  }

  @Test
  public void canChooseEndpoint() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    Endpoint endpoint = new Endpoint(new URL("https://example.com/upload"));
    group.addEndpoint(endpoint);
    // There's only one endpoint to choose from.
    assertEquals(endpoint, group.chooseEndpoint(I_1301));
  }

  @Test
  public void cannotChooseWhenNoEndpoints() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    // There aren't any endpoints to choose from.
    assertEquals(null, group.chooseEndpoint(I_1301));
  }

  @Test
  public void cannotChooseFromExpiredGroup() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1401 = Instant.parse("2018-02-20T14:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    Endpoint endpoint = new Endpoint(new URL("https://example.com/upload"));
    group.addEndpoint(endpoint);
    // Using a "now" that is after the group's TTL has expired.
    assertEquals(null, group.chooseEndpoint(I_1401));
  }

  @Test
  public void chooseEndpointObeysPending() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    final Instant I_1401 = Instant.parse("2018-02-20T14:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    Endpoint endpoint = new Endpoint(new URL("https://example.com/upload"));
    group.addEndpoint(endpoint);
    // Mark the endpoint as having failed, with a retryAfter time far into the future.
    endpoint.recordFailure(I_1401);
    // Using a "now" that is before the endpoint's retryAfter time means that endpoint isn't eligble
    // to be chosen.
    assertEquals(null, group.chooseEndpoint(I_1301));
  }

  @Test
  public void chooseEndpointObeysPriority() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    Endpoint endpoint1 = new Endpoint(new URL("https://example.com/upload/1"), 1, 1);
    Endpoint endpoint2 = new Endpoint(new URL("https://example.com/upload/2"), 2, 1);
    group.addEndpoint(endpoint1);
    group.addEndpoint(endpoint2);
    // We should always pick the endpoint with the lowest priority value.
    assertEquals(endpoint1, group.chooseEndpoint(I_1301));
  }

  @Test
  public void chooseEndpointObeysPriorityAndPending() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    final Instant I_1401 = Instant.parse("2018-02-20T14:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    Endpoint endpoint1 = new Endpoint(new URL("https://example.com/upload/1"), 1, 1);
    Endpoint endpoint2 = new Endpoint(new URL("https://example.com/upload/2"), 2, 1);
    group.addEndpoint(endpoint1);
    group.addEndpoint(endpoint2);
    // Mark endpoint 1 as having failed, with a retryAfter time far into the future.
    endpoint1.recordFailure(I_1401);
    // We should always pick the endpoint 2, even though it has a higher priority value, since
    // endpoint 1 is pending.
    assertEquals(endpoint2, group.chooseEndpoint(I_1301));
  }

  @Test
  public void chooseEndpointObeysWeight() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    Endpoint endpoint1 = new Endpoint(new URL("https://example.com/upload/1"), 1, 1);
    Endpoint endpoint2 = new Endpoint(new URL("https://example.com/upload/2"), 1, 2);
    group.addEndpoint(endpoint1);
    group.addEndpoint(endpoint2);
    // Pick an endpoint many many times to exercise the randomness.
    HashMap<Endpoint, Integer> counts = new HashMap<Endpoint, Integer>();
    final int ITERATIONS = 1000;
    for (int i = 0; i < ITERATIONS; i++) {
      Endpoint result = group.chooseEndpoint(I_1301);
      Integer count = counts.get(result);
      if (count == null) {
        counts.put(result, 1);
      } else {
        counts.put(result, count + 1);
      }
    }
    // We should have gotten each endpoint at least once.
    assertTrue(counts.get(endpoint1) > 0);
    assertTrue(counts.get(endpoint2) > 0);
    // And we should have gotten endpoint 2 roughly twice as many times as endpoint 1, since its
    // weight is 2x.
    assertEquals(2.0, ((double) counts.get(endpoint2)) / ((double) counts.get(endpoint1)), 0.5);
  }

}
