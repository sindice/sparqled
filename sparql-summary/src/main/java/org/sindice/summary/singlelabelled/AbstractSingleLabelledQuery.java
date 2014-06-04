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
package org.sindice.summary.singlelabelled;

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
    String query = "SELECT ?label ?pType ?pDescription (COUNT (?s) AS ?cardinality)\n"
        + _graphFrom
        + "WHERE {\n{\n"
        + "SELECT ?s (IF(isURI(?type),\n"
        + "           concat('{<', str(?type), '>,',?p,'}'),\n"
        + "           concat('{\"', ENCODE_FOR_URI(?type), '\",',?p,'}')) AS ?label)\n"
        + "        WHERE {\n";

    if (AnalyticsClassAttributes.getClassAttributes().size() > 0) {
      query += "{ ?s <" + AnalyticsClassAttributes.getClassAttributes().get(0)
          + "> ?type .\n  BIND ('0' AS ?p) }\n";
      for (int i = 1; i < AnalyticsClassAttributes.getClassAttributes().size(); ++i) {
        query += "UNION{ ?s <"
            + AnalyticsClassAttributes.getClassAttributes().get(i)
            + "> ?type .\n BIND ('" + i + "' AS ?p) }\n";
      }
    }

    query += "        }\n" + "    }\n" + "FILTER(?label != \"\")\n" + "}\n"
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
  public void computePredicate() throws Exception {//
    String query = "SELECT  ?label  (COUNT (?label) AS ?cardinality) "
        + "?source ?target\n" + _graphFrom + "WHERE {\n" + "       {\n";
    query += "        SELECT ?s (IF(isURI(?type),\n"
        + "                       concat('<', str(?type), '>'),\n"
        + "                       concat('\"', ENCODE_FOR_URI(?type),"
        + "'\"')) as ?source)\n" + "           WHERE {\n";

    if (AnalyticsClassAttributes.getClassAttributes().size() > 0) {
      query += "{ ?s <" + AnalyticsClassAttributes.getClassAttributes().get(0)
          + "> ?type . }\n";
      for (int i = 1; i < AnalyticsClassAttributes.getClassAttributes().size(); ++i) {
        query += "UNION { ?s <"
            + AnalyticsClassAttributes.getClassAttributes().get(i)
            + "> ?type . }\n";
      }
    }

    query += "           }\n" + "       }\n"
        + "        FILTER(?source != \"\")\n" + "        ?s ?label ?sSon .\n";

    // OPTIONAL
    query += "        OPTIONAL {\n" + "        {\n"
        + "        SELECT ?sSon (IF(isURI(?typeSon),\n"
        + "                       concat('<', str(?typeSon), '>'),\n"
        + "                       concat('\"', ENCODE_FOR_URI(?typeSon),"
        + "'\"')) as ?target)\n" + "           WHERE {\n";

    if (AnalyticsClassAttributes.getClassAttributes().size() > 0) {
      query += "{ ?sSon <" + AnalyticsClassAttributes.getClassAttributes().get(0)
          + "> ?typeSon . }\n";
      for (int i = 1; i < AnalyticsClassAttributes.getClassAttributes().size(); ++i) {
        query += "UNION { ?sSon <"
            + AnalyticsClassAttributes.getClassAttributes().get(i)
            + "> ?typeSon . }\n";
      }
    }
    query += "           }\n" + "        }\n" + "        }\n" + "}\n"
        + "GROUP BY ?label ?source ?target \n";

    _logger.debug(query);
    launchQueryPred(query);
  }

}
