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

import org.openrdf.repository.RepositoryException;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.summary.Dump;

/**
 * 
 */
public class HTTPVirtuosoMultiLabelledQuery extends AbstractMultiLabelledQuery {

  /**
   * This constructor make a connection with a virtuoso SPARQL repository with
   * HTTP repository.
   * 
   * @param d
   *          The dump object.
   * @param websiteURL
   *          URL of the web SPARQL repository
   * @throws RepositoryException
   * @throws SesameBackendException
   */
  public HTTPVirtuosoMultiLabelledQuery(Dump d, String websiteURL)
      throws RepositoryException, SesameBackendException {
    super(d);
    _repository = SesameBackendFactory.getDgsBackend(BackendType.HTTP,
        websiteURL);
    _repository.initConnection();

  }

  /**
   * This constructor make a connection with a virtuoso SPARQL repository with
   * HTTP repository.
   * 
   * @param websiteURL
   *          URL of the web SPARQL repository
   * @throws RepositoryException
   * @throws SesameBackendException
   */
  public HTTPVirtuosoMultiLabelledQuery(String websiteURL)
      throws RepositoryException, SesameBackendException {
    _repository = SesameBackendFactory.getDgsBackend(BackendType.HTTP,
        websiteURL);
    _repository.initConnection();

  }

  /**
   * This constructor without connection, for JUNIT test.
   * 
   * @param d
   *          The dump object.
   * @throws RepositoryException
   * @throws SesameBackendException
   */
  public HTTPVirtuosoMultiLabelledQuery(Dump d) throws RepositoryException,
      SesameBackendException {
    super(d);
  }

  /**
   * Create a valid SPARQL command for a GROUP_CONCAT, using the Virtuoso
   * functions.
   * 
   * @param unchangedVar
   *          All the variable which will be keep after the GROUP_CONCAT
   * @param initialVar
   *          The variable to group.
   * @param newVar
   *          The new name of this variable.
   */
  @Override
  protected String makeGroupConcat(String initialVar, String newVar) {
    return " (sql:GROUP_CONCAT(IF(isURI(" + initialVar + "),\n"
        + "                bif:concat('<', str(" + initialVar + "), '>'),\n"
        + "                bif:concat('\"', ENCODE_FOR_URI(" + initialVar
        + "), '\"')), \" \") AS " + newVar + ")\n";

  }

  /**
   * Get the name and the cardinality of the nodes, with a valid Virtuoso
   * GROUP_CONCAT.
   * 
   * @throws Exception
   */
  @Override
  public void computeName() throws Exception {
    String query = "SELECT ?label (COUNT (?s) AS ?cardinality)\n" + _graphFrom
        + "WHERE {\n{\n" + "SELECT ?s (sql:GROUP_CONCAT(IF(isURI(?type),\n";

    if (AnalyticsClassAttributes.getClassAttributes().size() > 0) {
      for (int i = 0; i < AnalyticsClassAttributes.getClassAttributes().size() - 1; ++i) {
        query += "            " + "IF(?p = <"
            + AnalyticsClassAttributes.getClassAttributes().get(i) + ">,\n"
            + "                " + "bif:concat('{<', str(?type), '>," + i
            + "}'),\n";
      }

      query += "            "
          + "# "
          + AnalyticsClassAttributes.getClassAttributes()
              .get(AnalyticsClassAttributes.getClassAttributes().size() - 1)
          + "\n" + "                " + "bif:concat('{<', str(?type), '>,"
          + (AnalyticsClassAttributes.getClassAttributes().size() - 1) + "}'\n"
          + "            ";
      // close parenthesis
      for (int i = 0; i < AnalyticsClassAttributes.getClassAttributes().size(); ++i) {
        query += ")";
      }
      query += ",\n";
    }

    query += "        " + "#else\n";

    // If it is a litteral, get the type of the "type".
    if (AnalyticsClassAttributes.getClassAttributes().size() > 0) {
      for (int i = 0; i < AnalyticsClassAttributes.getClassAttributes().size() - 1; ++i) {
        query += "            " + "IF(?p = <"
            + AnalyticsClassAttributes.getClassAttributes().get(i) + ">,\n"
            + "                "
            + "bif:concat('{\"', ENCODE_FOR_URI(?type), '\"," + i + "}'),\n";
      }

      query += "            "
          + "# "
          + AnalyticsClassAttributes.getClassAttributes()
              .get(AnalyticsClassAttributes.getClassAttributes().size() - 1)
          + "\n" + "                "
          + "bif:concat('{\"', ENCODE_FOR_URI(?type), '\","
          + (AnalyticsClassAttributes.getClassAttributes().size() - 1) + "}'\n"
          + "            ";
      // close parenthesis
      for (int i = 0; i < AnalyticsClassAttributes.getClassAttributes().size(); ++i) {
        query += ")";
      }
      query += "\n";

      if (AnalyticsClassAttributes.getClassAttributes().size() > 1) {
        query += "), \" \") AS ?label)\n" + "        WHERE {\n"
            + "        {\n" + "            SELECT ?s ?type ?p WHERE\n"
            + "            {\n";

        if (AnalyticsClassAttributes.getClassAttributes().size() > 0) {
          query += "{ ?s <" + AnalyticsClassAttributes.getClassAttributes().get(0)
              + "> ?type .\n" + "?s ?p ?type .\n" + "FILTER(?p = <"
              + AnalyticsClassAttributes.getClassAttributes().get(0) + ">) }\n";

          for (int i = 1; i < AnalyticsClassAttributes.getClassAttributes().size(); ++i) {
            query += "UNION { ?s <"
                + AnalyticsClassAttributes.getClassAttributes().get(i)
                + "> ?type .\n" + "?s ?p ?type .\n" + "FILTER(?p = <"
                + AnalyticsClassAttributes.getClassAttributes().get(i) + ">) }\n";
          }
        }

      } else { // Only 1 type => optimize query (remove FILTER and ?p variable)
        query += "), \" \") AS ?label)\n" + "        WHERE {\n"
            + "        {\n" + "            SELECT ?s ?type WHERE\n"
            + "            {\n" + "                { ?s <"
            + AnalyticsClassAttributes.getClassAttributes().get(0)
            + "> ?type . }\n";
      }
    }
    query += "            }\n" + "            ORDER BY ?type\n"
        + "        }\n" + "        }\n" + "        GROUP BY ?s\n" + "    }\n"
        + "}\n" + "GROUP BY ?label\n";

    _logger.debug(query);
    launchQueryNode(query);
  }

}
