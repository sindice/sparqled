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
