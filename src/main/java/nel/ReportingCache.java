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

import java.util.HashSet;
import java.util.Iterator;

import org.joda.time.Instant;

/**
 * A cache of all of all Reporting configurations that we have received, and of reports that are
 * queued for delivery.
 */
public class ReportingCache {
  /** Creates a new, empty cache. */
  public ReportingCache() {
    this.clients = new OriginMap<Client>();
    this.queuedReports = new HashSet<QueuedReport>();
  }

  /** Adds a new client to the cache, replacing any existing client for the same origin. */
  public void addClient(Client client) {
    clients.put(client.getOrigin(), client);
  }

  /** Returns the number of queued reports. */
  public int getQueuedReportCount() {
    return queuedReports.size();
  }

  /** Adds a new report to the cache. */
  public void enqueueReport(Report report) {
    queuedReports.add(new QueuedReport(report, "nel"));
  }

  /** Removes all queued reports older than <code>cutoff</code>. */
  public void removeOldReports(Instant cutoff) {
    Iterator<QueuedReport> iter = queuedReports.iterator();
    while (iter.hasNext()) {
      QueuedReport queuedReport = iter.next();
      if (queuedReport.getReport().getTimestamp().isBefore(cutoff)) {
        iter.remove();
      }
    }
  }

  /**
   * Chooses an endpoint for an origin, taking into account any <code>include-subdomains</code>,
   * <code>priority</code>, and <code>weight</code> properties of the endpoints.
   *
   * <p>
   * Returns <code>null</code> if we cannot find any appropriate endpoint for the origin.
   * </p>
   *
   * <p>
   * This implements step 3 of the <a href="https://wicg.github.io/reporting/#send-reports">"Send
   * reports"</a> algorithm in the Reporting spec.
   * </p>
   */
  public Endpoint chooseEndpoint(Instant now, Origin origin, String groupName) {
    // Loop through all of the clients registered for origin, or any of its superdomains.
    for (Client client : clients.getAll(origin)) {
      EndpointGroup group = client.getGroup(groupName);
      if (group == null) {
        // This client has no group with the requested name.
        continue;
      }
      if (client.getOrigin() != origin && !group.includeSubdomains()) {
        // This client is for a superdomain of origin, and its group does not have
        // include-subdomains set to true; that means we can't use it for the subdomain.
        continue;
      }
      Endpoint endpoint = group.chooseEndpoint(now);
      if (endpoint != null) {
        return endpoint;
      }
    }

    // Couldn't find any suitable endpoints!
    return null;
  }

  private OriginMap<Client> clients;
  private HashSet<QueuedReport> queuedReports;
}
