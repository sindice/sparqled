/* Generated By:JJTree: Do not edit this line. ASTOr.java */

package org.openrdf.sindice.query.parser.sparql.ast;

public class ASTOr extends SimpleNode {

	public ASTOr(int id) {
		super(id);
	}

	public ASTOr(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}
}
