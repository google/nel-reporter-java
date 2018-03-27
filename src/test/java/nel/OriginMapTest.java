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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

public class OriginMapTest {
  private <V> ArrayList<V> list(Iterable<V> iterable) {
    ArrayList<V> result = new ArrayList<V>();
    for (V element : iterable) {
      result.add(element);
    }
    return result;
  }

  @Test
  public void canGet() {
    final Origin origin = new Origin("https", "example.com", 443);
    OriginMap<String> map = new OriginMap<String>();
    map.put(origin, "test");
    assertEquals(Arrays.asList("test"), list(map.getAll(origin)));
  }

  @Test
  public void canGetForSubdomain() {
    final Origin origin = new Origin("https", "foo.example.com", 443);
    final Origin superdomainOrigin = new Origin("https", "example.com", 443);
    OriginMap<String> map = new OriginMap<String>();
    map.put(superdomainOrigin, "test");
    assertEquals(Arrays.asList("test"), list(map.getAll(origin)));
  }

  @Test
  public void canGetAllForSubdomain() {
    final Origin origin = new Origin("https", "foo.example.com", 443);
    final Origin superdomainOrigin = new Origin("https", "example.com", 443);
    OriginMap<String> map = new OriginMap<String>();
    map.put(superdomainOrigin, "test");
    map.put(origin, "test2");
    assertEquals(Arrays.asList("test2", "test"), list(map.getAll(origin)));
  }

}
