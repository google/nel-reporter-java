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

import java.net.URL;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Test;

public class NelPolicyTest {
  @Test
  public void canParseNelPolicy() throws InvalidHeaderException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Origin origin = new Origin("https", "example.com", 443);
    // CHECKSTYLE.OFF: OperatorWrap
    String header =
        "{\n" +
        "  \"report-to\": \"nel\",\n" +
        "  \"max-age\":600\n" +
        "}";
    // CHECKSTYLE.ON: OperatorWrap
    NelPolicy expected =
        new NelPolicy(origin, "nel", false, 0.0, 1.0, Duration.standardSeconds(600), I_1300);
    NelPolicy actual = NelPolicy.parseFromNelHeader(header, origin, I_1300);
    assertEquals(expected, actual);
  }

  private void checkInvalidHeader(String header)
      throws InvalidHeaderException {
    final Instant I_1300 = Instant.parse("2018-02-20T13:00:00.000Z");
    final Origin origin = new Origin("https", "example.com", 443);
    // If `header` is invalid (either malformed JSON, or incorrect contents as defined by the
    // Reporting spec), this method should throw an InvalidHeaderException.
    NelPolicy.parseFromNelHeader(header, origin, I_1300);
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseMissingMaxAge() throws InvalidHeaderException {
    checkInvalidHeader("{\"report-to\": \"nel\"}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNonIntegerMaxAge() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":\"\", \"report-to\": \"nel\"}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNegativeMaxAge() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":-1, \"report-to\": \"nel\"}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseMissingReportTo() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":1}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNonStringReportTo() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":1, \"report-to\":0}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNonBooleanIncludeSubdomains() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":1, \"report-to\":\"nel\", \"include-subdomains\":\"\"}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNonDoubleSuccessFraction() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":1, \"report-to\": \"nel\", \"success-fraction\":\"\"}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseLowSuccessFraction() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":1, \"report-to\": \"nel\", \"success-fraction\":-0.5}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseHighSuccessFraction() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":1, \"report-to\": \"nel\", \"success-fraction\":1.5}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseNonDoubleFailureFraction() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":1, \"report-to\": \"nel\", \"failure-fraction\":\"\"}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseLowFailureFraction() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":1, \"report-to\": \"nel\", \"failure-fraction\":-0.5}");
  }

  @Test(expected = InvalidHeaderException.class)
  public void cannotParseHighFailureFraction() throws InvalidHeaderException {
    checkInvalidHeader("{\"max-age\":1, \"report-to\": \"nel\", \"failure-fraction\":1.5}");
  }

}
