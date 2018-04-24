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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * Describes which Network Error Logging reports to collect for an origin.
 */
public class NelPolicy {
  /** Creates a new policy. */
  public NelPolicy(Origin origin, String reportTo, boolean includeSubdomains,
      double successFraction, double failureFraction, Duration ttl, Instant now) {
    this.origin = origin;
    this.reportTo = reportTo;
    this.subdomains = includeSubdomains;
    this.successFraction = successFraction;
    this.failureFraction = failureFraction;
    this.ttl = ttl;
    this.creation = now;
    this.expiry = now.plus(ttl);
  }

  /**
   * Parses a NEL policy from the contents of a <code>NEL</code> header.
   *
   * @param header The value of the <code>NEL</code> header from the response.
   * @param origin The origin of the response.
   * @param now The current timestamp.  Will be used to calculate expiry times for the new policy.
   */
  public static NelPolicy parseFromNelHeader(String header, Origin origin, Instant now)
      throws InvalidHeaderException {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(NelPolicy.class, new NelPolicyJsonAdapter(origin, now));
    Gson gson = builder.create();
    try {
      return gson.fromJson(header, NelPolicy.class);
    } catch (JsonSyntaxException e) {
      throw new InvalidHeaderException("Invalid \"NEL\" header", e);
    }
  }

  public boolean includeSubdomains() {
    return subdomains;
  }

  public String getReportTo() {
    return reportTo;
  }

  public double getSuccessFraction() {
    return successFraction;
  }

  public double getFailureFraction() {
    return failureFraction;
  }

  /** Returns whether this policy is expired as of <code>now</code>. */
  public boolean isExpired(Instant now) {
    return now.isAfter(expiry);
  }

  @Override
  public String toString() {
    return "NelPolicy(origin=" + origin + ", reportTo=" + reportTo + ", includeSubdomains="
        + Boolean.toString(subdomains) + ", successFraction=" + Double.toString(successFraction)
        + ", failureFraction=" + Double.toString(failureFraction) + ", ttl=" + ttl + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NelPolicy)) {
      return false;
    }
    NelPolicy other = (NelPolicy) obj;
    return this.origin.equals(other.origin) && reportTo.equals(other.reportTo)
        && subdomains == other.subdomains && successFraction == other.successFraction
        && failureFraction == other.failureFraction && this.ttl.equals(other.ttl)
        && this.creation.equals(other.creation);
  }

  private Origin origin;
  private String reportTo;
  private boolean subdomains;
  private double successFraction;
  private double failureFraction;
  private Duration ttl;
  private Instant creation;
  private Instant expiry;
}
