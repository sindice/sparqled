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
package org.sindice.core.analytics.commons.summary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class contains the definition of class attributes.
 * 
 * <p>
 * 
 * Class attributes are predicates which define the associated object
 * as a class. The default predicate used as a class attribute is
 * <code>rdf:type</code>.
 */
public class AnalyticsClassAttributes {

  public static final String  DEFAULT = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

  private static List<String> classAttributes;

  public static final void initClassAttributes(String... attributes) {
    initClassAttributes(Arrays.asList(attributes));
  }

  public static final void initClassAttributes(List<String> attributes) {
    Collections.sort(attributes);
    classAttributes = Collections.unmodifiableList(attributes);
  }

  public static boolean isClass(String predicate) {
    return getClassAttributes().contains(predicate);
  }

  public static List<String> getClassAttributes() {
    if (classAttributes == null) {
      classAttributes = Arrays.asList(DEFAULT);
    }
    return classAttributes;
  }

}
