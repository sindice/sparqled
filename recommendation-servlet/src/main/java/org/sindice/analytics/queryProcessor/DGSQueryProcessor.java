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

import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTConstraint;
import org.openrdf.sindice.query.parser.sparql.ast.ASTLimit;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

/**
 * 
 */
public class DGSQueryProcessor
implements QueryProcessor {

  private ASTQueryContainer  ast;
  private String             dgsQuery;
  private RecommendationType type;

  private POFMetadata        pofMetadata; // The Point Of Focus metadata

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
  public RecommendationType getRecommendationType() {
    return type;
  }

}
