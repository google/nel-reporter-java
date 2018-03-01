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

import org.joda.time.Instant;

/**
 * A location to which reports for a particular origin may be sent.
 */
public class Endpoint {
  /** Creates a new endpoint that will upload reports to the given <code>url</code>. */
  public Endpoint(URL url, int priority, int weight) {
    this.url = url;
    this.priority = priority;
    this.weight = weight;
    this.failures = 0;
    this.retryAfter = null;
  }

  /** Creates a new endpoint that will upload reports to the given <code>url</code>. */
  public Endpoint(URL url) {
    this(url, 1, 1);
  }

  public int getPriority() {
    return priority;
  }

  public int getWeight() {
    return weight;
  }

  /**
   * Returns whether this endpoint is <em>pending</em>.  A pending endpoint is one where we recently
   * encountered a failure trying to upload reports, and have not exceeded the retry delay.
   */
  public boolean isPending(Instant now) {
    return retryAfter != null && retryAfter.isAfter(now);
  }

  /**
   * Records that we were able to successfully upload reports to this endpoint.  This clears any
   * existing "pending" flag for the endpoint.
   */
  public void recordSuccess() {
    this.failures = 0;
    this.retryAfter = null;
  }

  /**
   * Records that we were <em>not</em> able to upload reports to this endpoint.  This sets the
   * pending flag, ensuring that we don't try to upload to this endpoint again until some point in
   * the future.
   */
  public void recordFailure(Instant retryAfter) {
    this.failures++;
    this.retryAfter = retryAfter;
  }

  public String toString() {
    return "<" + url.toString() + ">";
  }

  private URL url;
  private int priority;
  private int weight;
  private int failures;
  private Instant retryAfter;
}
