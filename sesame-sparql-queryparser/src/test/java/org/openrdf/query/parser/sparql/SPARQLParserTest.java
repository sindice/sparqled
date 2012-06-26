/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.sindice.query.parser.sparql.SPARQLParser;

/**
 * @author jeen
 */
public class SPARQLParserTest {

  private SPARQLParser parser;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp()
  throws Exception {
    parser = new SPARQLParser();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown()
  throws Exception {
    parser = null;
  }

  /**
   * Test method for
   * {@link org.openrdf.query.parser.sparql.SPARQLParser#parseQuery(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testSourceStringAssignment()
  throws Exception {
    String simpleSparqlQuery = "SELECT * WHERE {?X ?P ?Y }";

    ParsedQuery q = parser.parseQuery(simpleSparqlQuery, null);

    assertNotNull(q);
    assertEquals(simpleSparqlQuery, q.getSourceString());
  }

}
