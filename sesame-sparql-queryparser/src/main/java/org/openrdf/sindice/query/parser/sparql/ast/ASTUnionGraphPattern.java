/* Generated By:JJTree: Do not edit this line. ASTUnionGraphPattern.java */

package org.openrdf.sindice.query.parser.sparql.ast;

public class ASTUnionGraphPattern extends SimpleNode {

	public ASTUnionGraphPattern(int id) {
		super(id);
	}

	public ASTUnionGraphPattern(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}
}
