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

/**
 * Defines the <a href="https://wicg.github.io/network-error-logging/#dfn-report-type">type</a> of
 * network error described by a NEL report.
 */
public class Type {
  /** The request did not result in a network error */
  public static Type OK = Type.other("ok");

  /** DNS server was unreachable */
  public static Type DNS_UNREACHABLE = Type.other("dns.unreachable");
  /** DNS server responded but was unable to resolve the address */
  public static Type DNS_NAME_NOT_RESOLVED = Type.other("dns.name_not_resolved");
  /** Request to the DNS server failed due to reasons not covered by previous errors  */
  public static Type DNS_FAILED = Type.other("dns.failed");

  /** TCP connection to the server timed out */
  public static Type TCP_TIMED_OUT = Type.other("tcp.timed_out");
  /** The TCP connection was closed by the server */
  public static Type TCP_CLOSED = Type.other("tcp.closed");
  /** The TCP connection was reset */
  public static Type TCP_RESET = Type.other("tcp.reset");
  /** The TCP connection was refused by the server */
  public static Type TCP_REFUSED = Type.other("tcp.refused");
  /** The TCP connection was aborted */
  public static Type TCP_ABORTED = Type.other("tcp.aborted");
  /** The IP address was invalid */
  public static Type TCP_ADDRESS_INVALID = Type.other("tcp.address_invalid");
  /** The IP address was unreachable */
  public static Type TCP_ADDRESS_UNREACHABLE = Type.other("tcp.address_unreachable");
  /** The TCP connection failed due to reasons not covered by previous errors */
  public static Type TCP_FAILED = Type.other("tcp.failed");

  /** The TLS connection was aborted due to version or cipher mismatch */
  public static Type TLS_VERSION_OR_CIPHER_MISMATCH = Type.other("tls.version_or_cipher_mismatch");
  /** The TLS connection was aborted due to invalid client certificate */
  public static Type TLS_BAD_CLIENT_AUTH_CERT = Type.other("tls.bad_client_auth_cert");
  /** The TLS connection was aborted due to invalid name */
  public static Type TLS_CERT_NAME_INVALID = Type.other("tls.cert.name_invalid");
  /** The TLS connection was aborted due to invalid certificate date */
  public static Type TLS_CERT_DATE_INVALID = Type.other("tls.cert.date_invalid");
  /** The TLS connection was aborted due to invalid issuing authority */
  public static Type TLS_CERT_AUTHORITY_INVALID = Type.other("tls.cert.authority_invalid");
  /** The TLS connection was aborted due to invalid certificate */
  public static Type TLS_CERT_INVALID = Type.other("tls.cert.invalid");
  /** The TLS connection was aborted due to revoked server certificate */
  public static Type TLS_CERT_REVOKED = Type.other("tls.cert.revoked");
  /** The TLS connection was aborted due to a key pinning error */
  public static Type TLS_CERT_PINNED_KEY_NOT_IN_CERT_CHAIN =
    Type.other("tls.cert.pinned_key_not_in_cert_chain");
  /** The TLS connection was aborted due to a TLS protocol error */
  public static Type TLS_PROTOCOL_ERROR = Type.other("tls.protocol.error");
  /** The TLS connection failed due to reasons not covered by previous errors */
  public static Type TLS_FAILED = Type.other("tls.failed");

  /** The connection was aborted due to an HTTP protocol error */
  public static Type HTTP_PROTOCOL_ERROR = Type.other("http.protocol.error");
  /**
   * Response was empty, had a content-length mismatch, had improper encoding, and/or other
   * conditions that prevented user agent from processing the response
   */
  public static Type HTTP_RESPONSE_INVALID = Type.other("http.response.invalid");
  /** The request was aborted due to a detected redirect loop */
  public static Type HTTP_RESPONSE_REDIRECT_LOOP = Type.other("http.response.redirect_loop");
  /** The connection failed due to errors in HTTP protocol not covered by previous errors */
  public static Type HTTP_FAILED = Type.other("http.failed");

  /** User aborted the resource fetch before it was complete */
  public static Type ABANDONED = Type.other("abandoned");
  /** Error type is unknown */
  public static Type UNKNOWN = Type.other("unknown");

  /** An error not covered by any of the cases listed in the standard */
  public static Type other(String type) {
    return new Type(type);
  }

  public String toString() {
    return type;
  }

  private Type(String type) {
    this.type = type;
  }

  private String type;
}
