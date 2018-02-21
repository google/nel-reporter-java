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

import java.net.MalformedURLException;
import java.net.URL;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Test;

public class EndpointGroupTest {
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
  public void cannotChoosePendingEndpoint() throws MalformedURLException {
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
}
