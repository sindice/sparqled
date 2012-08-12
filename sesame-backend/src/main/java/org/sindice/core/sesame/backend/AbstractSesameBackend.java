/*******************************************************************************
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
 *******************************************************************************/
/**
 * @project sparql-editor-servlet
 * @author Pierre Bailly <pierre.bailly@deri.org>
 * @author Campinas Stephane [ 28 Feb 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.core.sesame.backend;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.openrdf.model.Resource;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.parser.sparql.ast.ASTAskQuery;
import org.openrdf.query.parser.sparql.ast.ASTConstructQuery;
import org.openrdf.query.parser.sparql.ast.ASTDescribeQuery;
import org.openrdf.query.parser.sparql.ast.ASTProjectionElem;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTSelectQuery;
import org.openrdf.query.parser.sparql.ast.ASTVar;
import org.openrdf.query.parser.sparql.ast.ParseException;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.query.parser.sparql.ast.VisitorException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.n3.N3ParserFactory;
import org.openrdf.rio.ntriples.NTriplesParserFactory;
import org.openrdf.rio.rdfxml.RDFXMLParserFactory;
import org.openrdf.rio.trig.TriGParserFactory;
import org.openrdf.rio.trix.TriXParserFactory;
import org.openrdf.rio.turtle.TurtleParserFactory;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stephane Campinas
 * @email stephane.campinas@deri.org
 * @param <VALUE>
 * @param <CONTEXT>
 */
public abstract class AbstractSesameBackend<VALUE, CONTEXT>
implements SesameBackend<VALUE, CONTEXT> {

  protected static final Logger                      logger = LoggerFactory
                                                            .getLogger(AbstractSesameBackend.class);

  private final QueryResultProcessor<VALUE, CONTEXT> qrp;

  private RepositoryConnection                       con;
  private Repository                                 repository;

  public AbstractSesameBackend() {
    this(null);
  }

  public AbstractSesameBackend(QueryResultProcessor<VALUE, CONTEXT> qrp) {
    this.qrp = qrp;
    initSesameRDFParsers();
  }

  private final void initSesameRDFParsers() {
    /* N3 */
    RDFParserRegistry.getInstance().add(new RDFParserFactory() {
      @Override
      public RDFFormat getRDFFormat() {
        return RDFFormat.N3;
      }

      @Override
      public RDFParser getParser() {
        return new N3ParserFactory().getParser();
      }
    });
    /* NTRIPLES */
    RDFParserRegistry.getInstance().add(new RDFParserFactory() {
      @Override
      public RDFFormat getRDFFormat() {
        return RDFFormat.NTRIPLES;
      }

      @Override
      public RDFParser getParser() {
        return new NTriplesParserFactory().getParser();
      }
    });
    /* RDFXML */
    RDFParserRegistry.getInstance().add(new RDFParserFactory() {
      @Override
      public RDFFormat getRDFFormat() {
        return RDFFormat.RDFXML;
      }

      @Override
      public RDFParser getParser() {
        return new RDFXMLParserFactory().getParser();
      }
    });
    /* TRIG */
    RDFParserRegistry.getInstance().add(new RDFParserFactory() {
      @Override
      public RDFFormat getRDFFormat() {
        return RDFFormat.TRIG;
      }

      @Override
      public RDFParser getParser() {
        return new TriGParserFactory().getParser();
      }
    });
    /* TRIX */
    RDFParserRegistry.getInstance().add(new RDFParserFactory() {
      @Override
      public RDFFormat getRDFFormat() {
        return RDFFormat.TRIX;
      }

      @Override
      public RDFParser getParser() {
        return new TriXParserFactory().getParser();
      }
    });
    /* TURTLE */
    RDFParserRegistry.getInstance().add(new RDFParserFactory() {
      @Override
      public RDFFormat getRDFFormat() {
        return RDFFormat.TURTLE;
      }

      @Override
      public RDFParser getParser() {
        return new TurtleParserFactory().getParser();
      }
    });
  }

  @Override
  public void initConnection()
  throws SesameBackendException {
    try {
      repository = getRepository();
      repository.initialize();
    } catch (RepositoryException e) {
      logger.error("", e);
      throw new SesameBackendException(e);
    }
    try {
      con = repository.getConnection();
      con.setAutoCommit(true);
    } catch (RepositoryException e) {
      logger.error("Unable to get a connection to the repository", e);
      con = null;
      throw new SesameBackendException(e);
    }
  }

  @Override
  public QueryIterator<VALUE, CONTEXT> submit(String query)
  throws SesameBackendException {
    return new SesameQueryIterator(qrp, query);
  }

  @Override
  public QueryIterator<VALUE, CONTEXT> submit(QueryResultProcessor<VALUE, CONTEXT> qrp,
                                              String query)
  throws SesameBackendException {
    return new SesameQueryIterator(qrp, query);
  }

  protected abstract Repository getRepository();

  @Override
  public RepositoryConnection getConnection() {
    return con;
  }

  public void closeConnection()
  throws SesameBackendException {
    try {
      if (con != null) {
        con.close();
      }
    } catch (RepositoryException e) {
      logger.error("{}", e);
      throw new SesameBackendException(e);
    }
    finally {
      try {
        if (con != null) {
          con.getRepository().shutDown();
        }
      } catch (RepositoryException e) {
        logger.error("{}", e);
        throw new SesameBackendException(e);
      }
    }
  }

  private void addDirectoryToRepository(File path,
                                        String baseURI,
                                        RDFFormat format,
                                        Resource... contexts)
  throws SesameBackendException {
    if (path.exists()) {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (!files[i].exists()) {
          throw new IllegalArgumentException("The file " + files[i] +
                                             " doesn't exist");
        }
        if (files[i].isDirectory()) {
          addDirectoryToRepository(files[i], baseURI, format, contexts);
        } else {
          logger.info("ADD FILE: " + files[i] + " (" + format + ")");
          try {
            if (files[i].getAbsolutePath().endsWith(".gz")) {
              final InputStream compressedFile = new GZIPInputStream(new BufferedInputStream(new FileInputStream(files[i])));
              getConnection().add(compressedFile, baseURI, format, contexts);
            } else {
              getConnection().add(files[i], baseURI, format, contexts);
            }
            logger.info("FILE ADDED: " + getConnection().size(contexts) +
                        " statements");
          } catch (RDFParseException e) {
            logger.error("", e);
            throw new SesameBackendException(e);
          } catch (RepositoryException e) {
            logger.error("", e);
            throw new SesameBackendException(e);
          } catch (IOException e) {
            logger.error("", e);
            throw new SesameBackendException(e);
          }
        }
      }
    }
  }

  @Override
  public void addToRepository(File path, RDFFormat format, Resource... contexts)
  throws SesameBackendException {
    final String baseURI = "";

    if (!path.exists()) {
      throw new IllegalArgumentException("The file " + path + " doesn't exist");
    }
    try {
      if (path.getAbsolutePath().endsWith(".gz")) {
        final InputStream compressedFile = new GZIPInputStream(new BufferedInputStream(new FileInputStream(path)));

        logger.info("ADD FILE: " + path + " (" + format + ")");
        getConnection().add(compressedFile, baseURI, format, contexts);
        logger.info("FILE ADDED: " + getConnection().size(contexts) +
                    " statements");
      } else {
        if (path.isFile()) {
          logger.info("Number of stamement :" + getConnection().size(contexts));
          logger.info("ADD FILE: " + path + " (" + format + ")");
          getConnection().add(path, baseURI, format, contexts);
          logger.info("FILE ADDED: " + getConnection().size(contexts) +
                      " statements");
        } else if (path.isDirectory()) { /* Directory */
          addDirectoryToRepository(path, baseURI, format, contexts);
        }
      }
    } catch (FileNotFoundException e) {
      logger.error("", e);
      throw new SesameBackendException(e);
    } catch (IOException e) {
      logger.error("", e);
      throw new SesameBackendException(e);
    } catch (RDFParseException e) {
      logger.error("", e);
      throw new SesameBackendException(e);
    } catch (RepositoryException e) {
      logger.error("", e);
      throw new SesameBackendException(e);
    }
  }

  private class SesameQueryIterator
  extends QueryIterator<VALUE, CONTEXT> {

    private int                                        pagination      = LIMIT;
    private long                                       limit           = 0;                          // user defined limit: by default get everything
    private long                                       offset          = 0;                          // user defined offset
    private long                                       paginatedOffset = 0;                          // offset used for the paginated
                                                                                                      // queries
    private String                                     query;
    private SesameQRHandler<?>                         results         = null;
    private final CONTEXT                              context;

    private final Set<String>                          bindingNames    = new LinkedHashSet<String>();

    // initialize the iterator to the passed query
    private boolean                                    init            = false;
    private final ASTQueryContainer                    ast;

    private final Matcher                              rmLimit         = Pattern
                                                                       .compile("limit\\s?\\d+", Pattern.CASE_INSENSITIVE)
                                                                       .matcher("");
    private final Matcher                              rmOffset        = Pattern
                                                                       .compile("offset\\s?\\d+", Pattern.CASE_INSENSITIVE)
                                                                       .matcher("");

    private final QueryResultProcessor<VALUE, CONTEXT> qrp;

    public SesameQueryIterator(QueryResultProcessor<VALUE, CONTEXT> qrp,
                               String query) throws SesameBackendException {
      this.qrp = qrp;
      this.query = query;
      try {
        this.ast = SyntaxTreeBuilder.parseQuery(query);
      } catch (TokenMgrError e) {
        throw new SesameBackendException(e);
      } catch (ParseException e) {
        throw new SesameBackendException(e);
      }

      // SELECT QUERY - get the binding names
      if (ast.getQuery() instanceof ASTSelectQuery) {
        doGetBindingNames();
      }
      if (qrp == null) {
        context = (CONTEXT) new Context();
      } else {
        context = this.qrp.getContext();
      }
    }

    @Override
    public ASTQueryContainer getQueryAst() {
      return ast;
    }

    private void doGetBindingNames()
    throws SesameBackendException {
      final ASTSelectQuery selectQuery = (ASTSelectQuery) ast.getQuery();

      if (selectQuery.getSelect().isWildcard()) { // get all the variables
        // in the query
        try {
          RetrieveVariables.process(ast, bindingNames);
        } catch (VisitorException e) {
          throw new SesameBackendException(e);
        }
      } else {
        for (ASTProjectionElem pe : selectQuery.getSelect()
        .getProjectionElemList()) {
          if (pe.hasAlias()) {
            bindingNames.add(pe.getAlias());
          } else {
            bindingNames.add(pe.jjtGetChild(ASTVar.class).getName());
          }
        }
      }
    }

    public void setPagination(int l) {
      pagination = l;
    }

    /**
     * Find the correct "LIMIT" and "OFFSET" for an iteration of a query
     * 
     * @return The query to send to the back-end
     */
    protected String constructSearchSequence() {
      if (pagination == 0) { // No Pagination
        return query;
      } else { // With Pagination
        final long newOffset = pagination * (paginatedOffset++) + offset;
        if (limit == 0) { // get all the results
          return query + "\nLIMIT " + pagination + "\nOFFSET " + newOffset;
        } else { // get the first limit-results
          if (limit > newOffset + pagination) {
            return query + "\nLIMIT " + pagination + "\nOFFSET " + newOffset;
          } else { // last page
            return query + "\nLIMIT " + (limit - newOffset) + "\nOFFSET " +
                   newOffset;
          }
        }
      }
    }

    /**
     * @return true if initialisation is OK
     */
    private boolean init() {
      try {
        if (pagination != 0) {
          // if the user specified a range, we will paginate the
          // results within that range
          if (!ast.containsQuery()) {
            return false;
          }
          if (ast.getQuery() instanceof ASTAskQuery) {
            pagination = 0; // disable pagination
          } else {
            if (ast.getQuery().getOffset() != null) {
              offset = ast.getQuery().getOffset().getValue();
              rmOffset.reset(this.query);
              this.query = rmOffset
              .region(this.query.lastIndexOf('}'), this.query.length())
              .replaceFirst("");
            }
            if (ast.getQuery().getLimit() != null) {
              /*
               * Add the offset, so that the pagination is correct
               * within that range
               */
              limit = ast.getQuery().getLimit().getValue() + offset;
              rmLimit.reset(this.query);
              this.query = rmLimit
              .region(this.query.lastIndexOf('}'), this.query.length())
              .replaceFirst("");
            }
          }
        }
        return true;
      } catch (TokenMgrError e) {
        logger.error("", e);
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean hasNext() {
      // first call to hasNext, initialise the query
      if (!init) {
        init = true;
        if (!init()) {
          return false;
        }
        if ((results = submitQuery()) == null) {
          return false;
        }
      }

      try {
        if (!results.hasNext()) {
          if (pagination == 0 || // all results were received with the
                                 // previous query
              (limit != 0 && pagination * paginatedOffset + offset >= limit)) { // the limit has been
            // reached
            close();
            return false;
          }
          results = submitQuery();
          if (results == null || !results.hasNext()) {
            close();
            return false;
          }
        }
      } catch (QueryEvaluationException e) {
        logger.error("", e);
        throw new RuntimeException(e);
      }
      return true;
    }

    private SesameQRHandler<?> submitQuery() {
      try {
        final SesameQRHandler<?> res;
        final String pagQuery = constructSearchSequence();
        logger.debug("Query=[{}]", pagQuery);

        if (ast.getQuery() instanceof ASTSelectQuery) {
          res = new BindingSetSesameQRHandler();
          final TupleQuery tupleQuery = getConnection()
          .prepareTupleQuery(QueryLanguage.SPARQL, pagQuery);
          res.set(tupleQuery.evaluate());
        } else if (ast.getQuery() instanceof ASTAskQuery) {
          res = new BooleanSesameQRHandler();
          final BooleanQuery booleanQuery = getConnection()
          .prepareBooleanQuery(QueryLanguage.SPARQL, pagQuery);
          res.set(booleanQuery.evaluate());
        } else if (ast.getQuery() instanceof ASTConstructQuery ||
                   ast.getQuery() instanceof ASTDescribeQuery) {
          res = new StatementSesameQRHandler();
          final GraphQuery graphQuery = getConnection()
          .prepareGraphQuery(QueryLanguage.SPARQL, pagQuery);
          res.set(graphQuery.evaluate());
        } else {
          logger.error("Unsupported query: {}\n{}", ast.getQuery(), query);
          throw new RuntimeException("Unsupported query: " +
                                     ast.getQuery().getClass().getSimpleName());
        }
        return res;
      } catch (RepositoryException e) {
        logger.error("", e);
        throw new RuntimeException(e);
      } catch (MalformedQueryException e) {
        logger.error("", e);
        throw new RuntimeException(e);
      } catch (QueryEvaluationException e) {
        logger.error("", e);
        throw new RuntimeException(e);
      }
    }

    @Override
    public Set<String> getBindingNames() {
      return bindingNames;
    }

    private void close() {
      if (results != null) {
        try {
          results.close();
        } catch (QueryEvaluationException e) {
          logger.error("{}", e);
        }
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public VALUE next() {
      try {
        if (qrp == null) {
          return (VALUE) results.next();
        }
        return qrp.process(results.next(), getContext());
      } catch (QueryEvaluationException e) {
        logger.error("", e);
        throw new RuntimeException(e);
      }
    }

    @Override
    public CONTEXT getContext() {
      return context;
    }

  }

}
