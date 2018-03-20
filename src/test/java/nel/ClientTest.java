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
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Test;

public class ClientTest {
  @Test
  public void canParseEndpointGroup() throws InvalidHeaderException, MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Origin origin = new Origin("https", "example.com", 443);
    ArrayList<String> headers = new ArrayList<String>();
    // CHECKSTYLE.OFF: OperatorWrap
    headers.add(
        "{\n" +
        "  \"group\": \"nel\",\n" +
        "  \"max-age\":600\n," +
        "  \"endpoints\": [\n" +
        "    {\"url\":\"https://example.com/upload\",\"priority\":1,\"weight\":1},\n" +
        "    {\"url\":\"https://example.com/upload2\",\"priority\":2,\"weight\":3}\n" +
        "  ]\n" +
        "}");
    // CHECKSTYLE.ON: OperatorWrap
    Client expected = new Client(origin);
    EndpointGroup group = new EndpointGroup("nel", false, Duration.standardSeconds(600), I_1300);
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload"), 1, 1));
    group.addEndpoint(new Endpoint(new URL("https://example.com/upload2"), 2, 3));
    expected.addGroup(group);
    Client actual = Client.parseFromReportToHeader(headers, origin, I_1300);
    assertEquals(expected, actual);
  }

  private void checkInvalidHeader(String header)
      throws InvalidHeaderException, MalformedURLException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Origin origin = new Origin("https", "example.com", 443);
    ArrayList<String> headers = new ArrayList<String>();
    headers.add(header);
    // If `header` is invalid (either malformed JSON, or incorrect contents as defined by the
    // Reporting spec), this method should throw an InvalidHeaderException.
    Client.parseFromReportToHeader(headers, origin, I_1300);
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseMissingUrl() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader("{\"max-age\":1, \"endpoints\": [{}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNonStringUrl() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader("{\"max-age\":1, \"endpoints\": [{\"url\":0}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseInsecureUrl() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader("{\"max-age\":1, \"endpoints\": [{\"url\":\"http://insecure/\"}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseMissingMaxAge() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader("{\"endpoints\": [{\"url\":\"https://endpoint/\"}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNonIntegerMaxAge() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader("{\"max-age\":\"\", \"endpoints\": [{\"url\":\"https://endpoint/\"}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNegativeMaxAge() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader("{\"max-age\":-1, \"endpoints\": [{\"url\":\"https://endpoint/\"}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNonStringGroup() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader(
        "{\"max-age\":1, \"group\":0, \"endpoints\": [{\"url\":\"https://endpoint/\"}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNonIntegerPriority() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader(
        "{\"max-age\":1, \"endpoints\": [{\"url\":\"https://endpoint/\",\"priority\":\"\"}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNonIntegerWeight() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader(
        "{\"max-age\":1, \"endpoints\": [{\"url\":\"https://endpoint/\",\"weight\":\"\"}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNegativeWeight() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader(
        "{\"max-age\":1, \"endpoints\": [{\"url\":\"https://endpoint/\",\"weight\":-1}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseZeroWeight() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader(
        "{\"max-age\":1, \"endpoints\": [{\"url\":\"https://endpoint/\",\"weight\":0}]}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseWrappedInList() throws InvalidHeaderException, MalformedURLException {
    checkInvalidHeader(
        "[{\"max-age\":1, \"endpoints\": [{\"url\":\"https://a/\"}]},"
        + "{\"max-age\":1, \"endpoints\": [{\"url\":\"https://b/\"}]}]");
  }

}
