/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
 *
 *
 * This project is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sindice.summary.multilabelled;

import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.summary.AbstractQuery;
import org.sindice.summary.Dump;

public class AbstractMultiLabelledQuery extends AbstractQuery {
  /**
   * Initialize the queries launcher.
   * 
   * @param d
   *          The Dump object, allows the use to modify the output easily.
   */
  public AbstractMultiLabelledQuery(Dump d) {
    super(d);
  }

  /**
   * Initialize the queries launcher.
   */
  public AbstractMultiLabelledQuery() {
    super();
  }

  /**
   * Create a valid SPARQL command for a GROUP_CONCAT.
   * 
   * @param unchangedVar
   *          All the variable which will be keep after the GROUP_CONCAT
   * @param initialVar
   *          The variable to group.
   * @param newVar
   *          The new name of this variable.
   */
  protected String makeGroupConcat(String initialVar, String newVar) {
    return " (GROUP_CONCAT(IF(isURI(" + initialVar + "),\n"
        + "                concat('<', str(" + initialVar + "), '>'),\n"
        + "                concat('\"', ENCODE_FOR_URI(" + initialVar
        + "), '\"'))) AS " + newVar + ")\n";
  }

  /**
   * Get the name and the cardinality of the nodes.
   * 
   * @throws Exception
   */
  @Override
  public void computeName() throws Exception {
    String query = "SELECT ?label ?pType ?pDescription (COUNT (?s) AS ?cardinality)\n"
        + _graphFrom
        + "WHERE {\n{\n"
        + "SELECT ?s (GROUP_CONCAT(IF(isURI(?type),\n"
        + "           concat('{<', str(?type), '>,',?p,'}'),\n"
        + "           concat('{\"', ENCODE_FOR_URI(?type), '\",',?p,'}'))) AS ?label)\n"
        + "        WHERE {\n"
        + "        {\n"
        + "            SELECT ?s ?type ?p WHERE\n" + "            {\n";

    if (AnalyticsClassAttributes.getClassAttributes().size() > 0) {
      query += "{ ?s <" + AnalyticsClassAttributes.getClassAttributes().get(0)
          + "> ?type .\n  BIND ('0' AS ?p) }\n";
      for (int i = 1; i < AnalyticsClassAttributes.getClassAttributes().size(); ++i) {
        query += "UNION{ ?s <"
            + AnalyticsClassAttributes.getClassAttributes().get(i)
            + "> ?type .\n BIND ('" + i + "' AS ?p) }\n";
      }
    }
    query += "            }\n" + "            ORDER BY ?type\n"
        + "        }\n" + "        }\n" + "        GROUP BY ?s\n" + "    }\n"
        + "FILTER(?label != \"\")\n" + "}\n"
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
    String query = "SELECT  ?label  (COUNT (?label) AS ?cardinality) "
        + "?source ?target\n" + _graphFrom + "WHERE {\n" + "       {\n";
    query += "        SELECT ?s " + makeGroupConcat("?type", "?source");
    query += "           WHERE {\n" + "           {\n"
        + "               SELECT ?s ?type WHERE {\n";

    if (AnalyticsClassAttributes.getClassAttributes().size() > 0) {
      query += "{ ?s <" + AnalyticsClassAttributes.getClassAttributes().get(0)
          + "> ?type . }\n";
      for (int i = 1; i < AnalyticsClassAttributes.getClassAttributes().size(); ++i) {
        query += "UNION { ?s <"
            + AnalyticsClassAttributes.getClassAttributes().get(i)
            + "> ?type . }\n";
      }
    }

    query += "               }\n" + "               ORDER BY ?type\n"
        + "           }\n" + "           }\n" + "           GROUP BY ?s\n"
        + "       }\n" + "        FILTER(?source != \"\")\n"
        + "        ?s ?label ?sSon .\n";

    // OPTIONAL
    query += "        OPTIONAL {\n" + "        {\n";
    query += "        SELECT ?sSon " + makeGroupConcat("?typeSon", "?target");
    query += "           WHERE {\n" + "           {\n"
        + "               SELECT ?sSon ?typeSon WHERE " + "{\n";

    if (AnalyticsClassAttributes.getClassAttributes().size() > 0) {
      query += "{ ?sSon <" + AnalyticsClassAttributes.getClassAttributes().get(0)
          + "> ?typeSon . }\n";
      for (int i = 1; i < AnalyticsClassAttributes.getClassAttributes().size(); ++i) {
        query += "UNION { ?sSon <"
            + AnalyticsClassAttributes.getClassAttributes().get(i)
            + "> ?typeSon . }\n";
      }
    }
    query += "              }\n" + "               ORDER BY ?typeSon\n"
        + "           }\n" + "           }\n" + "           GROUP BY ?sSon\n"
        + "        }\n" + "        }\n";

    query += "}\n" + "GROUP BY ?label ?source ?target \n";

    _logger.debug(query);
    launchQueryPred(query);
  }

}
