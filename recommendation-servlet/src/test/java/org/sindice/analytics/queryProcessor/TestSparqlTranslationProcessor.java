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
 * @project sparql-editor-servlet
 * @author Campinas Stephane [ 23 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.analytics.queryProcessor.ASTVarGenerator;
import org.sindice.analytics.queryProcessor.PipelineObject;
import org.sindice.analytics.queryProcessor.RecommendationType;
import org.sindice.analytics.queryProcessor.SparqlToDGSQueryInterface;
import org.sindice.analytics.queryProcessor.SparqlTranslationProcessor;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.AnalyticsVocab;


/**
 * 
 */
public class TestSparqlTranslationProcessor {

  private ASTQueryContainer   ast;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp()
  throws Exception {
    ast = null;
    AnalyticsClassAttributes.initClassAttributes(Arrays.asList(AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE));
    ASTVarGenerator.reset();
  }

  @Test
  public void testTranslateSimplePredicate()
  throws Exception {
    final String query = "SELECT * { ?s <name> ?o1 . }";
    ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    po = new SparqlTranslationProcessor().process(po);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(1, vars.length);
    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  DatasetClause (named=false)\n" +
                            "   IRI (" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + ")\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "       ObjectList\n" +
                            "        Var (s)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "       ObjectList\n" +
                            "        RDFLiteral\n" +
                            "         String (" + AnalyticsVocab.BLANK_NODE_COLLECTION + ")\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        IRI (name)";

    assertEquals(expected, po.getAst().dump(""));
  }

  @Test
  public void testTranslateClassTriplePattern()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> . }";
    ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    po = new SparqlTranslationProcessor().process(po);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(1, vars.length);
    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  DatasetClause (named=false)\n" +
                            "   IRI (" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + ")\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (s)\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (" + vars[0] + ")\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        IRI (Person)";

    assertEquals(expected, po.getAst().dump(""));
  }

  @Test
  public void testTranslateDataset()
  throws Exception {
    final String query = "SELECT * FROM <http://ste.live.com> { Graph <http://sindice.com> { ?s a <Person> . ?s ?p ?o } ." +
                                                              " ?s a <Person> . ?s ?p ?o }";
    ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    po = new SparqlTranslationProcessor().process(po);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(4, vars.length);
    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  DatasetClause (named=false)\n" +
                            "   IRI (" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + ")\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    GraphPatternGroup\n" +
                            "     BasicGraphPattern\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (s)\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.DOMAIN_URI + ")\n" +
                            "        ObjectList\n" +
                            "         IRI (" + AnalyticsVocab.DOMAIN_URI_PREFIX + "sindice.com)\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (s)\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "        ObjectList\n" +
                            "         Var (" + vars[0] + ")\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[0] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "        ObjectList\n" +
                            "         IRI (Person)\n" +
                            "      Constraint\n" +
                            "       Not\n" +
                            "        IsLiteral\n" +
                            "         Var (o)\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[1] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "        ObjectList\n" +
                            "         Var (s)\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[1] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "        ObjectList\n" +
                            "         Var (o)\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[1] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "        ObjectList\n" +
                            "         Var (p)\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[1] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.EDGE_PUBLISHED_IN + ")\n" +
                            "        ObjectList\n" +
                            "         IRI (" + AnalyticsVocab.DOMAIN_URI_PREFIX + "sindice.com)\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (s)\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.DOMAIN_URI + ")\n" +
                            "       ObjectList\n" +
                            "        IRI (" + AnalyticsVocab.DOMAIN_URI_PREFIX + "live.com)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (s)\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (" + vars[2] + ")\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[2] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        IRI (Person)\n" +
                            "     Constraint\n" +
                            "      Not\n" +
                            "       IsLiteral\n" +
                            "        Var (o)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[3] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "       ObjectList\n" +
                            "        Var (s)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[3] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "       ObjectList\n" +
                            "        Var (o)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[3] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (p)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[3] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_PUBLISHED_IN + ")\n" +
                            "       ObjectList\n" +
                            "        IRI (" + AnalyticsVocab.DOMAIN_URI_PREFIX + "live.com)";

    assertEquals(expected, po.getAst().dump(""));
  }

  @Test
  public void testTranslateDataset2()
  throws Exception {
    final String query = "SELECT * { Graph <http://sindice.com> { ?s a <Person> . OPTIONAL {?s ?p ?o } } }";
    ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    po = new SparqlTranslationProcessor().process(po);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(2, vars.length);
    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  DatasetClause (named=false)\n" +
                            "   IRI (" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + ")\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    GraphPatternGroup\n" +
                            "     BasicGraphPattern\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (s)\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.DOMAIN_URI + ")\n" +
                            "        ObjectList\n" +
                            "         IRI (" + AnalyticsVocab.DOMAIN_URI_PREFIX + "sindice.com)\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (s)\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "        ObjectList\n" +
                            "         Var (" + vars[0] + ")\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[0] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "        ObjectList\n" +
                            "         IRI (Person)\n" +
                            "     OptionalGraphPattern\n" +
                            "      BasicGraphPattern\n" +
                            "       TriplesSameSubjectPath\n" +
                            "        Var (" + vars[1] + ")\n" +
                            "        PropertyListPath\n" +
                            "         PathAlternative\n" +
                            "          PathSequence\n" +
                            "           PathElt\n" +
                            "            IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "         ObjectList\n" +
                            "          Var (s)\n" +
                            "       TriplesSameSubjectPath\n" +
                            "        Var (" + vars[1] + ")\n" +
                            "        PropertyListPath\n" +
                            "         PathAlternative\n" +
                            "          PathSequence\n" +
                            "           PathElt\n" +
                            "            IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "         ObjectList\n" +
                            "          RDFLiteral\n" +
                            "           String (" + AnalyticsVocab.BLANK_NODE_COLLECTION + ")\n" +
                            "       TriplesSameSubjectPath\n" +
                            "        Var (" + vars[1] + ")\n" +
                            "        PropertyListPath\n" +
                            "         PathAlternative\n" +
                            "          PathSequence\n" +
                            "           PathElt\n" +
                            "            IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "         ObjectList\n" +
                            "          Var (p)\n" +
                            "       TriplesSameSubjectPath\n" +
                            "        Var (" + vars[1] + ")\n" +
                            "        PropertyListPath\n" +
                            "         PathAlternative\n" +
                            "          PathSequence\n" +
                            "           PathElt\n" +
                            "            IRI (" + AnalyticsVocab.EDGE_PUBLISHED_IN + ")\n" +
                            "         ObjectList\n" +
                            "          IRI (" + AnalyticsVocab.DOMAIN_URI_PREFIX + "sindice.com)";

    assertEquals(expected, po.getAst().dump(""));
  }

  @Test
  public void testTranslateDatasetPOF()
  throws Exception {
    final String query = "SELECT * { GRAPH ?POF { ?s a <Person> . ?s <age> ?age } }";
    ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    po = new SparqlTranslationProcessor().process(po);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(2, vars.length);
    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( distinct * )\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.CARDINALITY_VAR + ")\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "  DatasetClause (named=false)\n" +
                            "   IRI (" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + ")\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    GraphPatternGroup\n" +
                            "     BasicGraphPattern\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.DOMAIN_URI + ")\n" +
                            "        ObjectList\n" +
                            "         Var (POF)\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.CARDINALITY + ")\n" +
                            "        ObjectList\n" +
                            "         Var (" + SparqlToDGSQueryInterface.CARDINALITY_VAR + ")\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "        ObjectList\n" +
                            "         Var (" + vars[0] + ")\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[0] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "        ObjectList\n" +
                            "         IRI (Person)\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[1] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "        ObjectList\n" +
                            "         Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[1] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "        ObjectList\n" +
                            "         RDFLiteral\n" +
                            "          String (" + AnalyticsVocab.BLANK_NODE_COLLECTION + ")\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[1] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "        ObjectList\n" +
                            "         IRI (age)\n" +
                            "      TriplesSameSubjectPath\n" +
                            "       Var (" + vars[1] + ")\n" +
                            "       PropertyListPath\n" +
                            "        PathAlternative\n" +
                            "         PathSequence\n" +
                            "          PathElt\n" +
                            "           IRI (" + AnalyticsVocab.EDGE_PUBLISHED_IN + ")\n" +
                            "        ObjectList\n" +
                            "         Var (POF)";

    assertEquals(expected, po.getAst().dump(""));
  }

  @Test
  public void testPOFonClass() throws Exception {
    final String query = "SELECT * { ?a a ?POF . }";
    ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    po = new SparqlTranslationProcessor().process(po);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(2, vars.length);
    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( distinct * )\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.CLASS_ATTRIBUTE_CARD_VAR + ")\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.CLASS_ATTRIBUTE_LABEL_VAR + ")\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.CARDINALITY_VAR + ")\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "  DatasetClause (named=false)\n" +
                            "   IRI (" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + ")\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (" + vars[0] + ")\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (POF)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.CARDINALITY + ")\n" +
                            "       ObjectList\n" +
                            "        Var (" + SparqlToDGSQueryInterface.CARDINALITY_VAR + ")\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.TYPE + ")\n" +
                            "       ObjectList\n" +
                            "        Var (" + vars[1] + ")\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[1] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.CARDINALITY + ")\n" +
                            "       ObjectList\n" +
                            "        Var (" + SparqlToDGSQueryInterface.CLASS_ATTRIBUTE_CARD_VAR + ")\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[1] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (" + SparqlToDGSQueryInterface.CLASS_ATTRIBUTE_LABEL_VAR + ")";

    assertEquals(expected, po.getAst().dump(""));
  }

  @Test
  public void testConnectedObject1() throws Exception {
    final String query = "SELECT * { ?s ?p ?o . ?a ?POF ?s . }";
    ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    po = new SparqlTranslationProcessor().process(po);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(2, vars.length);
    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( distinct * )\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.CARDINALITY_VAR + ")\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "  DatasetClause (named=false)\n" +
                            "   IRI (" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + ")\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "       ObjectList\n" +
                            "        Var (s)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "       ObjectList\n" +
                            "        RDFLiteral\n" +
                            "         String (" + AnalyticsVocab.BLANK_NODE_COLLECTION + ")\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (p)\n" +
                            "     Constraint\n" +
                            "      Not\n" +
                            "       IsLiteral\n" +
                            "        Var (s)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "       ObjectList\n" +
                            "        Var (a)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "       ObjectList\n" +
                            "        Var (s)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (POF)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.CARDINALITY + ")\n" +
                            "       ObjectList\n" +
                            "        Var (POFcardinality)";

    assertEquals(expected, po.getAst().dump(""));
  }

  @Test
  public void testConnectedObject2() throws Exception {
    final String query = "SELECT * { ?s ?p ?o . ?a ?POF ?o . }";
    ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    po = new SparqlTranslationProcessor().process(po);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(2, vars.length);
    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( distinct * )\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.CARDINALITY_VAR + ")\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "  DatasetClause (named=false)\n" +
                            "   IRI (" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + ")\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     Constraint\n" +
                            "      Not\n" +
                            "       IsLiteral\n" +
                            "        Var (o)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "       ObjectList\n" +
                            "        Var (s)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "       ObjectList\n" +
                            "        Var (o)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (p)\n" +
                            "     Constraint\n" +
                            "      Not\n" +
                            "       IsLiteral\n" +
                            "        Var (o)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "       ObjectList\n" +
                            "        Var (a)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "       ObjectList\n" +
                            "        Var (o)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (POF)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.CARDINALITY + ")\n" +
                            "       ObjectList\n" +
                            "        Var (POFcardinality)";

    assertEquals(expected, po.getAst().dump(""));
  }

  @Test
  public void testConnectedObject3() throws Exception {
    final String query = "SELECT * { ?s ?p ?o . ?a ?POF ?p . }";
    ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    po = new SparqlTranslationProcessor().process(po);

    final String[] vars = ASTVarGenerator.getCurrentVarNames();
    assertEquals(2, vars.length);
    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( distinct * )\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.CARDINALITY_VAR + ")\n" +
                            "   ProjectionElem\n" +
                            "    Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "  DatasetClause (named=false)\n" +
                            "   IRI (" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + ")\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "       ObjectList\n" +
                            "        Var (s)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "       ObjectList\n" +
                            "        RDFLiteral\n" +
                            "         String (" + AnalyticsVocab.BLANK_NODE_COLLECTION + ")\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + vars[0] + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (p)\n" +
                            "     Constraint\n" +
                            "      Not\n" +
                            "       IsLiteral\n" +
                            "        Var (p)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_SOURCE + ")\n" +
                            "       ObjectList\n" +
                            "        Var (a)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.EDGE_TARGET + ")\n" +
                            "       ObjectList\n" +
                            "        Var (p)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.LABEL + ")\n" +
                            "       ObjectList\n" +
                            "        Var (POF)\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (" + SparqlToDGSQueryInterface.POF_RESOURCE + ")\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (" + AnalyticsVocab.CARDINALITY + ")\n" +
                            "       ObjectList\n" +
                            "        Var (POFcardinality)";

    assertEquals(expected, po.getAst().dump(""));
  }

}
