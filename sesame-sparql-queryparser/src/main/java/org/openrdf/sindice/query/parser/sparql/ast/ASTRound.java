/* Generated By:JJTree: Do not edit this line. ASTRound.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.sindice.query.parser.sparql.ast;

public
class ASTRound extends SimpleNode {
  public ASTRound(int id) {
    super(id);
  }

  public ASTRound(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=55cfeb13f8aa6740942346ccaaf276fe (do not edit this line) */
