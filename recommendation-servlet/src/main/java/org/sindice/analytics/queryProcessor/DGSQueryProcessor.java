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

import java.io.StringWriter;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTConstraint;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * 
 */
public class DGSQueryProcessor
implements QueryProcessor {

  /** Default name of the template */
  public static final String     TEMPLATE_NAME = "translate.mustache";

  private final Mustache         mustache;

  private ASTQueryContainer      ast;
  private String                 dgsQuery;

  private final SparqlToDGSQuery dgs           = new SparqlToDGSQuery();
  private final StringWriter     writer        = new StringWriter();

  public DGSQueryProcessor() {
    MustacheFactory mf = new DefaultMustacheFactory();
    mustache = mf.compile(TEMPLATE_NAME);
  }

  /**
   * Create a new instance with a custom {@link Mustache} template
   * @param pathToTemplate the path to the Mustache template for the SPARQL query translation
   */
  public DGSQueryProcessor(String pathToTemplate) {
    MustacheFactory mf = new DefaultMustacheFactory();
    mustache = mf.compile(pathToTemplate);
  }

  /**
   * Create a new instance with the given {@link Mustache} instance
   */
  public DGSQueryProcessor(Mustache template) {
    if (template == null) {
      MustacheFactory mf = new DefaultMustacheFactory();
      mustache = mf.compile(TEMPLATE_NAME);
    } else {
      mustache = template;
    }
  }

  @Override
  public String getDGSQueryWithLimit(int limit, ASTConstraint... contraints)
  throws DGSException {
    dgs.getRecommendationQuery().setLimit(limit);
    return this.getDGSQuery(contraints);
  }

  @Override
  public String getDGSQuery(ASTConstraint... contraints)
  throws DGSException {
    if (dgsQuery == null) {
      if (contraints != null && contraints.length != 0) {
        // TODO: add possible constraints to the query
        ast.getQuery().getWhereClause();
      }
      writer.getBuffer().setLength(0);
      dgsQuery = mustache.execute(writer, dgs.getRecommendationQuery()).toString();
    }
    return dgsQuery;
  }

  @Override
  public void load(String query)
  throws DGSException {
    try {
      dgsQuery = null;
      ast = SyntaxTreeBuilder.parseQuery(query);
      dgs.process(ast);
    } catch (Exception e) {
      throw new DGSException(e);
    }
  }

  @Override
  public POFMetadata getPofASTMetadata() {
    return dgs.getPOFMetadata();
  }

  @Override
  public RecommendationType getRecommendationType() {
    return dgs.getRecommendationType();
  }

}
