/**
 * Copyright (c) 2012 National University of Ireland, Galway. All Rights Reserved.
 *
 *
 * This project is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sindice.core.analytics.commons.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sindice.core.analytics.commons.util.Hash;

/**
 * @author diego
 */
public class TestHash {

  @Test
  public void test() {
    assertTrue(Hash.get("test1") != Hash.get("test2"));
    assertTrue(Hash.get("test1", "test2") != Hash.get("test2"));
    assertTrue(Hash.get("test1", "test2") != Hash.get("test2", "test1"));
    assertTrue(Hash.get("test1test3") == Hash.get("test1", "test3"));
  }

  @Test
  public void testLong() {
    assertTrue(Hash.getLong("test1") != Hash.getLong("test2"));
    assertTrue(Hash.getLong("test1", "test2") != Hash.getLong("test2"));
    assertTrue(Hash.getLong("test1", "test2") != Hash.getLong("test2", "test1"));
    assertTrue(Hash.getLong("test1", "test3") == Hash.getLong("test1", "test3"));
  }

}
