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

public class ReportingCacheTest {
  @Test
  public void canEnqueueReports() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    ReportingCache cache = new ReportingCache();
    cache.enqueueReport(new Report()
      .setTimestamp(I_1300)
      .setUri("https://example.com")
      .setSamplingFraction(0.5)
      .setServerIp("192.0.2.24")
      .setProtocol("h2")
      .setStatusCode(200)
      .setElapsedTime(Duration.millis(1000))
      .setType(Type.OK));
    assertEquals(1, cache.getQueuedReportCount());
  }

  @Test
  public void canRemoveOldReports() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1330 = Instant.parse("2018-02-20T13:30:00.000Z");
    final Instant I_1400 = Instant.parse("2018-02-20T14:00:00.000Z");
    ReportingCache cache = new ReportingCache();
    // This report will be removed
    cache.enqueueReport(new Report()
      .setTimestamp(I_1300)
      .setUri("https://example.com")
      .setSamplingFraction(0.5)
      .setServerIp("192.0.2.24")
      .setProtocol("h2")
      .setStatusCode(200)
      .setElapsedTime(Duration.millis(1000))
      .setType(Type.OK));
    // And this one won't
    cache.enqueueReport(new Report()
      .setTimestamp(I_1400)
      .setUri("https://example.com")
      .setSamplingFraction(0.5)
      .setServerIp("192.0.2.24")
      .setProtocol("h2")
      .setStatusCode(200)
      .setElapsedTime(Duration.millis(1000))
      .setType(Type.OK));
    cache.removeOldReports(I_1330);
    assertEquals(1, cache.getQueuedReportCount());
  }

  @Test
  public void canChooseEndpoint() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    ReportingCache cache = new ReportingCache();
    Origin origin = new Origin("https", "example.com", 443);
    Client client = new Client(origin);
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    Endpoint endpoint = new Endpoint(new URL("https://example.com/upload"));
    group.addEndpoint(endpoint);
    client.addGroup(group);
    cache.addClient(client);
    // There's only one endpoint to choose from.
    assertEquals(endpoint, cache.chooseEndpoint(I_1301, origin, "nel"));
  }

  @Test
  public void canChooseSuperdomainEndpoint() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    ReportingCache cache = new ReportingCache();
    Origin origin = new Origin("https", "foo.example.com", 443);
    Origin superdomainOrigin = new Origin("https", "example.com", 443);
    Client client = new Client(superdomainOrigin);
    EndpointGroup group = new EndpointGroup("nel", true, Duration.standardHours(1), I_1300);
    Endpoint endpoint = new Endpoint(new URL("https://example.com/upload"));
    group.addEndpoint(endpoint);
    client.addGroup(group);
    cache.addClient(client);
    // There's only one endpoint to choose from.
    assertEquals(endpoint, cache.chooseEndpoint(I_1301, origin, "nel"));
  }

  @Test
  public void chooseEndpointObeysIncludeSubdomains() throws MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Instant I_1301 = Instant.parse("2018-02-20T13:01:00.000Z");
    ReportingCache cache = new ReportingCache();
    Origin origin = new Origin("https", "foo.example.com", 443);
    Origin superdomainOrigin = new Origin("https", "example.com", 443);
    Client client = new Client(superdomainOrigin);
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardHours(1), I_1300);
    Endpoint endpoint = new Endpoint(new URL("https://example.com/upload"));
    group.addEndpoint(endpoint);
    client.addGroup(group);
    cache.addClient(client);
    // The superdomain endpoint group has include-subdomains = false, so it's not eligible.
    assertEquals(null, cache.chooseEndpoint(I_1301, origin, "nel"));
  }

}
