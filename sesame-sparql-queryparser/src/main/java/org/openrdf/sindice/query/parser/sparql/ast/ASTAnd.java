/* Generated By:JJTree: Do not edit this line. ASTAnd.java */

package org.openrdf.sindice.query.parser.sparql.ast;

public class ASTAnd extends SimpleNode {

	public ASTAnd(int id) {
		super(id);
	}

	public ASTAnd(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}
}
