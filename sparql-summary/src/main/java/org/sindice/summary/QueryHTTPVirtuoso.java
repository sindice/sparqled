package org.sindice.summary;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import org.openrdf.model.Resource;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;

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
public class QueryHTTPVirtuoso extends Query {

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
	public QueryHTTPVirtuoso(Dump d, String websiteURL, String user,
	    String password) throws RepositoryException, SesameBackendException {
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
	public QueryHTTPVirtuoso(Dump d, String websiteURL)
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
	public QueryHTTPVirtuoso(String websiteURL, String user, String password)
	    throws RepositoryException, SesameBackendException {
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
	public QueryHTTPVirtuoso(String websiteURL) throws RepositoryException,
	    SesameBackendException {
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
	public QueryHTTPVirtuoso(Dump d) throws RepositoryException,
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
		_queriesResults = new Stack<TupleQueryResult>();

		String query = "SELECT ?label (COUNT (?s) AS ?cardinality)\n" + _graphFrom
		    + "WHERE {\n{\n" + "SELECT ?s (sql:GROUP_CONCAT(IF(isURI(?type),\n";

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
				query += "), \" \") AS ?label)\n" + "        WHERE {\n"
				    + "        {\n" + "            SELECT ?s ?type ?p WHERE\n"
				    + "            {\n";

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
				query += "), \" \") AS ?label)\n" + "        WHERE {\n"
				    + "        {\n" + "            SELECT ?s ?type WHERE\n"
				    + "            {\n" + "                { ?s <"
				    + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(0)
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
