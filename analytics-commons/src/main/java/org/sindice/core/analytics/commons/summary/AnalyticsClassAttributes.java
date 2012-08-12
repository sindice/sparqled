/*******************************************************************************
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
 *******************************************************************************/
package org.sindice.core.analytics.commons.summary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author thomas
 */
public class AnalyticsClassAttributes {

  public static final Logger logger                  = LoggerFactory.getLogger(AnalyticsClassAttributes.class);

  public static final String DEFAULT_CLASS_ATTRIBUTE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

  private static boolean     normalizeLiteralType    = false;

  /**
   * Because of the class {@link HashTypesAggregator}, the TYPES cannot be
   * larger than 256
   */
  public static List<String> CLASS_ATTRIBUTES;

  public static void enableLiteralTypeNormalisation() {
    normalizeLiteralType = true;
  }

  public static final void initClassAttributes(String[] attributes) {
    initClassAttributes(Arrays.asList(attributes));
  }

  public static final void initClassAttributes(List<String> attributes) {
    Collections.sort(attributes);
    CLASS_ATTRIBUTES = Collections.unmodifiableList(attributes);
    logger.debug("Loaded the Class Attributes: {}", CLASS_ATTRIBUTES);
  }

  public static String normalizeLiteralType(String input) {
    if (!normalizeLiteralType) {
      return input;
    }

    // normalise the literals of type
    int startIndex = input.indexOf("\"");
    int endIndex = input.lastIndexOf("\"");

    if (endIndex - startIndex > 0) {
      // Remove language identifier
      input = input.substring(startIndex + 1, endIndex);
      // Remove extra whitespace and lowercase
      input = input.trim().toLowerCase();
      // Replace any whitespace with a single space
      input = input.replaceAll("\\s+", " ");
      // add quotation marks back on
      if (!input.isEmpty()) {
        return "\"" + input + "\"";
      }
    }
    return null;
  }

  public static String normalizeLiteralTypeLabel(StringBuilder sb, String input) {
    if (!normalizeLiteralType) {
      return "\"" + input + "\""; // add the double quotes
    }

    int start = 0;
    int end = 0;

    if (input.isEmpty()) {
      return null;
    }
    // normalise the literals of type
    input = input.toLowerCase();
    for (int i = 0; i < input.length(); i++) {
      if (!isWhitespace(input.charAt(i))) {
        start = i;
        break;
      }
    }
    for (int i = input.length() - 1; i >= 0; i--) {
      if (!isWhitespace(input.charAt(i))) {
        end = i;
        break;
      }
    }

    int nWS = 0;
    sb.setLength(0);
    sb.append('"');
    for (int i = start; i <= end; i++) {
      if (isWhitespace(input.charAt(i))) {
        nWS++;
      } else {
        nWS = 0;
      }
      if (nWS <= 1) {
        sb.append(input.charAt(i));
      }
    }
    sb.append('"');
    // add quotation marks back on
    if (sb.length() != 2) {
      return sb.toString();
    }
    return null;
  }

  private static boolean isWhitespace(char c) {
    if (c == ' ' || c == '\t' || c == '\n' || c == '\u000B' ||
        c == '\f' || c == '\r') {
      return true;
    }
    return false;
  }

  public static boolean isClass(String predicate) {
    return CLASS_ATTRIBUTES.contains(predicate);
  }

  public static boolean isClassWithAngleBrackets(String predicate) {
    String pred = predicate;
    pred = pred.substring(1, pred.length() - 1);
    return CLASS_ATTRIBUTES.contains(pred);
  }

}
