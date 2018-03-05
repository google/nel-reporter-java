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

import java.net.URISyntaxException;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Test;

public class ReportTest {
  @Test
  public void canBuildReports() {
    Report report = new Report()
        .setTimestamp(Instant.parse("2018-02-20T13:00:00.000Z"))
        .setUri("https://example.com")
        .setSamplingFraction(0.5)
        .setServerIp("192.0.2.24")
        .setProtocol("h2")
        .setStatusCode(200)
        .setElapsedTime(Duration.millis(1000))
        .setType(Type.OK);
    assertEquals(
        // CHECKSTYLE.OFF: OperatorWrap
        "{\n" +
        "  \"age\": 200,\n" +
        "  \"type\": \"network-error\",\n" +
        "  \"url\": \"https://example.com\",\n" +
        "  \"body\": {\n" +
        "    \"uri\": \"https://example.com\",\n" +
        "    \"sampling-fraction\": 0.5,\n" +
        "    \"server-ip\": \"192.0.2.24\",\n" +
        "    \"protocol\": \"h2\",\n" +
        "    \"status-code\": 200,\n" +
        "    \"elapsed-time\": 1000,\n" +
        "    \"type\": \"ok\"\n" +
        "  }\n" +
        "}",
        // CHECKSTYLE.ON: OperatorWrap
        report.toString(Instant.parse("2018-02-20T13:00:00.200Z")));
  }

  @Test
  public void removesUriFragments() {
    Report report = new Report()
        .setTimestamp(Instant.parse("2018-02-20T13:00:00.000Z"))
        .setUri("https://example.com#fragment")
        .setSamplingFraction(0.5)
        .setServerIp("192.0.2.24")
        .setProtocol("h2")
        .setStatusCode(200)
        .setElapsedTime(Duration.millis(1000))
        .setType(Type.OK);
    assertEquals(
        // CHECKSTYLE.OFF: OperatorWrap
        "{\n" +
        "  \"age\": 200,\n" +
        "  \"type\": \"network-error\",\n" +
        "  \"url\": \"https://example.com\",\n" +
        "  \"body\": {\n" +
        "    \"uri\": \"https://example.com\",\n" +
        "    \"sampling-fraction\": 0.5,\n" +
        "    \"server-ip\": \"192.0.2.24\",\n" +
        "    \"protocol\": \"h2\",\n" +
        "    \"status-code\": 200,\n" +
        "    \"elapsed-time\": 1000,\n" +
        "    \"type\": \"ok\"\n" +
        "  }\n" +
        "}",
        // CHECKSTYLE.ON: OperatorWrap
        report.toString(Instant.parse("2018-02-20T13:00:00.200Z")));
  }
}
