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

import org.openrdf.sindice.query.parser.sparql.ast.ASTAskQuery;
import org.openrdf.sindice.query.parser.sparql.ast.ASTDatasetClause;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTSelect;
import org.openrdf.sindice.query.parser.sparql.ast.ASTSelectQuery;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;

/**
 * Converts an AST to a Select Query AST if it is anything other than Select Query
 * @author bibhas [Jul 20, 2012]
 * @email bibhas.das@deri.org
 *
 */
public class Change2Select {
  private Change2Select(){
    
  }
  
  public static void process(ASTQueryContainer ast) {
    if (!(ast.getQuery() instanceof ASTSelectQuery)) { // Change to a SelectQuery
      final ASTSelectQuery selectQuery = new ASTSelectQuery(SyntaxTreeBuilderTreeConstants.JJTSELECTQUERY);
      final ASTSelect select = new ASTSelect(SyntaxTreeBuilderTreeConstants.JJTSELECT);

      selectQuery.jjtAppendChild(select);
      for (ASTDatasetClause d : ast.getQuery().getDatasetClauseList()) {
        selectQuery.jjtAppendChild(d);
      }
      selectQuery.jjtAppendChild(ast.getQuery().getWhereClause());
      if (!(ast.getQuery() instanceof ASTAskQuery)) {
        if (ast.getQuery().getGroupClause() != null) {
          selectQuery.jjtAppendChild(ast.getQuery().getGroupClause());
        }
        if (ast.getQuery().getHavingClause() != null) {
          selectQuery.jjtAppendChild(ast.getQuery().getHavingClause());
        }
        if (ast.getQuery().getOrderClause() != null) {
          selectQuery.jjtAppendChild(ast.getQuery().getOrderClause());
        }
        if (ast.getQuery().getLimit() != null) {
          selectQuery.jjtAppendChild(ast.getQuery().getLimit());
        }
        if (ast.getQuery().getOffset() != null) {
          selectQuery.jjtAppendChild(ast.getQuery().getOffset());
        }
      }
      if (ast.getQuery().getBindingsClause() != null) {
        selectQuery.jjtAppendChild(ast.getQuery().getBindingsClause());
      }
      ast.getQuery().jjtReplaceWith(selectQuery);
    }
  }

}
