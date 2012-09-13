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
 * @author Campinas Stephane [ 25 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTConstraint;
import org.openrdf.sindice.query.parser.sparql.ast.ASTLimit;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.sindice.core.analytics.commons.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DGSQueryProcessor
implements QueryProcessor {

  private static final Logger            logger       = LoggerFactory.getLogger(QueryProcessor.class);

  private ASTQueryContainer              ast;
  private String                         dgsQuery;
  private RecommendationType             type;

  private POFMetadata                    pofMetadata; // The Point Of Focus metadata

  @Override
  public String getDGSQueryWithLimit(int limit, ASTConstraint... contraints)
  throws DGSException {
    final ASTLimit astLimit;

    if (limit != 0) {
      if (ast.getQuery().getLimit() == null) {
        astLimit = new ASTLimit(SyntaxTreeBuilder.LIMIT);
        ast.getQuery().jjtAppendChild(astLimit);
      } else {
        astLimit = ast.getQuery().getLimit();
      }
      astLimit.setValue(limit);
    }
    return this.getDGSQuery(contraints);
  }

  @Override
  public String getDGSQuery(ASTConstraint... contraints)
  throws DGSException {
    if (dgsQuery == null) {
      try {
        if (contraints != null && contraints.length != 0) {
          // TODO: add possible constraints to the query
          ast.getQuery().getWhereClause();
        }
        dgsQuery = AST2TextTranslator.translate(ast);
      } catch (VisitorException e) {
        throw new DGSException(e);
      }
    }
    return dgsQuery;
  }

  @Override
  public void load(String query)
  throws DGSException {
    this.load(query, null);
  }

  @Override
  public void load(String query, List<String> varsToProject)
  throws DGSException {
    try {
      dgsQuery = null;
      pofMetadata = null;
      ast = SyntaxTreeBuilder.parseQuery(query);
      pofMetadata = SparqlToDGSQuery.process(ast, varsToProject);
      type = SparqlToDGSQuery.getRecommendationType();
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
    return pofMetadata;
  }

  @Override
  public String getDomainsQuery(String domain, int limit)
  throws DGSException {
    throw new NotImplementedException();
  }

  @Override
  public String getPropertiesQuery(String domain, int limit)
  throws DGSException {
    if (domain == null) {
      throw new DGSException("The given domain is null");
    }
    final StringBuilder sb = new StringBuilder();
    final String d = URIUtil.getSndDomainFromUrl(domain);
    if (d == null) {
      throw new DGSException("Unable to get second-level domain name from " + domain);
    }
    domain = d;
    sb.append("SELECT DISTINCT ?property FROM <").append(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH).append("> WHERE {\n")
      .append("  ?edge <").append(DataGraphSummaryVocab.EDGE_PUBLISHED_IN).append("> ")
      .append(domain.isEmpty() ? "?domain .\n" : "<" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + domain + "> .\n")
      .append("  ?edge <").append(DataGraphSummaryVocab.LABEL).append("> ?property .\n}\nORDER BY (?property)\n");
    if (limit != 0) {
      sb.append("LIMIT ").append(limit);
    }
    return sb.toString();
  }

  @Override
  public String getClassesQuery(String domain, int limit)
  throws DGSException {
    if (domain == null) {
      throw new DGSException("The given domain is null");
    }
    final StringBuilder sb = new StringBuilder();
    final String d = URIUtil.getSndDomainFromUrl(domain);
    if (d == null) {
      throw new DGSException("Unable to get second-level domain name from " + domain);
    }
    domain = d;
    sb.append("SELECT DISTINCT ?class FROM <").append(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH).append("> WHERE {\n")
      .append("  ?node <").append(DataGraphSummaryVocab.DOMAIN_URI).append("> ")
      .append(domain.isEmpty() ? "?domain .\n" : "<" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + domain + "> .\n")
      .append("  ?node <").append(DataGraphSummaryVocab.LABEL).append("> ?l .\n")
      .append("  ?l <").append(DataGraphSummaryVocab.LABEL).append("> ?class .\n}\nORDER BY (?class)\n");
    if (limit != 0) {
      sb.append("LIMIT ").append(limit);
    }
    return sb.toString();
  }

  @Override
  public RecommendationType getRecommendationType() {
    return type;
  }

}
