/**
 * Copyright (c) 2013 National University of Ireland, Galway. All Rights Reserved.
 */
package org.sindice.analytics;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

/**
 * 
 */
public class RDFTestHelper {

  private RDFTestHelper() {}

  public static URI uri(String value) {
    return new URIImpl(value);
  }

  public static Literal literal(String value) {
    return new LiteralImpl(value);
  }

}
