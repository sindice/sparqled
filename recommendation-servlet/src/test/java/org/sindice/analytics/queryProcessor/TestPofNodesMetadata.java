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
package org.sindice.analytics.queryProcessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;

public class TestPofNodesMetadata {

  private ASTQueryContainer ast;

  @Before
  public void setUp()
  throws Exception {
    ast = null;
    AnalyticsClassAttributes.initClassAttributes(Arrays.asList(AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE));
    ASTVarGenerator.reset();
  }

  @Test
  public void testClassAttribute()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * { ?s a < }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofClassAttribute != null);
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(0, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(0, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(15, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(15, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testClassAttribute2()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * { ?s ?p ?o ; rdf:type < }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofClassAttribute != null);
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(0, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(0, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(54, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(61, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testClassAttribute3()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s ?p [ rdf:type < ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofClassAttribute != null);
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(10, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(17, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testClassAttribute4()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s ?p [ rdf:type <Person>, < ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofClassAttribute != null);
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(10, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(17, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testClassAttribute5()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s ?p [ ?p1 <Person>, < ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofClassAttribute == null);
  }

  @Test
  public void testPOFQName()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s rdf:< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofNode != null);
    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.Qname));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.Qname).size());
    assertEquals("http://type.com/", meta.pofNode.getMetadata().get(SyntaxTreeBuilder.Qname).get(0));

    assertFalse(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
  }

  @Test
  public void testPOFQName2()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s a rdf:< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofNode != null);
    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.Qname));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.Qname).size());
    assertEquals("http://type.com/", meta.pofNode.getMetadata().get(SyntaxTreeBuilder.Qname).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(5, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(5, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testPOFkeyword()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s a rdf< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofNode != null);
    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.Keyword));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.Keyword).size());
    assertEquals("rdf", meta.pofNode.getMetadata().get(SyntaxTreeBuilder.Keyword).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(7, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(9, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));


    assertTrue(meta.pofClassAttribute != null);
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(5, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(5, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testPOFkeyword2()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s a [ <type> rdf< ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofNode != null);
    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.Keyword));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.Keyword).size());
    assertEquals("rdf", meta.pofNode.getMetadata().get(SyntaxTreeBuilder.Keyword).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(16, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(18, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));


    assertTrue(meta.pofClassAttribute != null);
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(9, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(14, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testPOFkeyword3()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s a <Person> ; <type> rdf< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofNode != null);
    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.Keyword));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.Keyword).size());
    assertEquals("rdf", meta.pofNode.getMetadata().get(SyntaxTreeBuilder.Keyword).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(25, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(27, meta.pofNode.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));


    assertTrue(meta.pofClassAttribute != null);
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(18, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(meta.pofClassAttribute.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(23, meta.pofClassAttribute.getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

}
