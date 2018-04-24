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

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * A GSON TypeAdapter that can parse <code>NEL</code> headers as defined by the <a
 * href="https://wicg.github.io/reporting/">Reporting</a> spec.
 */
public class NelPolicyJsonAdapter extends TypeAdapter<NelPolicy> {
  /**
   * Creates a new adapter that can parse {@link NelPolicy} instances for a particular
   * <code>origin</code>, using <code>now</code> as the creation time.
   */
  public NelPolicyJsonAdapter(Origin origin, Instant now) {
    this.origin = origin;
    this.now = now;
  }

  @Override
  public NelPolicy read(JsonReader reader) throws IOException {
    String reportTo = null;
    boolean subdomains = false;
    double successFraction = 0.0;
    double failureFraction = 1.0;
    Duration ttl = null;

    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals("report-to")) {
        if (reader.peek() != JsonToken.STRING) {
          throw new MalformedJsonException("\"report-to\" must be a string in NEL header");
        }
        reportTo = reader.nextString();
      } else if (name.equals("include-subdomains")) {
        if (reader.peek() != JsonToken.BOOLEAN) {
          throw new MalformedJsonException(
              "\"include-subdomains\" must be a boolean in NEL header");
        }
        subdomains = reader.nextBoolean();
      } else if (name.equals("max-age")) {
        if (reader.peek() != JsonToken.NUMBER) {
          throw new MalformedJsonException("\"max-age\" must be a number in NEL header");
        }
        long maxAge = reader.nextLong();
        if (maxAge < 0) {
          throw new MalformedJsonException("\"max-age\" must be non-negative in NEL header");
        }
        ttl = Duration.standardSeconds(maxAge);
      } else if (name.equals("success-fraction")) {
        if (reader.peek() != JsonToken.NUMBER) {
          throw new MalformedJsonException("\"success-fraction\" must be a number in NEL header");
        }
        successFraction = reader.nextDouble();
        if (successFraction < 0.0) {
          throw new MalformedJsonException("\"success-fraction\" must be >= 0.0 in NEL header");
        }
        if (successFraction > 1.0) {
          throw new MalformedJsonException("\"success-fraction\" must be <= 1.0 in NEL header");
        }
      } else if (name.equals("failure-fraction")) {
        if (reader.peek() != JsonToken.NUMBER) {
          throw new MalformedJsonException("\"failure-fraction\" must be a number in NEL header");
        }
        failureFraction = reader.nextDouble();
        if (failureFraction < 0.0) {
          throw new MalformedJsonException("\"failure-fraction\" must be >= 0.0 in NEL header");
        }
        if (failureFraction > 1.0) {
          throw new MalformedJsonException("\"failure-fraction\" must be <= 1.0 in NEL header");
        }
      } else {
        reader.skipValue();
      }
    }
    reader.endObject();

    if (ttl == null) {
      throw new MalformedJsonException("Missing \"max-age\" in NEL header");
    }

    if (reportTo == null && !ttl.equals(Duration.ZERO)) {
      throw new MalformedJsonException("Missing \"report-to\" in NEL header");
    }

    return new NelPolicy(origin, reportTo, subdomains, successFraction, failureFraction, ttl, now);
  }

  @Override
  public void write(JsonWriter writer, NelPolicy policy) throws IOException {
    throw new IllegalStateException("Cannot write NelPolicies to JSON");
  }

  private Origin origin;
  private Instant now;
}
