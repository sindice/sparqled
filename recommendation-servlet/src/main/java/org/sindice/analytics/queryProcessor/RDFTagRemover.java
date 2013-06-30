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

import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTRDFLiteral;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

public class RDFTagRemover {

  private RDFTagRemover() {
  }

  public static void remove(ASTQueryContainer ast)
  throws VisitorException {
    RDFTagVisitor tag = new RDFTagVisitor();
    tag.visit(ast, null);
  }

  private static class RDFTagVisitor extends ASTVisitorBase {

    @Override
    public Object visit(ASTRDFLiteral node, Object data)
    throws VisitorException {
      final ASTIRI datatype;

      // Remove language tag
      node.setLang(null);
      // Remove datatype tag
      datatype = node.getDatatype();
      if (datatype != null) {
        node.removeChild(datatype);
      }
      return super.visit(node, data);
    }

  }

}
