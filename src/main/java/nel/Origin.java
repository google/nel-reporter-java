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

import java.util.Objects;

/**
 * The <a href="https://html.spec.whatwg.org/multipage/origin.html#origin">origin</a> of an HTTP
 * request.  Per the HTML spec:
 *
 * <blockquote>
 * Origins are the fundamental currency of the Web's security model. Two actors in the Web platform
 * that share an origin are assumed to trust each other and to have the same authority. Actors with
 * differing origins are considered potentially hostile versus each other, and are isolated from
 * each other to varying degrees.
 * </blockquote>
 *
 * This class in particular represents a <em>tuple origin</em> — the combination of scheme, host,
 * and port from the original request.
 */
public class Origin {
  /**
   * Creates a new origin with the given <code>scheme</code>, <code>host</code>, and
   * <code>port</code>.
   */
  public Origin(String scheme, String host, int port) {
    this.scheme = scheme;
    this.host = host;
    this.port = port;
  }

  /**
   * Creates a new origin whose host is the <a
   * href="https://tools.ietf.org/html/rfc6797#section-8.2">superdomain</a> of this origin's host,
   * or <code>null</code> if this origin's host has no superdomain.
   */
  public Origin getSuperdomainOrigin() {
    int index = host.indexOf('.');
    if (index == -1) {
      return null;
    }
    return new Origin(scheme, host.substring(index + 1), port);
  }

  public String getScheme() {
    return scheme;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String toString() {
    return scheme + "://" + host + ":" + Integer.toString(port);
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Origin)) {
      return false;
    }
    Origin other = (Origin) obj;
    return this.scheme.equals(other.scheme) && this.host.equals(other.host)
      && this.port == other.port;
  }

  public int hashCode() {
    return Objects.hash(scheme, host, port);
  }

  private String scheme;
  private String host;
  private int port;
}
