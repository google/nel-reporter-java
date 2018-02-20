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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * A GSON TypeAdapter that can render a {@link Report} instance in JSON as defined by the <a
 * href="https://wicg.github.io/reporting/">Reporting</a> and <a
 * href="https://wicg.github.io/network-error-logging/">NEL</a> specs.
 *
 * Note that {@link Report}s include a timestamp (when the report was generated), while the specs
 * define an <code>age</code> field (the difference in time between when the report was generated
 * and when it was uploaded).  When constructing a new adapter, you must pass in the current time
 * (or whatever time you wish to use as the "upload time"), which we will use to calculate the
 * <code>age</code> fields.
 */
public class ReportJsonAdapter extends TypeAdapter<Report> {
  /**
   * Creates a new adapter that uses <code>now</code> as the base time for calculating the
   * <code>age</code> field of any JSON report payloads.
   */
  public ReportJsonAdapter(Instant now) {
    this.now = now;
  }

  public Report read(JsonReader reader) throws IOException {
    throw new IllegalStateException("Cannot parse Reports from JSON");
  }

  public void write(JsonWriter writer, Report report) throws IOException {
    writer.beginObject();
    if (report.getTimestamp() != null) {
      writer.name("age").value(
          new Duration(report.getTimestamp(), now).getMillis());
    }
    writer.name("type").value("network-error");
    writer.name("url").value(report.getUri().toString());
    writer.name("body").beginObject();
    writer.name("uri").value(report.getUri().toString());
    if (report.getReferrer() == null) {
      writer.name("referrer").nullValue();
    } else {
      writer.name("referrer").value(report.getReferrer().toString());
    }
    writer.name("sampling-fraction").value(report.getSamplingFraction());
    writer.name("server-ip").value(report.getServerIp().getHostAddress());
    writer.name("protocol").value(report.getProtocol());
    if (report.getStatusCode() != 0) {
      writer.name("status-code").value(report.getStatusCode());
    }
    writer.name("elapsed-time").value(report.getElapsedTime().getMillis());
    writer.name("type").value(report.getType().toString());
    writer.endObject();
    writer.endObject();
  }

  private Instant now;
}
