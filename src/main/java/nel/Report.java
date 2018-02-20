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

import com.google.gson.GsonBuilder;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URI;
import java.net.URISyntaxException;
import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * Contains a single NEL report, as defined by the <a
 * href="https://wicg.github.io/reporting/">Reporting</a> and <a
 * href="https://wicg.github.io/network-error-logging/">NEL</a> specs.
 */
public class Report {
  /** Returns the timestamp when the report was created. */
  public Instant getTimestamp() {
    return timestamp;
  }

  /** Returns the URI of the request that this report describes (the "original request"). */
  public URI getUri() {
    return uri;
  }

  /** Returns the referrer information for the original request, if any. */
  public URI getReferrer() {
    return referrer;
  }

  /**
   * Returns the sampling rate that was in effect when this report was captured.  When doing any
   * follow-on analysis, you should assume that this report represents <code>1 /
   * samplingFraction</code> requests.
   */
  public double getSamplingFraction() {
    return samplingFraction;
  }

  /**
   * Returns the IP address of the server that the original request was sent to.  This can be
   * <code>null</code> if DNS failed to resolve an IP address for the request.
   */
  public InetAddress getServerIp() {
    return serverIp;
  }

  /**
   * Returns the <a href="https://tools.ietf.org/html/rfc7301">ALPN protocol</a> that was used for
   * the original request.
   */
  public String getProtocol() {
    return protocol;
  }

  /** Returns the HTTP status code that was returned for the original request. */
  public int getStatusCode() {
    return statusCode;
  }

  /** Returns the amount of time that it took to process the original request. */
  public Duration getElapsedTime() {
    return elapsedTime;
  }

  /**
   * Returns the description of the error that occurred when processing the original request, or
   * <code>ok</code> if the original request was successful.
   */
  public Type getType() {
    return type;
  }

  /** Sets the timestamp of this report. */
  public Report setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Sets the URI of this report.  We will remove any fragment in <code>uri</code> as required by
   * the specs.
   */
  public Report setUri(URI uri) {
    // Remove the fragment identifier from the URI, if any.
    try {
      this.uri = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
    } catch (URISyntaxException e) {
      // Rethrow this as unchecked; we started with a valid URI, so this should never occur.
      throw new IllegalArgumentException(e);
    }
    return this;
  }

  /**
   * Sets the URI of this report from a string.  This should only be used in test cases.
   */
  public Report setUri(String uri) {
    try {
      return setUri(new URI(uri));
    } catch (URISyntaxException e) {
      // Rethrow this as unchecked; this should only be used in test cases, and you should only pass
      // in valid URIs.
      throw new IllegalArgumentException(e);
    }
  }

  /** Sets the referrer URI of this report. */
  public Report setReferrer(URI referrer) {
    this.referrer = referrer;
    return this;
  }

  /** Sets the sampling fraction of this report. */
  public Report setSamplingFraction(double samplingFraction) {
    this.samplingFraction = samplingFraction;
    return this;
  }

  /** Sets the server IP address of this report. */
  public Report setServerIp(InetAddress serverIp) {
    this.serverIp = serverIp;
    return this;
  }

  /**
   * Sets the server IP address of this report.  This should only be used in test cases, and you
   * must only pass in IP address literals for <code>serverIp</code>.
   */
  public Report setServerIp(String serverIp) {
    try {
      return setServerIp(InetAddress.getByName(serverIp));
    } catch (UnknownHostException e) {
      // Rethrow this as unchecked; this method is only for test cases, and should only be operating
      // on IP addresses.
      throw new IllegalArgumentException(e);
    }
  }

  /** Sets the protocol of this report. */
  public Report setProtocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  /** Sets the HTTP status code of this report. */
  public Report setStatusCode(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  /** Sets the elapsed time of this report. */
  public Report setElapsedTime(Duration elapsedTime) {
    this.elapsedTime = elapsedTime;
    return this;
  }

  /** Sets the error type of this report. */
  public Report setType(Type type) {
    this.type = type;
    return this;
  }

  /**
   * Renders this report in JSON, using the current system time to calculate the <code>age</code> of
   * the report.
   */
  public String toString() {
    return toString(new Instant());
  }

  /**
   * Renders this report in JSON, using <code>now</code> to calculate the <code>age</code> of the
   * report.
   */
  public String toString(Instant now) {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Report.class, new ReportJsonAdapter(now));
    return builder.setPrettyPrinting().create().toJson(this);
  }

  private Instant timestamp;
  private URI uri;
  private URI referrer;
  private double samplingFraction;
  private InetAddress serverIp;
  private String protocol;
  private int statusCode;
  private Duration elapsedTime;
  private Type type;
}
