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
package org.sindice.analytics.queryProcessor;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.analytics.backend.DGSQueryResultProcessor;
import org.sindice.analytics.backend.DGSQueryResultProcessor.Context;
import org.sindice.analytics.queryProcessor.AST2TextTranslator;
import org.sindice.analytics.queryProcessor.AddOneHopPropertyPath;
import org.sindice.analytics.queryProcessor.PipelineObject;
import org.sindice.analytics.queryProcessor.RecommendationType;
import org.sindice.analytics.queryProcessor.SparqlTranslationProcessor;
import org.sindice.analytics.ranking.Label;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.AnalyticsVocab;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.core.sesame.backend.testHelper.SesameNxParser;

/**
 * @author bibhas [Jul 16, 2012]
 * @email bibhas.das@deri.org
 * 
 */
public class TestAddOneHopPath {
  private static final SesameBackend<Label, Context> backend = SesameBackendFactory
      .getDgsBackend(BackendType.MEMORY, new DGSQueryResultProcessor());
  private static final String dgsInput = "./src/test/resources/HopsBackend/test-data-hops.nt.gz";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  @BeforeClass
  public static void init() throws SesameBackendException,
      FileNotFoundException, IOException, RDFParseException,
      RepositoryException {
    final BufferedReader dgsInputReader = new BufferedReader(
        new InputStreamReader(
            new GZIPInputStream(new FileInputStream(dgsInput))));
    backend.initConnection();

    AnalyticsClassAttributes
        .initClassAttributes(new String[] { AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE });
    AnalyticsVocab.resetToDefaults();

    RDFParserRegistry.getInstance().add(new RDFParserFactory() {

      @Override
      public RDFFormat getRDFFormat() {
        return SesameNxParser.nquadsFormat;
      }

      @Override
      public RDFParser getParser() {
        return new SesameNxParser();
      }

    });
    try {
      addNQuads(dgsInputReader);
    } finally {
      dgsInputReader.close();
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @AfterClass
  public static void release() throws IOException, SesameBackendException {
    backend.closeConnection();
  }

  @Test
  public void testSimplePredicate() throws Exception {
    final String query = "SELECT ?POF WHERE {" + "  ?s ?POF ?p ." + "}";
    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null,
        RecommendationType.PREDICATE, null, 3, backend);
    AddOneHopPropertyPath ahp = new AddOneHopPropertyPath(new SparqlTranslationProcessor());
    po = ahp.process(po);

    assertEquals(0, po.getVarsToProject().size());
  }

  @Test
  public void testMaxHops() throws Exception {
    final String query = "SELECT ?POF WHERE {"
        + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Assay> ."
        + "  ?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Canonical_smile> ."
        + "  ?s ?POF ?p ." + "}";
    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null,
        RecommendationType.PREDICATE, null, 2, backend);
    AddOneHopPropertyPath ahp = new AddOneHopPropertyPath(new SparqlTranslationProcessor());
    po = ahp.process(po);

    assertEquals(2, po.getVarsToProject().size());
    final String expected = "SELECT ?POF WHERE {\n"
        + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Assay> .\n"
        + "  ?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Canonical_smile> .\n"
        + "  ?s ?POF ?" + po.getVarsToProject().get(0) + " .\n" + "  ?"
        + po.getVarsToProject().get(0) + " ?" + po.getVarsToProject().get(1)
        + " ?p .\n" + "}\n";
    assertEquals(expected, AST2TextTranslator.translate(po.getAst()));
  }

  @Test
  public void testDirectLink() throws Exception {
    final String query = "SELECT ?POF WHERE {"
        + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Assay> ."
        + "  ?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Activity> ."
        + "  ?s ?POF ?p ." + "}";
    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null,
        RecommendationType.PREDICATE, null, 2, backend);
    AddOneHopPropertyPath ahp = new AddOneHopPropertyPath(new SparqlTranslationProcessor());
    po = ahp.process(po);

    assertEquals(0, po.getVarsToProject().size());
    final String expected = "SELECT ?POF WHERE {\n"
        + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Assay> .\n"
        + "  ?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Activity> .\n"
        + "  ?s ?POF ?p .\n" + "}\n";
    assertEquals(expected, AST2TextTranslator.translate(po.getAst()));
  }

  @Test
  public void testTwoHops() throws Exception {
    final String query = "SELECT ?POF WHERE {\n"
        + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Assay> .\n"
        + "  ?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Compound> .\n"
        + "  ?s ?POF ?p .\n" + "}\n";
    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null,
        RecommendationType.PREDICATE, null, 3, backend);
    AddOneHopPropertyPath ahp = new AddOneHopPropertyPath(new SparqlTranslationProcessor());
    po = ahp.process(po);

    assertEquals(2, po.getVarsToProject().size());
    final String expected = "SELECT ?POF WHERE {\n"
        + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Assay> .\n"
        + "  ?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Compound> .\n"
        + "  ?s ?POF ?" + po.getVarsToProject().get(0) + " .\n" + "  ?"
        + po.getVarsToProject().get(0) + " ?" + po.getVarsToProject().get(1)
        + " ?p .\n" + "}\n";
    assertEquals(expected, AST2TextTranslator.translate(po.getAst()));
  }

  @Test
  public void testThreeHops() throws Exception {
    final String query = "SELECT ?POF WHERE {"
        + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Assay> ."
        + "  ?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Canonical_smile> ."
        + "  ?s ?POF ?p ." + "}";
    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null,
        RecommendationType.PREDICATE, null, 3, backend);
    AddOneHopPropertyPath ahp = new AddOneHopPropertyPath(new SparqlTranslationProcessor());
    po = ahp.process(po);

    assertEquals(4, po.getVarsToProject().size());
    final String expected = "SELECT ?POF WHERE {\n"
        + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Assay> .\n"
        + "  ?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Canonical_smile> .\n"
        + "  ?s ?POF ?" + po.getVarsToProject().get(0) + " .\n" + "  ?"
        + po.getVarsToProject().get(0) + " ?" + po.getVarsToProject().get(1)
        + " ?" + po.getVarsToProject().get(2) + " .\n" + "  ?"
        + po.getVarsToProject().get(2) + " ?" + po.getVarsToProject().get(3)
        + " ?p .\n" + "}\n";
    assertEquals(expected, AST2TextTranslator.translate(po.getAst()));
  }

  @Test
  public void testMultipleWithType() throws Exception {
    final String query = "SELECT ?POF WHERE {"
        + "  ?activity <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Activity> ."
        + "  ?smile <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Canonical_smile> ."
        + "  ?assay <http://www.example.com/#activity> ?activity ."
        + "  ?compound <http://www.example.com/#smile> ?smile ."
        + "  ?assay ?POF ?compound ." + "}";

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null,
        RecommendationType.PREDICATE, null, 3, backend);
    AddOneHopPropertyPath ahp = new AddOneHopPropertyPath(new SparqlTranslationProcessor());
    po = ahp.process(po);

    assertEquals(2, po.getVarsToProject().size());

    final String expected = "SELECT ?POF WHERE {\n"
        + "  ?activity <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Activity> .\n"
        + "  ?smile <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Canonical_smile> .\n"
        + "  ?assay <http://www.example.com/#activity> ?activity .\n"
        + "  ?compound <http://www.example.com/#smile> ?smile .\n"
        + "  ?assay ?POF ?" + po.getVarsToProject().get(0) + " .\n" + "  ?"
        + po.getVarsToProject().get(0) + " ?" + po.getVarsToProject().get(1)
        + " ?compound .\n" + "}\n";

    assertEquals(expected, AST2TextTranslator.translate(po.getAst()));

  }

  @Test
  public void testMultipleWithoutType() throws Exception {
    final String query = "SELECT ?POF WHERE {"
        + "  ?assay <http://www.example.com/#activity> ?activity ."
        + "  ?compound <http://www.example.com/#smile> ?smile ."
        + "  ?assay ?POF ?compound ." + "}";

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null,
        RecommendationType.PREDICATE, null, 3, backend);
    AddOneHopPropertyPath ahp = new AddOneHopPropertyPath(new SparqlTranslationProcessor());
    po = ahp.process(po);

    assertEquals(2, po.getVarsToProject().size());

    final String expected = "SELECT ?POF WHERE {\n"
        + "  ?assay <http://www.example.com/#activity> ?activity .\n"
        + "  ?compound <http://www.example.com/#smile> ?smile .\n"
        + "  ?assay ?POF ?" + po.getVarsToProject().get(0) + " .\n" + "  ?"
        + po.getVarsToProject().get(0) + " ?" + po.getVarsToProject().get(1)
        + " ?compound .\n" + "}\n";

    assertEquals(expected, AST2TextTranslator.translate(po.getAst()));

  }

  @Test
  public void testMultipleThreeHops() throws Exception {
    final String query = "SELECT ?POF WHERE {"
        + "  ?assay <http://www.example.com/#activity> ?activity ."
        + "  ?smile <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Canonical_smile> ."
        + "  ?assay ?POF ?smile ." + "}";

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null,
        RecommendationType.PREDICATE, null, 3, backend);
    AddOneHopPropertyPath ahp = new AddOneHopPropertyPath(new SparqlTranslationProcessor());
    po = ahp.process(po);

    assertEquals(4, po.getVarsToProject().size());

    final String expected = "SELECT ?POF WHERE {\n"
        + "  ?assay <http://www.example.com/#activity> ?activity .\n"
        + "  ?smile <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Canonical_smile> .\n"
        + "  ?assay ?POF ?" + po.getVarsToProject().get(0) + " .\n" + "  ?"
        + po.getVarsToProject().get(0) + " ?" + po.getVarsToProject().get(1)
        + " ?" + po.getVarsToProject().get(2) + " .\n" + "  ?"
        + po.getVarsToProject().get(2) + " ?" + po.getVarsToProject().get(3)
        + " ?smile .\n" + "}\n";

    assertEquals(expected, AST2TextTranslator.translate(po.getAst()));

  }

  @Test
  public void testMultipleHopsLimit() throws Exception {
    final String query = "SELECT ?POF WHERE {"
        + "  ?assay <http://www.example.com/#activity> ?activity ."
        + "  ?smile <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Canonical_smile> ."
        + "  ?assay ?POF ?smile ." + "}";

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    PipelineObject po = new PipelineObject(ast, null,
        RecommendationType.PREDICATE, null, 2, backend);
    AddOneHopPropertyPath ahp = new AddOneHopPropertyPath(new SparqlTranslationProcessor());
    po = ahp.process(po);

    assertEquals(2, po.getVarsToProject().size());

    final String expected = "SELECT ?POF WHERE {\n"
        + "  ?assay <http://www.example.com/#activity> ?activity .\n"
        + "  ?smile <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com/Canonical_smile> .\n"
        + "  ?assay ?POF ?" + po.getVarsToProject().get(0) + " .\n" + "  ?"
        + po.getVarsToProject().get(0) + " ?" + po.getVarsToProject().get(1)
        + " ?smile .\n" + "}\n";

    assertEquals(expected, AST2TextTranslator.translate(po.getAst()));

  }

  private static void addNQuads(BufferedReader ntriples)
      throws RDFParseException, RepositoryException, IOException {
    backend.getConnection().add(ntriples, "", SesameNxParser.nquadsFormat);
  }

}
