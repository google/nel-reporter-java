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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * A GSON TypeAdapter that can parse <code>Report-To</code> headers as defined by the <a
 * href="https://wicg.github.io/reporting/">Reporting</a> spec.
 */
public class EndpointGroupJsonAdapter extends TypeAdapter<EndpointGroup> {
  /**
   * Creates a new adapter that can parse {@link EndpointGroup} instances, using <code>now</code> as
   * the creation time.
   */
  public EndpointGroupJsonAdapter(Instant now) {
    this.now = now;
  }

  @Override
  public EndpointGroup read(JsonReader reader) throws IOException {
    String groupName = "default";
    boolean subdomains = false;
    Duration ttl = null;
    ArrayList<Endpoint> endpoints = null;

    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals("group")) {
        if (reader.peek() != JsonToken.STRING) {
          throw new MalformedJsonException("\"group\" must be a string in Report-To header");
        }
        groupName = reader.nextString();
      } else if (name.equals("include-subdomains")) {
        if (reader.peek() != JsonToken.BOOLEAN) {
          subdomains = false;
          continue;
        }
        subdomains = reader.nextBoolean();
      } else if (name.equals("max-age")) {
        if (reader.peek() != JsonToken.NUMBER) {
          throw new MalformedJsonException("\"max-age\" must be a number in Report-To header");
        }
        long maxAge = reader.nextLong();
        if (maxAge < 0) {
          throw new MalformedJsonException("\"max-age\" must be non-negative in Report-To header");
        }
        ttl = Duration.standardSeconds(maxAge);
      } else if (name.equals("endpoints")) {
        endpoints = readEndpoints(reader);
      } else {
        reader.skipValue();
      }
    }
    reader.endObject();

    if (ttl == null) {
      throw new MalformedJsonException("Missing \"max-age\" in Report-To header");
    }

    if (endpoints == null) {
      throw new MalformedJsonException("Missing \"endpoints\" in Report-To header");
    }

    if (endpoints.size() == 0) {
      throw new MalformedJsonException("Empty \"endpoints\" in Report-To header");
    }

    EndpointGroup group = new EndpointGroup(groupName, subdomains, ttl, now);
    group.addEndpoints(endpoints);
    return group;
  }

  private ArrayList<Endpoint> readEndpoints(JsonReader reader) throws IOException {
    ArrayList<Endpoint> endpoints = new ArrayList<Endpoint>();
    reader.beginArray();
    while (reader.hasNext()) {
      endpoints.add(readEndpoint(reader));
    }
    reader.endArray();
    return endpoints;
  }

  private Endpoint readEndpoint(JsonReader reader) throws IOException {
    URL url = null;
    int priority = 1;
    int weight = 1;

    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals("url")) {
        if (reader.peek() != JsonToken.STRING) {
          throw new MalformedJsonException("\"url\" must be a string in Report-To header");
        }
        try {
          url = new URL(reader.nextString());
        } catch (MalformedURLException e) {
          throw new MalformedJsonException("Invalid endpoint \"url\" in Report-To header", e);
        }
        if (!url.getProtocol().equals("https")) {
          throw new MalformedJsonException("\"url\" must be secure (HTTPS) in Report-To header");
        }
      } else if (name.equals("priority")) {
        if (reader.peek() != JsonToken.NUMBER) {
          throw new MalformedJsonException("\"priority\" must be a string in Report-To header");
        }
        priority = reader.nextInt();
        if (priority < 0) {
          throw new MalformedJsonException("\"priority\" must be non-negative in Report-To header");
        }
      } else if (name.equals("weight")) {
        if (reader.peek() != JsonToken.NUMBER) {
          throw new MalformedJsonException("\"weight\" must be a string in Report-To header");
        }
        weight = reader.nextInt();
        if (weight <= 0) {
          throw new MalformedJsonException("\"weight\" must be positive in Report-To header");
        }
      } else {
        reader.skipValue();
      }
    }
    reader.endObject();

    if (url == null) {
      throw new MalformedJsonException("Missing endpoint \"url\" in Report-To header");
    }

    return new Endpoint(url, priority, weight);
  }

  @Override
  public void write(JsonWriter writer, EndpointGroup group) throws IOException {
    throw new IllegalStateException("Cannot write EndpointGroups to JSON");
  }

  private Instant now;
}
