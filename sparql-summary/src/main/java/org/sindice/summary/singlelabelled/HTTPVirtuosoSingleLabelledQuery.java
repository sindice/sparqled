package org.sindice.summary.singlelabelled;

import java.io.File;
import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.summary.Dump;

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
/**
 * @author Pierre Bailly <pierre.bailly@deri.org>
 */
public class HTTPVirtuosoSingleLabelledQuery extends
    AbstractSingleLabelledQuery {

  boolean _identified;

  /**
   * This constructor make a connection with a virtuoso SPARQL repository.
   * 
   * @param d
   *          The dump object.
   * @param websiteURL
   *          URL of the web SPARQL repository
   * @throws RepositoryException
   * @throws SesameBackendException
   */
  public HTTPVirtuosoSingleLabelledQuery(Dump d, String websiteURL,
      String user, String password) throws RepositoryException,
      SesameBackendException {
    super(d);
    _repository = SesameBackendFactory.getDgsBackend(BackendType.VIRTUOSO,
        websiteURL, user, password);
    _repository.initConnection();
    _identified = true;

  }

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
  public HTTPVirtuosoSingleLabelledQuery(Dump d, String websiteURL)
      throws RepositoryException, SesameBackendException {
    super(d);
    _repository = SesameBackendFactory.getDgsBackend(BackendType.HTTP,
        websiteURL);
    _repository.initConnection();
    _identified = false;

  }

  /**
   * This constructor make a connection with a virtuoso SPARQL repository.
   * 
   * @param websiteURL
   *          URL of the web SPARQL repository
   * @throws RepositoryException
   * @throws SesameBackendException
   */
  public HTTPVirtuosoSingleLabelledQuery(String websiteURL, String user,
      String password) throws RepositoryException, SesameBackendException {
    _repository = SesameBackendFactory.getDgsBackend(BackendType.VIRTUOSO,
        websiteURL, user, password);
    _repository.initConnection();
    _identified = true;
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
  public HTTPVirtuosoSingleLabelledQuery(String websiteURL)
      throws RepositoryException, SesameBackendException {
    _repository = SesameBackendFactory.getDgsBackend(BackendType.HTTP,
        websiteURL);
    _repository.initConnection();
    _identified = false;
  }

  /**
   * This constructor without connection, for JUNIT test.
   * 
   * @param d
   *          The dump object.
   * @throws RepositoryException
   * @throws SesameBackendException
   */
  public HTTPVirtuosoSingleLabelledQuery(Dump d) throws RepositoryException,
      SesameBackendException {
    super(d);
  }

  /**
   * Add an RDF file to the local repository.
   * 
   * @param RDFFile
   *          The path of the new RDF file
   * @param Ressource
   *          Optional argument for the file (example : The domain).
   * @throws RDFParseException
   * @throws RepositoryException
   * @throws IOException
   * @throws SesameBackendException
   */
  @Override
  public void addFileToRepository(String RDFFile, RDFFormat format,
      Resource... contexts) throws RDFParseException, RepositoryException,
      IOException, SesameBackendException {
    if (_identified) {
      _repository.addToRepository(new File(RDFFile), format, contexts);
    } else {
      _logger
          .error("You should identify yourself to the database before adding triples inside.");
      _logger.error("OPTION : user, password");
    }
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
        + "WHERE {\n{\n" + "SELECT ?s (IF(isURI(?type),\n";

    if (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() > 0) {
      for (int i = 0; i < AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() - 1; ++i) {
        query += "            " + "IF(?p = <"
            + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(i) + ">,\n"
            + "                " + "bif:concat('{<', str(?type), '>," + i
            + "}'),\n";
      }

      query += "            "
          + "# "
          + AnalyticsClassAttributes.CLASS_ATTRIBUTES
              .get(AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() - 1)
          + "\n" + "                " + "bif:concat('{<', str(?type), '>,"
          + (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() - 1) + "}'\n"
          + "            ";
      // close parenthesis
      for (int i = 0; i < AnalyticsClassAttributes.CLASS_ATTRIBUTES.size(); ++i) {
        query += ")";
      }
      query += ",\n";
    }

    query += "        " + "#else\n";

    // If it is a litteral, get the type of the "type".
    if (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() > 0) {
      for (int i = 0; i < AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() - 1; ++i) {
        query += "            " + "IF(?p = <"
            + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(i) + ">,\n"
            + "                "
            + "bif:concat('{\"', ENCODE_FOR_URI(?type), '\"," + i + "}'),\n";
      }

      query += "            "
          + "# "
          + AnalyticsClassAttributes.CLASS_ATTRIBUTES
              .get(AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() - 1)
          + "\n" + "                "
          + "bif:concat('{\"', ENCODE_FOR_URI(?type), '\","
          + (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() - 1) + "}'\n"
          + "            ";
      // close parenthesis
      for (int i = 0; i < AnalyticsClassAttributes.CLASS_ATTRIBUTES.size(); ++i) {
        query += ")";
      }
      query += "\n";

      if (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() > 1) {
        query += ") AS ?label)\n" + "        WHERE {\n";

        if (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() > 0) {
          query += "{ ?s <" + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(0)
              + "> ?type .\n" + "?s ?p ?type .\n" + "FILTER(?p = <"
              + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(0) + ">) }\n";

          for (int i = 1; i < AnalyticsClassAttributes.CLASS_ATTRIBUTES.size(); ++i) {
            query += "UNION { ?s <"
                + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(i)
                + "> ?type .\n" + "?s ?p ?type .\n" + "FILTER(?p = <"
                + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(i) + ">) }\n";
          }
        }

      } else { // Only 1 type => optimize query (remove FILTER and ?p variable)
        query += ") AS ?label)\n" + "        WHERE {\n"
            + "                { ?s <"
            + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(0)
            + "> ?type . }\n";
      }
    }
    query += "        }\n" + "    }\n" + "}\n" + "GROUP BY ?label\n";

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
    query += "        SELECT ?s (IF(isURI(?type),\n"
        + "                       bif:concat('<', str(?type), '>'),\n"
        + "                       bif:concat('\"', ENCODE_FOR_URI(?type),"
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

    query += "           }\n" + "       }\n"
        + "        FILTER(?source != \"\")\n" + "        ?s ?label ?sSon .\n";

    // OPTIONAL
    query += "        OPTIONAL {\n" + "        {\n";
    query += "        SELECT ?sSon (IF(isURI(?typeSon),\n"
        + "                       bif:concat('<', str(?typeSon), '>'),\n"
        + "                       bif:concat('\"', ENCODE_FOR_URI(?typeSon),"
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
    query += "           }\n" + "        }\n" + "        }\n";

    query += "}\n" + "GROUP BY ?label ?source ?target \n";

    _logger.debug(query);
    launchQueryPred(query);
  }
}
