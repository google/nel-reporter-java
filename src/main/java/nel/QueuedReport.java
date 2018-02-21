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
 * A wrapper around a {@link Report} once it's been added to a {@link ReportingCache}.  Keeps track
 * of some logistics about the report, such as how many times we've tried to upload it.
 */
public class QueuedReport {
  public QueuedReport(Report report, String group) {
    this.report = report;
    this.origin = new Origin(report.getUri());
    this.group = group;
  }

  public Report getReport() {
    return report;
  }

  public Origin getOrigin() {
    return origin;
  }

  public String getGroup() {
    return group;
  }

  private Report report;
  private Origin origin;
  private String group;
}
