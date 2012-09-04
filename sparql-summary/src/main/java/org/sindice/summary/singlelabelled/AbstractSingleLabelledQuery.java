package org.sindice.summary.singlelabelled;

import java.util.Stack;

import org.openrdf.query.TupleQueryResult;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.summary.AbstractQuery;
import org.sindice.summary.Dump;

public class AbstractSingleLabelledQuery extends AbstractQuery {
  /**
   * Initialize the queries launcher.
   * 
   * @param d
   *          The Dump object, allows the use to modify the output easily.
   */
  public AbstractSingleLabelledQuery(Dump d) {
    super(d);
  }

  /**
   * Initialize the queries launcher.
   */
  public AbstractSingleLabelledQuery() {
    super();
  }

  /**
   * Get the name and the cardinality of the nodes.
   * 
   * @throws Exception
   */
  @Override
  public void computeName() throws Exception {
    _queriesResults = new Stack<TupleQueryResult>();
    String query = "SELECT ?label ?pType ?pDescription (COUNT (?s) AS ?cardinality)\n"
        + _graphFrom
        + "WHERE {\n{\n"
        + "SELECT ?s (IF(isURI(?type),\n"
        + "           concat('{<', str(?type), '>,',?p,'}'),\n"
        + "           concat('{\"', ENCODE_FOR_URI(?type), '\",',?p,'}')) AS ?label)\n"
        + "        WHERE {\n" + "        {\n";

    if (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() > 0) {
      query += "{ ?s <" + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(0)
          + "> ?type .\n  BIND ('0' AS ?p) }\n";
      for (int i = 1; i < AnalyticsClassAttributes.CLASS_ATTRIBUTES.size(); ++i) {
        query += "UNION{ ?s <"
            + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(i)
            + "> ?type .\n BIND ('" + i + "' AS ?p) }\n";
      }
    }
    query += "        }\n" + "        }\n" + "        GROUP BY ?s ?type ?p\n"
        + "    }\n" + "FILTER(?label != \"\")\n" + "}\n"
        + "GROUP BY ?label ?pType ?pDescription\n";

    _logger.debug(query);
    launchQueryNode(query);

  }

  /**
   * Get the name and the cardinality of the predicate/the edge for each node
   * and get the name of the node son/the target if it is possible.
   * 
   * @throws Exception
   */
  @Override
  public void computePredicate() throws Exception {
    _queriesResults = new Stack<TupleQueryResult>();

    String query = "SELECT  ?label  (COUNT (?label) AS ?cardinality) "
        + "?source ?target\n" + _graphFrom + "WHERE {\n" + "       {\n";
    query += "        SELECT ?s (IF(isURI(?type),\n"
        + "                       concat('<', str(?type), '>'),\n"
        + "                       concat('\"', ENCODE_FOR_URI(?type),"
        + "'\"')) as ?source)\n" + "           WHERE {\n";

    if (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() > 0) {
      query += "{ ?s <" + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(0)
          + "> ?type . }\n";
      for (int i = 1; i < AnalyticsClassAttributes.CLASS_ATTRIBUTES.size(); ++i) {
        query += "UNION { ?s <"
            + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(i)
            + "> ?type . }\n";
      }
    }

    query += "           }\n" + "           GROUP BY ?s ?type\n"
        + "       }\n" + "        FILTER(?source != \"\")\n"
        + "        ?s ?label ?sSon .\n";

    // OPTIONAL
    query += "        OPTIONAL {\n" + "        {\n";
    query += "        SELECT ?sSon (IF(isURI(?typeSon),\n"
        + "                       concat('<', str(?typeSon), '>'),\n"
        + "                       concat('\"', ENCODE_FOR_URI(?typeSon),"
        + "'\"')) as ?target)\n" + "           WHERE {\n";

    if (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() > 0) {
      query += "{ ?sSon <" + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(0)
          + "> ?typeSon . }\n";
      for (int i = 1; i < AnalyticsClassAttributes.CLASS_ATTRIBUTES.size(); ++i) {
        query += "UNION { ?sSon <"
            + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(i)
            + "> ?typeSon . }\n";
      }
    }
    query += "           }\n" + "           GROUP BY ?sSon ?typeSon\n"
        + "        }\n" + "        }\n";

    query += "}\n" + "GROUP BY ?label ?source ?target \n";

    _logger.debug(query);
    launchQueryPred(query);
  }
}
