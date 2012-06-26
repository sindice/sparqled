/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sindice.query.parser.sparql.ast;

import org.openrdf.model.Value;

/**
 * @author jeen
 */
public abstract class ASTRDFValue extends SimpleNode {

	private Value value;

	/**
	 * @param id
	 */
	public ASTRDFValue(int id) {
		super(id);
	}

	/**
	 * @param parser
	 * @param id
	 */
	public ASTRDFValue(SyntaxTreeBuilder parser, int id) {
		super(parser, id);
	}

	public Value getRDFValue() {
		return value;

	}

	public void setRDFValue(final Value value) {
		this.value = value;
	}

}
