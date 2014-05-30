/**
 * Copyright (c) 2014 National University of Ireland, Galway. All Rights Reserved.
 */
package org.sindice.sparqled.sparql;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for building expected bindings of a SPARQL query
 */
public class SparqlResultsHelper {

  private SparqlResultsHelper() {}

  /**
   * A pair of strings about a binding, e.g., the value-label pair
   */
  public static class Pair {
    final String name;
    final String value;
    Pair(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  /**
   * The serialization of a {@link Binding} as per <a href="http://www.w3.org/TR/rdf-sparql-json-res/#variable-binding-results">
   * http://www.w3.org/TR/rdf-sparql-json-res/#variable-binding-results</a>
   */
  public static class Binding {
    final String name;
    final Map<String, String> data = new HashMap<String, String>();
    Binding(String name) {
      this.name = name;
    }
  }

  /**
   * Returns a set of {@link Binding}s
   */
  public static Map<String, Object> bindings(Binding...bindings) {
    final Map<String, Object> result = new HashMap<String, Object>();
    for (Binding b : bindings) {
      result.put(b.name, b.data);
    }
    return result;
  }

  /**
   * Create a {@link Binding} for the variable name with the given serialized data.
   * @param name
   * @param pairs
   * @return
   */
  public static Binding binding(String name, Pair...pairs) {
    final Binding b = new Binding(name);
    for (Pair pair : pairs) {
      b.data.put(pair.name, pair.value);
    }
    return b;
  }

  /**
   * Add a pair of metadata for describing a binding
   * @param name
   * @param value
   * @return
   */
  public static Pair add(String name, String value) {
    return new Pair(name, value);
  }

}
