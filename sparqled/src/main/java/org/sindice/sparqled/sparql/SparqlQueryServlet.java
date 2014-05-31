/**
 * Copyright (c) 2012 National University of Ireland, Galway. All Rights Reserved.
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
package org.sindice.sparqled.sparql;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.parser.sparql.ast.ASTAskQuery;
import org.openrdf.query.parser.sparql.ast.ASTConstructQuery;
import org.openrdf.query.parser.sparql.ast.ASTDescribeQuery;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTSelectQuery;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.sparqled.sparql.preprocessing.PreProcessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class SparqlQueryServlet
extends HttpServlet {

  private static final long   serialVersionUID = 4137296200305461786L;

  private static final Logger logger           = LoggerFactory.getLogger(SparqlQueryServlet.class);

  private SesameBackend       _repository;
  private PreProcessing       preprocessing    = null;

  /**
   * Initialize the proxy servlet
   * 
   * @param config
   *          configuration file
   */
  @Override
  public void init(ServletConfig config)
  throws ServletException {
    super.init(config);

    final ServletContext c = config.getServletContext();
    final String prep = (String) getParameter(c, SparqlQueryServletListener.PREPROCESSING);
    try {
      if (prep != null && !prep.isEmpty()) {
        final String[] prepArgs = (String[]) getParameter(c, SparqlQueryServletListener.PREPROCESSING_ARGS);
        preprocessing = (PreProcessing) Class.forName(prep).newInstance();
        if (preprocessing.getVarPrefix() == null || preprocessing.getVarPrefix().isEmpty() ||
            preprocessing.getVarSuffix() == null || preprocessing.getVarSuffix().isEmpty()) {
          throw new RuntimeException("The PreProcessing Class must return non empty prefix/suffix");
        }
        preprocessing.init(prepArgs);
        logger.info("Using preprocessing class: {} args={}", prep, Arrays.toString(prepArgs));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    final BackendType backend = BackendType.valueOf((String) getParameter(c, SparqlQueryServletListener.BACKEND));
    final String[] backendArgs = (String[]) getParameter(c, SparqlQueryServletListener.BACKEND_ARGS);

    // create repository
    _repository = SesameBackendFactory.getDgsBackend(backend, backendArgs);
    try {
      _repository.initConnection();
    } catch (SesameBackendException e) {
      logger.error("", e);
    }
    logger.info("Intialized Proxy Servlet");
  }

  private Object getParameter(ServletContext c, String param) {
    return c.getAttribute(SparqlQueryServletListener.SQS_WRAPPER + param);
  }

  @Override
  public void destroy() {
    logger.info("Destroy Proxy Servlet");
    try {
      _repository.closeConnection();
    } catch (SesameBackendException e) {
      logger.error("", e);
    }
    super.destroy();
  }

  /**
   * Process request from editor, provides recommendations
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    try {
      getResult(request, response);
    } catch (Exception e) {
      logger.error("", e);
    }
  }

  /**
   * Process request from editor, provides recommendations
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    try {
      getResult(request, response);
    } catch (Exception e) {
      logger.error("", e);
    }
  }

  /**
   * Send the query to the repository, retrieve the result and format it.
   */
  private void getResult(HttpServletRequest request, HttpServletResponse response)
  throws Exception {
    response.setContentType("application/json");

    // get the query
    final String query;
    if (preprocessing != null) {
      query = preprocessing.process(URLDecoder.decode(request.getParameter(Protocol.QUERY_PARAM_NAME), "UTF-8"));
    } else {
      query = URLDecoder.decode(request.getParameter(Protocol.QUERY_PARAM_NAME), "UTF-8");
    }

    // Process the query
    logger.debug(query);
    QueryIterator queryIt;
    try {
      queryIt = _repository.submit(query);

      ASTQueryContainer ast = queryIt.getQueryAst();

      if (ast.getQuery() instanceof ASTSelectQuery) {
        parseSelect(response, queryIt);
      } else if (ast.getQuery() instanceof ASTConstructQuery) {
        parseGraphResult(response, queryIt);
      } else if (ast.getQuery() instanceof ASTDescribeQuery) {
        parseGraphResult(response, queryIt);
      } else if (ast.getQuery() instanceof ASTAskQuery) {
        parseAsk(response, queryIt);
      }
    } catch (SesameBackendException e) {
      final JSONObject json = new JSONObject();
      final PrintWriter out = response.getWriter();

      JSONObject results = new JSONObject();

      results.put("distinct", "false");
      results.put("ordered", "true");
      json.put("status", "ERROR");
      json.put("message", e.getLocalizedMessage());
      out.print(json);
      out.flush();
      out.close();
    }
  }

  private void parseSelect(HttpServletResponse response, final QueryIterator<BindingSet> queryIt)
  throws IOException {
    final JSONObject json = new JSONObject();
    final PrintWriter out = response.getWriter();

    // Parse the result to a JSON format
    final JSONObject head = new JSONObject();
    head.put("link", "[]");
    JSONObject results = new JSONObject();

    results.put("distinct", "false");
    results.put("ordered", "true");

    try {
      while (queryIt.hasNext()) {
        // Find the correct format of the return value.
        BindingSet result = queryIt.next();
        Map<String, JSONObject> bindings = new HashMap<String, JSONObject>();

        for (Binding binding : result) {
          if (preprocessing != null && binding.getName().startsWith(preprocessing.getVarPrefix()) &&
          binding.getName().endsWith(preprocessing.getVarSuffix())) {
            /*
             * Filter additional SPARQL variables produced by
             * the preprocessing class
             */
            continue;
          }
          bindings.put(binding.getName(), getSerializedValue(binding.getValue()));
        }
        results.accumulate("bindings", bindings);
      }
      if (!results.containsKey("bindings")) {
        // handle empty select result
        results.put("bindings", "[]");
      }

      for (String name : queryIt.getBindingNames()) {
        if (preprocessing != null &&
            name.startsWith(preprocessing.getVarPrefix()) &&
            name.endsWith(preprocessing.getVarSuffix())) {
          /*
           * Filter additional SPARQL variables produced by the
           * preprocessing class
           */
          continue;
        }
        head.accumulate("vars", name);
      }
      json.put("results", results);

      json.put("head", head);
      json.put("status", "SUCCESS");
      json.put("message", "");
    } catch (Exception e) {
      logger.error("Cannot compute the query.", e);
      json.put("status", "ERROR");
      json.put("message", e.getLocalizedMessage());
    }
    out.print(json);
    out.flush();
    out.close();
  }

  private void parseGraphResult(HttpServletResponse response, final QueryIterator<Statement> queryIt)
  throws IOException {
    final JSONObject json = new JSONObject();
    final PrintWriter out = response.getWriter();

    // Parse the result to a JSON format
    final JSONObject head = new JSONObject();
    head.put("link", "[]");
    JSONObject results = new JSONObject();

    results.put("distinct", "false");
    results.put("ordered", "true");

    try {
      while (queryIt.hasNext()) {
        // Find the correct format of the return value.
        Statement st = queryIt.next();
        Map<String, JSONObject> bindings = new HashMap<String, JSONObject>();
        bindings.put("s", getSerializedValue(st.getSubject()));
        bindings.put("p", getSerializedValue(st.getPredicate()));
        bindings.put("o", getSerializedValue(st.getObject()));
        results.accumulate("bindings", bindings);
        logger.debug("s=[{}] p=[{}] o=[{}]", st.getSubject(), st.getPredicate(), st.getObject());
      }
      if (!results.containsKey("bindings")) {
        // handle empty select result
        results.put("bindings", "[]");
      }

      head.accumulate("vars", "s");
      head.accumulate("vars", "p");
      head.accumulate("vars", "o");
      json.put("results", results);

      json.put("head", head);
      json.put("status", "SUCCESS");
      json.put("message", "");
    } catch (Exception e) {
      logger.error("Cannot compute the query.", e);
      json.put("status", "ERROR");
      json.put("message", e.getLocalizedMessage());
    }
    out.print(json);
    out.flush();
    out.close();
  }

  /**
   * Returns a JSON object for the serialization of the result {@link Value}.
   * @param v the {@link Value} of a {@link Binding}
   * @return a {@link JSONObject} built according to
   * <a href="http://www.w3.org/TR/rdf-sparql-json-res/#variable-binding-results">
   * http://www.w3.org/TR/rdf-sparql-json-res/#variable-binding-results</a>
   */
  private JSONObject getSerializedValue(Value v) {
    JSONObject node = new JSONObject();

    node.put("value", v.stringValue());
    if (v instanceof URI) {
      node.put("type", "uri");
    } else if (v instanceof BNode) {
      node.put("type", "bnode");
    } else {
      final Literal lit = (Literal) v;
      final String lang = lit.getLanguage();
      final URI datatype = lit.getDatatype();
      if (datatype != null) {
        node.put("type", "typed-literal");
        node.put("datatype", datatype.stringValue());
      } else {
        node.put("type", "literal");
        if (lang != null) {
          node.put("xml:lang", lang);
        }
      }
    }
    return node;
  }

  private void parseAsk(HttpServletResponse response, final QueryIterator<Boolean> queryIt)
  throws IOException {
    final JSONObject json = new JSONObject();

    // Parse the result to a JSON format
    final JSONObject head = new JSONObject();
    head.put("link", "[]");
    JSONObject results = new JSONObject();

    results.put("distinct", "false");
    results.put("ordered", "true");

    try {
      while (queryIt.hasNext()) {
        // Find the correct format of the return value.
        Boolean result = queryIt.next();
        json.put("boolean", result);
      }
      json.put("head", head);
      json.put("status", "SUCCESS");
      json.put("message", "");
    } catch (Exception e) {
      logger.error("Cannot compute the query.", e);
      json.put("status", "ERROR");
      json.put("message", e.getLocalizedMessage());
    }
    logger.debug(json.toString());

    final PrintWriter out = response.getWriter();
    out.print(json);
    out.flush();
    out.close();
  }

}
