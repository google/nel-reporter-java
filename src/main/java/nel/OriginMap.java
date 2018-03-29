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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@link HashMap} specialization that only works with {@link Origin} as the key.  Includes a
 * {@link #getAll} method that knows how to look up entries for all superdomains of an origin.
 */
public class OriginMap<V> extends HashMap<Origin, V> {
  /** Creates a new, empty map. */
  public OriginMap() {
    super();
  }

  /**
   * Returns all of the entries that cover a particular origin.  This includes any entry for the
   * origin itself, as well as the entries for all of the origin's superdomains.  The elements of
   * the list will be ordered, with more specific matches occurring first.
   */
  public Iterable<V> getAll(Origin origin) {
    return new AllIterable(origin);
  }

  private class AllIterable implements Iterable<V> {
    private AllIterable(Origin origin) {
      this.origin = origin;
    }

    public Iterator<V> iterator() {
      return new AllIterator(origin);
    }

    private Origin origin;
  }

  private class AllIterator implements Iterator<V> {
    private AllIterator(Origin start) {
      this.origin = start;
      this.nextElement = null;
      advance();
    }

    private void advance() {
      while (origin != null) {
        nextElement = get(origin);
        origin = origin.getSuperdomainOrigin();
        if (nextElement != null) {
          return;
        }
      }
    }

    @Override
    public boolean hasNext() {
      return origin != null;
    }

    @Override
    public V next() {
      if (origin == null) {
        throw new NoSuchElementException();
      }
      V result = nextElement;
      advance();
      return result;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove from OriginMap#getAll");
    }

    private Origin origin;
    private V nextElement;
  }

}
