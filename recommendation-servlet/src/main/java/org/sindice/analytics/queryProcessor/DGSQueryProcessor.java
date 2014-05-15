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

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

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
      try {
        if (contraints != null && contraints.length != 0) {
          // TODO: add possible constraints to the query
          ast.getQuery().getWhereClause();
        }
        writer.getBuffer().setLength(0);
        dgsQuery = mustache.execute(writer, dgs.getRecommendationQuery()).toString();
      } catch (IOException e) {
        throw new DGSException(e);
      }
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
    return dgs.getPOFMetadata();
  }

  @Override
  public RecommendationType getRecommendationType() {
    return dgs.getRecommendationType();
  }

}
