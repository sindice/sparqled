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

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.core.analytics.commons.summary.AnalyticsVocab;
import org.sindice.core.analytics.commons.util.URIUtil;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bibhas [Jul 12, 2012]
 * @email bibhas.das@deri.org
 * 
 */
public class SparqlToDGSQueryManager implements SparqlToDGSQueryInterface {
  private static final Logger logger = LoggerFactory
      .getLogger(SparqlToDGSQueryInterface.class);
  private PipelineObject pipeObj;
  private String dgsQuery;

  public SparqlToDGSQueryManager() {
    pipeObj = new PipelineObject(null, null, RecommendationType.NONE, null, 0, null);
  }

  @Override
  public String getDGSQuery() throws DGSException {
    try {
      dgsQuery = AST2TextTranslator.translate(pipeObj.getAst());
    } catch (VisitorException e) {
      throw new DGSException(e);
    }
    return dgsQuery;
  }

  @Override
  public void load(String query) throws Exception {
    this.load(query, null);
  }

  @Override
  public void load(String query, List<String> varsToProject) throws Exception {
    try {
      dgsQuery = null;
      pipeObj.setMeta(null);
      pipeObj.setAst(SyntaxTreeBuilder.parseQuery(query));
      pipeObj.setVarsToProject(varsToProject);
      process();
    } catch (ParseException e) {
      throw new DGSException(e);
    } catch (VisitorException e) {
      throw new DGSException(e);
    } catch (MalformedQueryException e) {
      throw new DGSException(e);
    }
  }

  @Override
  public POFMetadata getPofASTMetadata() {
    return pipeObj.getMeta();
  }

  @Override
  public String getDomainsQuery(String domain, int limit) throws DGSException {
    throw new NotImplementedException();
  }

  @Override
  public String getPropertiesQuery(String domain, int limit)
      throws DGSException {
    throw new NotImplementedException();
  }

  @Override
  public String getClassesQuery(String domain, int limit) throws DGSException {
    if (domain == null) {
      throw new DGSException("The given domain is null");
    }
    final StringBuilder sb = new StringBuilder();
    final String d = URIUtil.getSndDomainFromUrl(domain);
    if (d == null) {
      throw new DGSException("Unable to get second-level domain name from "
          + domain);
    }
    domain = d;
    sb.append("SELECT DISTINCT ?class FROM <").append(
        AnalyticsVocab.GRAPH_SUMMARY_GRAPH).append("> WHERE {\n").append(
        "  ?node <").append(AnalyticsVocab.DOMAIN_URI).append("> ").append(
        domain.isEmpty() ? "?domain .\n" : "<"
            + AnalyticsVocab.DOMAIN_URI_PREFIX + domain + "> .\n").append(
        "  ?node <").append(AnalyticsVocab.LABEL).append("> ?l .\n").append(
        "  ?l <").append(AnalyticsVocab.LABEL).append(
        "> ?class .\n}\nORDER BY (?class)\n");
    if (limit != 0) {
      sb.append("LIMIT ").append(limit);
    }
    return sb.toString();
  }

  @Override
  public RecommendationType getRecommendationType() {
    return pipeObj.getType();
  }

  public void process() throws MalformedQueryException, VisitorException,
      ParseException, SesameBackendException {
    if (!pipeObj.getAst().containsQuery()) {
      return;
    }

    ASTVarGenerator.reset();
    // Retrieve the POF metadata
    pipeObj = new PofNodesMetadata().process(pipeObj);
    // expand each TP into simple one: denormalize syntax sugar constructions
    pipeObj = new DeNormalizeAST().process(pipeObj);

    // Ensure the query is valid for recommendation
    pipeObj = new ValidateQ4Recommendations().process(pipeObj);

    ASTVarGenerator.addVars(ASTVarProcessor.process(pipeObj.getAst()));
    /*
     * Map SPARQL query to a Data Graph Summary query
     */
    // 1. Materialize the POF
    pipeObj = new PointOfFocusProcessor().process(pipeObj);
    // 2. Remove Content Elements
    pipeObj = new ContentRemovalProcessor().process(pipeObj);
    // Define Recommendation Scope
    pipeObj = new RecommendationScopeProcessor().process(pipeObj);
    // Shortest path in case of PREDICATE recommendation
    pipeObj = new AddOneHopPropertyPath(new SparqlTranslationProcessor())
        .process(pipeObj);

    // TODO: Optimize the query by removing unnecessary parts, e.g., optional,
    // unions
    // 3. Map to the DGS query
    pipeObj = new SparqlTranslationProcessor().process(pipeObj);
  }

  // getter of pipeobj
  public PipelineObject getPipelineObject() {
    return pipeObj;
  }

}
