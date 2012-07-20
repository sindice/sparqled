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
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

/**
 * @author bibhas [Jul 2, 2012]
 * @email bibhas.das@deri.org
 * 
 */
public class Select2Ask {
  public ASTQueryContainer convert(ASTQueryContainer ast)
      throws VisitorException {
    ASTQueryContainer newAst = new ASTQueryContainer(
        SyntaxTreeBuilderTreeConstants.JJTQUERYCONTAINER);
    final ASTAskQuery askQuery = new ASTAskQuery(
        SyntaxTreeBuilderTreeConstants.JJTASKQUERY);
    for (ASTDatasetClause d : ast.getQuery().getDatasetClauseList()) {
      askQuery.jjtAppendChild(d);
    }
    if (ast.getQuery().getWhereClause() != null)
      askQuery.jjtAppendChild(ast.getQuery().getWhereClause());
    if (ast.getQuery().getBindingsClause() != null)
      askQuery.jjtAppendChild(ast.getQuery().getBindingsClause());
    newAst.jjtAppendChild(askQuery);
    return newAst;
  }
}
