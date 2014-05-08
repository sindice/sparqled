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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;

/**
 * 
 */
public class TestDeNormalizeASTVisitor {

  private ASTQueryContainer ast;

  @Before
  public void setUp()
  throws Exception {
    ast = null;
    ASTVarGenerator.reset();
  }

  @After
  public void tearDown()
  throws Exception {
    checkCreatedNodes(ast);
  }

  private void checkCreatedNodes(SimpleNode n)
  throws InstantiationException, IllegalAccessException {
    final String str = n.toString("org.openrdf.sindice.query.parser.sparql.ast.AST");
    assertEquals(n.getClass().getName(), str.substring(0, str.indexOf(' ') == -1 ? str.length() : str.indexOf(' ')));
    for (int i = 0; i < n.jjtGetNumChildren(); i++) {
      checkCreatedNodes((SimpleNode) n.jjtGetChild(i));
    }
  }

  @Test
  public void testNoDeNormalization()
  throws Exception {
    final String q = "SELECT * WHERE { ?s a ?o }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    final String expectedAst = "QueryContainer\n" +
                               " SelectQuery\n" +
                               "  Select ( * )\n" +
                               "  WhereClause\n" +
                               "   GraphPatternGroup\n" +
                               "    BasicGraphPattern\n" +
                               "     TriplesSameSubjectPath\n" +
                               "      Var (s)\n" +
                               "      PropertyListPath\n" +
                               "       PathAlternative\n" +
                               "        PathSequence\n" +
                               "         PathElt\n" +
                               "          IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                               "       ObjectList\n" +
                               "        Var (o)";

    DeNormalizeAST.process(ast);
    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testQName()
  throws Exception {
    final String q = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT * WHERE { ?s foaf:name ?o }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    final String expectedAst = "QueryContainer\n" +
                               " PrefixDecl (prefix=foaf)\n" +
                               "  IRI (http://xmlns.com/foaf/0.1/)\n" +
                               " SelectQuery\n" +
                               "  Select ( * )\n" +
                               "  WhereClause\n" +
                               "   GraphPatternGroup\n" +
                               "    BasicGraphPattern\n" +
                               "     TriplesSameSubjectPath\n" +
                               "      Var (s)\n" +
                               "      PropertyListPath\n" +
                               "       PathAlternative\n" +
                               "        PathSequence\n" +
                               "         PathElt\n" +
                               "          IRI (http://xmlns.com/foaf/0.1/name)\n" +
                               "       ObjectList\n" +
                               "        Var (o)";

    DeNormalizeAST.process(ast);
    assertEquals(expectedAst, ast.dump(""));
  }

  @Test(expected=MalformedQueryException.class)
  public void testUnknownQName()
  throws Exception {
    final String q = "SELECT * WHERE { ?s foaf:name ?o }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    DeNormalizeAST.process(ast);
  }

  @Test
  public void testBlankNodePropertyList()
  throws Exception {
    final String q = "SELECT * WHERE { [ a <Person>, [ ?p ?o ] ] <writes> ?o,[ a <Publication> ; a [ <title> ?t] ] }";
    final BufferedReader r = new BufferedReader(new FileReader(new File("./src/test/resources/denormalizeTriples/blankNodePropertyList.txt")));
    String expectedAst = "";
    String line;

    try {
      while ((line = r.readLine()) != null) {
        expectedAst += line + "\n";
      }

      ast = SyntaxTreeBuilder.parseQuery(q);

      DeNormalizeAST.process(ast);
      assertEquals(expectedAst, ast.dump("") + "\n");
    } finally {
      r.close();
    }
  }

  @Test
  public void testBlankNodePropertyList2()
  throws Exception {
    final String q = "SELECT * WHERE { [ a <Person> ] <writes> | <author> [ a <Publication> ] }";
    final BufferedReader r = new BufferedReader(new FileReader(new File("./src/test/resources/denormalizeTriples/blankNodePropertyList2.txt")));
    String expectedAst = "";
    String line;

    try {
      while ((line = r.readLine()) != null) {
        expectedAst += line + "\n";
      }
  
      ast = SyntaxTreeBuilder.parseQuery(q);

      DeNormalizeAST.process(ast);
      assertEquals(expectedAst, ast.dump("") + "\n");
    } finally {
      r.close();
    }
  }

  @Test
  public void testObjectList()
  throws Exception {
    final String q = "SELECT * WHERE { ?s a ?o1 , ?o2 , ?o3 }";
    final BufferedReader r = new BufferedReader(new FileReader(new File("./src/test/resources/denormalizeTriples/objectList.txt")));
    String expectedAst = "";
    String line;

    try {
      while ((line = r.readLine()) != null) {
        expectedAst += line + "\n";
      }
      ast = SyntaxTreeBuilder.parseQuery(q);

      DeNormalizeAST.process(ast);
      assertEquals(expectedAst, ast.dump("") + "\n");
    } finally {
      r.close();
    }
  }

  @Test
  public void testVerbSimpleObjectList()
  throws Exception {
    final String q = "SELECT * WHERE { ?s ?p ?o1 , ?o2 , ?o3 }";
    final BufferedReader r = new BufferedReader(new FileReader(new File("./src/test/resources/denormalizeTriples/verbSimpleObjectList.txt")));
    String expectedAst = "";
    String line;

    try {
      while ((line = r.readLine()) != null) {
        expectedAst += line + "\n";
      }
      ast = SyntaxTreeBuilder.parseQuery(q);

      DeNormalizeAST.process(ast);
      assertEquals(expectedAst, ast.dump("") + "\n");
    } finally {
      r.close();
    }
  }

  @Test
  public void testPredicatesPath()
  throws Exception {
    final String q = "SELECT * WHERE { ?s a / <foaf:type> / <dc:type> ?o }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    DeNormalizeAST.process(ast);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(2, vars.length);
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select ( * )\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[0] + ")\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (" + vars[0] + ")\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (foaf:type)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[1] + ")\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (" + vars[1] + ")\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (dc:type)\n" +
                                "       ObjectList\n" +
                                "        Var (o)";

    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testPredicatesPathObjectList()
  throws Exception {
    final String q = "SELECT * WHERE { ?s a / <dc:type> ?o1, ?o2 }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    DeNormalizeAST.process(ast);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(1, vars.length);
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select ( * )\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[0] + ")\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (" + vars[0] + ")\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (dc:type)\n" +
                                "       ObjectList\n" +
                                "        Var (o1)\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (" + vars[0] + ")\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (dc:type)\n" +
                                "       ObjectList\n" +
                                "        Var (o2)";

    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testInversetPredicate()
  throws Exception {
    final String q = "SELECT * WHERE { ?s a / ^<dc:type> ?o }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    DeNormalizeAST.process(ast);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(1, vars.length);
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select ( * )\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[0] + ")\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (o)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (dc:type)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[0] + ")";

    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testInversetPredicateObjectList()
  throws Exception {
    final String q = "SELECT * WHERE { ?s a / ^<dc:type> ?o1, ?o2 }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    DeNormalizeAST.process(ast);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(1, vars.length);
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select ( * )\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[0] + ")\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (o1)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (dc:type)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[0] + ")\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (o2)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (dc:type)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[0] + ")";

    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testPathSequence()
  throws Exception {
    final String q = "SELECT * WHERE { ?s a | <dc:type> ?o1 }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    final BufferedReader r = new BufferedReader(new FileReader(new File("./src/test/resources/denormalizeTriples/pathSequence.txt")));
    String expectedAst = "";
    String line;

    try {
      while ((line = r.readLine()) != null) {
        expectedAst += line + "\n";
      }

      DeNormalizeAST.process(ast);
      assertEquals(expectedAst, ast.dump("") + "\n");
    } finally {
      r.close();
    }
  }

  @Test
  public void testPathSequence2()
  throws Exception {
    final String q = "SELECT * WHERE { ?s <foaf:name> | <dc:name> | <dc:title> ?o1 }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    final BufferedReader r = new BufferedReader(new FileReader(new File("./src/test/resources/denormalizeTriples/pathSequence2.txt")));
    String expectedAst = "";
    String line;

    try {
      while ((line = r.readLine()) != null) {
        expectedAst += line + "\n";
      }

      DeNormalizeAST.process(ast);
      assertEquals(expectedAst, ast.dump("") + "\n");
    } finally {
      r.close();
    }
  }

  @Test
  public void testPathSequence3()
  throws Exception {
    final String q = "SELECT * WHERE { ?s a <foaf:Person>; <dc:name> | <dc:title> ?o1 }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    final BufferedReader r = new BufferedReader(new FileReader(new File("./src/test/resources/denormalizeTriples/pathSequence3.txt")));
    String expectedAst = "";
    String line;

    try {
      while ((line = r.readLine()) != null) {
        expectedAst += line + "\n";
      }

      DeNormalizeAST.process(ast);
      assertEquals(expectedAst, ast.dump("") + "\n");
    } finally {
      r.close();
    }
  }

  @Test
  public void testPathSequenceObjectList()
  throws Exception {
    final String q = "SELECT * WHERE { ?s a | <dc:type> ?o1,?o2 }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    final BufferedReader r = new BufferedReader(new FileReader(new File("./src/test/resources/denormalizeTriples/pathSequenceObjectList.txt")));
    String expectedAst = "";
    String line;

    try {
      while ((line = r.readLine()) != null) {
        expectedAst += line + "\n";
      }

      DeNormalizeAST.process(ast);
      assertEquals(expectedAst, ast.dump("") + "\n");
    } finally {
      r.close();
    }
  }

  @Test
  public void testPathSequenceObjectList2()
  throws Exception {
    final String q = "SELECT * WHERE { ?s a | <foaf:type> / <dc:type> ?o1,?o2 }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    DeNormalizeAST.process(ast);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(1, vars.length);
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select ( * )\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    UnionGraphPattern\n" +
                                "     GraphPatternGroup\n" +
                                "      BasicGraphPattern\n" +
                                "       TriplesSameSubjectPath\n" +
                                "        Var (s)\n" +
                                "        PropertyListPath\n" +
                                "         PathAlternative\n" +
                                "          PathSequence\n" +
                                "           PathElt\n" +
                                "            IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                                "         ObjectList\n" +
                                "          Var (o1)\n" +
                                "       TriplesSameSubjectPath\n" +
                                "        Var (s)\n" +
                                "        PropertyListPath\n" +
                                "         PathAlternative\n" +
                                "          PathSequence\n" +
                                "           PathElt\n" +
                                "            IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                                "         ObjectList\n" +
                                "          Var (o2)\n" +
                                "     GraphPatternGroup\n" +
                                "      BasicGraphPattern\n" +
                                "       TriplesSameSubjectPath\n" +
                                "        Var (s)\n" +
                                "        PropertyListPath\n" +
                                "         PathAlternative\n" +
                                "          PathSequence\n" +
                                "           PathElt\n" +
                                "            IRI (foaf:type)\n" +
                                "         ObjectList\n" +
                                "          Var ("+ vars[0] + ")\n" +
                                "       TriplesSameSubjectPath\n" +
                                "        Var (" + vars[0] + ")\n" +
                                "        PropertyListPath\n" +
                                "         PathAlternative\n" +
                                "          PathSequence\n" +
                                "           PathElt\n" +
                                "            IRI (dc:type)\n" +
                                "         ObjectList\n" +
                                "          Var (o1)\n" +
                                "       TriplesSameSubjectPath\n" +
                                "        Var (" + vars[0] + ")\n" +
                                "        PropertyListPath\n" +
                                "         PathAlternative\n" +
                                "          PathSequence\n" +
                                "           PathElt\n" +
                                "            IRI (dc:type)\n" +
                                "         ObjectList\n" +
                                "          Var (o2)";

    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testNestedSelect()
  throws Exception {
    final String q = "SELECT * WHERE { SERVICE ?s { ?a ?b ?c1,?c2. OPTIONAL { SELECT * { ?e a / <foaf:name> ?f }}} }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    DeNormalizeAST.process(ast);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(1, vars.length);
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select ( * )\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    ServiceGraphPattern\n" +
                                "     Var (s)\n" +
                                "     GraphPatternGroup\n" +
                                "      BasicGraphPattern\n" +
                                "       TriplesSameSubjectPath\n" +
                                "        Var (a)\n" +
                                "        PropertyListPath\n" +
                                "         Var (b)\n" +
                                "         ObjectList\n" +
                                "          Var (c1)\n" +
                                "       TriplesSameSubjectPath\n" +
                                "        Var (a)\n" +
                                "        PropertyListPath\n" +
                                "         Var (b)\n" +
                                "         ObjectList\n" +
                                "          Var (c2)\n" +
                                "      OptionalGraphPattern\n" +
                                "       SelectQuery\n" +
                                "        Select ( * )\n" +
                                "        WhereClause\n" +
                                "         GraphPatternGroup\n" +
                                "          BasicGraphPattern\n" +
                                "           TriplesSameSubjectPath\n" +
                                "            Var (e)\n" +
                                "            PropertyListPath\n" +
                                "             PathAlternative\n" +
                                "              PathSequence\n" +
                                "               PathElt\n" +
                                "                IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                                "             ObjectList\n" +
                                "              Var (" + vars[0] + ")\n" +
                                "           TriplesSameSubjectPath\n" +
                                "            Var (" + vars[0] + ")\n" +
                                "            PropertyListPath\n" +
                                "             PathAlternative\n" +
                                "              PathSequence\n" +
                                "               PathElt\n" +
                                "                IRI (foaf:name)\n" +
                                "             ObjectList\n" +
                                "              Var (f)";

    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void DeNormalizeASTVisitoralizeGraphPatternNotTriples()
  throws Exception {
    final String q = "SELECT * WHERE { SERVICE ?service { ?s a ?o1, ?o2 }\n" +
                                      "OPTIONAL { ?s a ?o1, ?o2 }\n" +
                                      "MINUS { ?s a ?o1, ?o2 }\n" +
                                      "GRAPH ?g { ?s a ?o1, ?o2 }\n" +
                                      "{ ?s ^a ?o2 } UNION { ?s ^a ?o1, ?o2 } }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    final BufferedReader r = new BufferedReader(new FileReader(new File("./src/test/resources/denormalizeTriples/graphPatternNotTriples.txt")));
    String expectedAst = "";
    String line;

    try {
      while ((line = r.readLine()) != null) {
        expectedAst += line + "\n";
      }
      DeNormalizeAST.process(ast);
      assertEquals(expectedAst, ast.dump("") + "\n");
    } finally {
      r.close();
    }
  }

}
