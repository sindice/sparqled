/* Generated By:JJTree: Do not edit this line. ASTBindingSet.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.sindice.query.parser.sparql.ast;

public
class ASTBindingSet extends SimpleNode {
  public ASTBindingSet(int id) {
    super(id);
  }

  public ASTBindingSet(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=de82e0fc76cce0593d8b8cfd0e7188e5 (do not edit this line) */
