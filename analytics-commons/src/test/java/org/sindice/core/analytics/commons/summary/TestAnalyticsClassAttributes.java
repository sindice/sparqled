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
/**
 * @project analytics-core
 * @author Campinas Stephane [ 4 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.core.analytics.commons.summary;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;


/**
 * 
 */
public class TestAnalyticsClassAttributes {

  public static final String PRED_1  = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  public static final String PRED_2  = "http://opengraph.org/schema/type";
  public static final String PRED_3  = "http://opengraphprotocol.org/schema/type";
  public static final String PRED_4  = "http://ogp.me/ns#type";
  public static final String PRED_5  = "http://purl.org/dc/elements/1.1/type";
  public static final String PRED_6  = "http://dbpedia.org/property/type";
  public static final String PRED_7  = "http://dbpedia.org/ontology/type";

  public static final String PRED_1B = "http://www.w3.org/1999/02/22-rdf-syntax-ns#TYPE";
  public static final String PRED_2B = "opengraph.org/schema/type";
  public static final String PRED_3B = "http://www.sindice.com/type";

  @Before
  public void setUp()
  throws Exception {
    AnalyticsClassAttributes.initClassAttributes(new String[] {
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
      "http://opengraphprotocol.org/schema/type",
      "http://opengraph.org/schema/type",
      "http://ogp.me/ns#type",
      "http://purl.org/dc/elements/1.1/type",
      "http://purl.org/stuff/rev#type", //Added 19 Oct 2011
      "http://purl.org/dc/terms/type", //Added 19 Oct 2011
      "http://dbpedia.org/property/type",
      "http://dbpedia.org/ontology/type",
      "http://dbpedia.org/ontology/Organisation/type", //Added 25 Oct 2011
      "http://xmlns.com/foaf/0.1/type", //Added 25 Oct 2011
    });
    AnalyticsClassAttributes.enableLiteralTypeNormalisation();
  }

  @Test
  /**
   * Verifies Analytics commons recognises correct predicates to define a class
   */
  public void testClassDefinitions() {

    assertEquals(true, AnalyticsClassAttributes.isClass(PRED_1));
    assertEquals(true, AnalyticsClassAttributes.isClass(PRED_2));
    assertEquals(true, AnalyticsClassAttributes.isClass(PRED_3));
    assertEquals(true, AnalyticsClassAttributes.isClass(PRED_4));
    assertEquals(true, AnalyticsClassAttributes.isClass(PRED_5));
    assertEquals(true, AnalyticsClassAttributes.isClass(PRED_6));
    assertEquals(true, AnalyticsClassAttributes.isClass(PRED_7));

    assertEquals(false, AnalyticsClassAttributes.isClass(PRED_1B));
    assertEquals(false, AnalyticsClassAttributes.isClass(PRED_2B));
    assertEquals(false, AnalyticsClassAttributes.isClass(PRED_3B));

    assertEquals(true, AnalyticsClassAttributes.isClassWithAngleBrackets("<" + PRED_1 + ">"));
    assertEquals(true, AnalyticsClassAttributes.isClassWithAngleBrackets("<" + PRED_2 + ">"));
    assertEquals(false, AnalyticsClassAttributes.isClassWithAngleBrackets("<" + PRED_1B + ">"));
    assertEquals(false, AnalyticsClassAttributes.isClassWithAngleBrackets("<" + PRED_2B + ">"));

  }

  public static final String TYPE1      = "\"   MOVIE \"";
  public static final String TYPE1_NORM = "\"movie\"";
  public static final String TYPE2      = "\"   class AS  SEntence   \"";
  public static final String TYPE2_NORM = "\"class as sentence\"";

  @Test
  /**
   * Verifies Analytics Commons correctly normalizes classes as strings
   */
  public void testNormalizeTypeString() {
    assertEquals(TYPE1_NORM, AnalyticsClassAttributes.normalizeLiteralType(TYPE1));
    assertEquals(TYPE2_NORM, AnalyticsClassAttributes.normalizeLiteralType(TYPE2));
    final StringBuilder sb = new StringBuilder();
    assertEquals(TYPE1_NORM, AnalyticsClassAttributes.normalizeLiteralTypeLabel(sb, TYPE1.substring(1, TYPE1.length() - 1)));
    assertEquals(TYPE2_NORM, AnalyticsClassAttributes.normalizeLiteralTypeLabel(sb, TYPE2.substring(1, TYPE2.length() - 1)));
  }

  @Test
  public void testLiteralType()
  throws Exception {
    assertEquals("\"test test\"", AnalyticsClassAttributes.normalizeLiteralType("\"    test  test  \""));
    assertEquals("\"test test\"", AnalyticsClassAttributes.normalizeLiteralType("\" test \n test  \""));
    assertEquals("\"test test\"", AnalyticsClassAttributes.normalizeLiteralType("\" test \t\n TEST  \""));
    final StringBuilder sb = new StringBuilder();
    assertEquals("\"test test\"", AnalyticsClassAttributes.normalizeLiteralTypeLabel(sb, "    test  test  "));
    assertEquals("\"test test\"", AnalyticsClassAttributes.normalizeLiteralTypeLabel(sb, "    test \n test  "));
    assertEquals("\"test test\"", AnalyticsClassAttributes.normalizeLiteralTypeLabel(sb, "\t  test \t\n TEST \n \n"));
    assertEquals("\"en\"", AnalyticsClassAttributes.normalizeLiteralTypeLabel(sb, "en"));
  }

}
