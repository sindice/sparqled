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

import java.util.List;

/**
 * @author diego
 */
public class Hash {

  private static MurmurHash3           hasher = new MurmurHash3();
  protected static final StringBuilder sb     = new StringBuilder();

  public static int get(Object s) {
    return hasher.hash(s);
  }

  public static int get(Object s1, Object s2) {
    sb.setLength(0);
    sb.append(s1).append(s2);
    return hasher.hash(sb.toString());
  }

  public static long getLong(Object s) {
    return hasher.hashLong(s);
  }

  public static long getLong(String s) {
    return hasher.hashLong(s);
  }

  public static long getLong(Object s1, Object s2) {
    sb.setLength(0);
    sb.append(s1).append(s2);
    return hasher.hashLong(sb.toString());
  }

  public static long getLong(List<Object> toHash) {
    sb.setLength(0);
    for (Object s : toHash) {
      sb.append(s);
    }
    return getLong(sb.toString());
  }

}
