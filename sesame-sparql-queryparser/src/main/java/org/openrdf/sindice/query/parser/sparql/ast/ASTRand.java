/* Generated By:JJTree: Do not edit this line. ASTRand.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.sindice.query.parser.sparql.ast;

public
class ASTRand extends SimpleNode {
  public ASTRand(int id) {
    super(id);
  }

  public ASTRand(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=517c9a57c9cad533daa72b9dbfe2a280 (do not edit this line) */
