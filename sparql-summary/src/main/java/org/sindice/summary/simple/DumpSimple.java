/**
 * Copyright (c) 2014 National University of Ireland, Galway. All Rights Reserved.
 */
package org.sindice.summary.simple;

import java.io.IOException;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.summary.Dump;

/**
 * 
 */
public class DumpSimple
extends Dump {

  private final Literal emptyLit = new LiteralImpl("");

  /**
   * Write an edge in the output..
   * 
   * @param bindingSet
   *          Result of the query from computePredicate()
   */
  public void dumpRDFPred(BindingSet bindingSet)
  throws QueryEvaluationException, IOException {
    final Value s = bindingSet.getValue("source");
    final Value p = bindingSet.getValue("predicate");
    final Value o = bindingSet.hasBinding("target") ? bindingSet.getValue("target") : emptyLit;

    if (!AnalyticsClassAttributes.isClass(p.stringValue())) {
      dumpTriple(s, p, o);
    }
  }

}
