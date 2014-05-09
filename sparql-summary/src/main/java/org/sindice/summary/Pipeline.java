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
package org.sindice.summary;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.summary.AbstractQuery.SummaryAlgorithm;
import org.sindice.summary.multilabelled.HTTPMultiLabelledQuery;
import org.sindice.summary.multilabelled.HTTPVirtuosoMultiLabelledQuery;
import org.sindice.summary.multilabelled.MemoryMultiLabelledQuery;
import org.sindice.summary.multilabelled.NativeMultiLabelledQuery;
import org.sindice.summary.multilabelled.RDBMSMultiLabelledQuery;
import org.sindice.summary.singlelabelled.HTTPSingleLabelledQuery;
import org.sindice.summary.singlelabelled.HTTPVirtuosoSingleLabelledQuery;
import org.sindice.summary.singlelabelled.MemorySingleLabelledQuery;
import org.sindice.summary.singlelabelled.NativeSingleLabelledQuery;
import org.sindice.summary.singlelabelled.RDBMSSingleLabelledQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class Pipeline {

  private final static Logger logger            = LoggerFactory.getLogger(Pipeline.class);

  private static final String HELP              = "help";
  private static final String CLASS_ATTRIBUTE   = "class-attribute";
  private static final String TYPE              = "type";
  private static final String FEED              = "feed";
  private static final String ADD               = "add";
  private static final String ADDFORMAT         = "addformat";
  private static final String PAGINATION        = "pagination";
  private static final String SUMMARY_ALGORITHM = "summary-algorithm";

  /**
   * @param
   * @throws SesameBackendException
   */
  public static void main(String[] args) throws SesameBackendException {
    OptionParser parser = new OptionParser() {
      {
        accepts(
            TYPE,
            "Type of the input file "
                + Arrays.toString(SesameBackendFactory.BackendType.values()))
            .withRequiredArg().ofType(SesameBackendFactory.BackendType.class)
            .required();
        acceptsAll(
            asList(FEED, "feedmode"),
            "The feed mode allow you to create a repository without launching the SPARQL queries");
        acceptsAll(
            asList("repository", "input", "url"),
            "Directory where the local repository is."
                + " Create the repository if "
                + "notthing is found. Or the url of the webserver.")
            .withRequiredArg().required();
        accepts(ADD, "Add a file to the local repository.").withRequiredArg();
        accepts(ADDFORMAT, "The type of the input file: " + RDFFormat.values())
            .withRequiredArg().ofType(RDFFormat.class)
            .defaultsTo(RDFFormat.NTRIPLES);
        accepts(PAGINATION, "Limit of the pagination (0 for infinite).")
            .withRequiredArg().ofType(Integer.class).defaultsTo(0);
        acceptsAll(asList("domain", "graph"),
            "Limit the search to one domain.").withRequiredArg();
        accepts("outputfile", "The output file. It will be GZIP compressed.").withRequiredArg().required();
        acceptsAll(asList("database", "db"), "The database in the MYSQL.")
            .withRequiredArg();
        accepts("user", "The user for a MYSQL connection.").withRequiredArg();
        acceptsAll(asList("pass", "password"),
            "The password for a MYSQL connection.").withRequiredArg();
        accepts(
            CLASS_ATTRIBUTE,
            "for the user defined class-attribute. The default class-attribute is: "
                + AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE)
            .withRequiredArg().ofType(String.class);
        acceptsAll(asList(SUMMARY_ALGORITHM, "algorithm"),
            "Allow the user to change the clustering algorithm to create the summary: " + Arrays.toString(SummaryAlgorithm.values()))
            .withRequiredArg().ofType(SummaryAlgorithm.class).defaultsTo(SummaryAlgorithm.MULTI_LABELLED);
        accepts(HELP, "show help");
      }
    };

    // Show the help
    OptionParser helpParser = new OptionParser() {
      {
        accepts(HELP);
        acceptsAll(asList(FEED, "feedmode"));
        accepts(TYPE);
        acceptsAll(asList("repository", "input", "url"));
        accepts(ADD);
        accepts(ADDFORMAT);
        accepts(PAGINATION);
        acceptsAll(asList("domain", "graph"));
        accepts("outputfile");
        acceptsAll(asList("database", "db"));
        accepts("user");
        acceptsAll(asList("pass", "password"));
        accepts(CLASS_ATTRIBUTE);
        acceptsAll(asList(SUMMARY_ALGORITHM, "algorithm"));
      }
    };

    // feed mode
    OptionParser feedParser = new OptionParser() {
      {
        acceptsAll(asList(FEED, "feedmode")).isRequired();
        accepts(TYPE).withRequiredArg()
            .ofType(SesameBackendFactory.BackendType.class).required();
        acceptsAll(asList("repository", "input", "url")).withRequiredArg()
            .required();
        acceptsAll(asList("domain", "graph")).withRequiredArg();
        accepts(ADD).withRequiredArg().required();
        accepts(ADDFORMAT).withRequiredArg().ofType(RDFFormat.class)
            .defaultsTo(RDFFormat.NTRIPLES);
        acceptsAll(asList("database", "db")).withRequiredArg();
        accepts("user").withRequiredArg();
        acceptsAll(asList("pass", "password")).withRequiredArg();
      }
    };

    OptionSet options = helpParser.parse(args);
    if (options.has(HELP)) {
      try {
        parser.printHelpOn(System.out);
      } catch (IOException e) {
        logger.error("", e);
      }
      System.exit(0);
    }

    if (options.has(FEED)) {
      // Parse the other options in the feed mode
      options = feedParser.parse(args);
      feedInput(options);

    } else {
      // Parse the other options in the normal mode
      options = parser.parse(args);
      normalInput(options);
    }
  }

  /**
   * Get the object MultiLabelledQuery asked by the user.
   * 
   * @param options
   *          The valid options from STDIN
   */
  private static AbstractQuery getMultiLabelledQueryInstance(OptionSet options)
      throws RepositoryException, RDFParseException, IOException,
      SesameBackendException {
    // First at all, get the location of the local repository
    String repository = "";
    repository = options.valueOf("repository").toString();

    AbstractQuery q = null;
    if (options.valueOf(TYPE).equals(SesameBackendFactory.BackendType.RDBMS)) {
      if (options.has("db") && options.hasArgument("db")) {
        if (options.has("user") && options.hasArgument("user")) {
          if (options.has("pass") && options.hasArgument("pass")) {
            q = new RDBMSMultiLabelledQuery(repository, options.valueOf("db")
                .toString(), options.valueOf("user").toString(), options
                .valueOf("pass").toString());
          } else {
            logger.error("You need to define a password "
                + "to connect to a MYSQL database");
            throw new SesameBackendException("You need to define a password "
                + "to connect to a MYSQL database");
          }
        } else {
          logger.error("You need to define a user to "
              + "connect to a MYSQL database");
          throw new SesameBackendException("You need to define a user to "
          + "connect to a MYSQL database");
        }
      } else {
        logger.error("You need to select a database");
        throw new SesameBackendException("You need to select a database");
      }
    } else if (options.valueOf(TYPE).equals(SesameBackendFactory.BackendType.NATIVE)) {
      q = new NativeMultiLabelledQuery(repository);
    } else if (options.valueOf(TYPE).equals(SesameBackendFactory.BackendType.MEMORY)) {
      q = new MemoryMultiLabelledQuery(repository);
    } else if (options.valueOf(TYPE).equals(SesameBackendFactory.BackendType.HTTP)) {
      q = new HTTPMultiLabelledQuery(repository);
    } else if (options.valueOf(TYPE).equals(SesameBackendFactory.BackendType.VIRTUOSO)) {
      q = new HTTPVirtuosoMultiLabelledQuery(new Dump(), repository);
    }
    return q;
  }

  /**
   * Get the object SignleLabelledQuery asked by the user.
   * 
   * @param options
   *          The valid options from STDIN
   */
  private static AbstractQuery getSingleLabelledQueryInstance(OptionSet options)
      throws RepositoryException, RDFParseException, IOException,
      SesameBackendException {
    // First at all, get the location of the local repository
    String repository = "";
    repository = options.valueOf("repository").toString();

    AbstractQuery q = null;
    if (options.valueOf(TYPE).equals(SesameBackendFactory.BackendType.RDBMS)) {
      if (options.has("db") && options.hasArgument("db")) {
        if (options.has("user") && options.hasArgument("user")) {
          if (options.has("pass") && options.hasArgument("pass")) {
            q = new RDBMSSingleLabelledQuery(repository, options.valueOf("db")
                .toString(), options.valueOf("user").toString(), options
                .valueOf("pass").toString());
          } else {
            logger.error("You need to define a password "
                + "to connect to a MYSQL database");
            throw new SesameBackendException("You need to define a password "
            + "to connect to a MYSQL database");
          }
        } else {
          logger.error("You need to define a user to "
              + "connect to a MYSQL database");
          throw new SesameBackendException("You need to define a user to "
          + "connect to a MYSQL database");
        }
      } else {
        logger.error("You need to select a database");
        throw new SesameBackendException("You need to select a database");
      }
    } else if (options.valueOf(TYPE).equals(SesameBackendFactory.BackendType.NATIVE)) {
      q = new NativeSingleLabelledQuery(repository);
    } else if (options.valueOf(TYPE).equals(SesameBackendFactory.BackendType.MEMORY)) {
      q = new MemorySingleLabelledQuery(repository);
    } else if (options.valueOf(TYPE).equals(SesameBackendFactory.BackendType.HTTP)) {
      q = new HTTPSingleLabelledQuery(repository);
    } else if (options.valueOf(TYPE).equals(SesameBackendFactory.BackendType.VIRTUOSO)) {
      q = new HTTPVirtuosoSingleLabelledQuery(new Dump(), repository);
    }
    return q;
  }

  /**
   * Parse the STDIN input, then launch the query and the dump.
   * 
   * @param options
   *          The valid options from STDIN
   * @throws SesameBackendException
   */
  private static void normalInput(OptionSet options)
      throws SesameBackendException {

    if (options.has(CLASS_ATTRIBUTE)) {
      // Suggestion :
      // "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
      // "http://opengraphprotocol.org/schema/type",
      // "http://ogp.me/ns#type",
      // "http://opengraph.org/schema/type",
      // "http://purl.org/dc/elements/1.1/type",
      // "http://dbpedia.org/property/type"

      String[] classAttributes = new String[options.valuesOf(CLASS_ATTRIBUTE)
          .size()];

      int i = 0;
      for (Object elt : options.valuesOf(CLASS_ATTRIBUTE)) {
        classAttributes[i++] = elt.toString();
      }

      AnalyticsClassAttributes.initClassAttributes(classAttributes);
    } else {
      String[] defaultClassAttribute = { AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE };
      AnalyticsClassAttributes.initClassAttributes(defaultClassAttribute);
    }
    // Then create the Query. The local repository is created and open in
    // the query.
    AbstractQuery q = null;
    try {
      final SummaryAlgorithm sa = (SummaryAlgorithm) options.valueOf(SUMMARY_ALGORITHM);
      if (sa.equals(SummaryAlgorithm.MULTI_LABELLED)) {
        q = getMultiLabelledQueryInstance(options);
      } else {
        q = getSingleLabelledQueryInstance(options);
      }
    } catch (Exception e) {
      // Fail at the connection, need to print the stack

      logger.error(e.getMessage());
      try {
        q.stopConnexion();
      } catch (Exception e1) {
        logger.error("", e1);
      }
      throw new SesameBackendException("Connection failed", e);
    }

    // Set the domain
    String domain = "";
    if (options.has("domain") && options.hasArgument("domain")) {
      domain = options.valueOf("domain").toString();
      q.setGraph(domain);
    }

    // Add file to the repository
    if (options.has(ADD) && options.hasArgument(ADD)) {
      for (Object newFile : options.valuesOf(ADD)) {
        try {
          if (options.has(ADDFORMAT) && options.hasArgument(ADDFORMAT)) {
            if (domain.equals("")) {
              q.addFileToRepository(newFile.toString(),
                  (RDFFormat) options.valueOf(ADDFORMAT));
            } else {
              q.addFileToRepository(newFile.toString(), (RDFFormat) options
                  .valueOf(ADDFORMAT), NTriplesUtil.parseResource("<" + domain
                  + ">", new MemValueFactory()));
            }
          } else {
            // Ntriples format
            if (domain.equals("")) {
              q.addFileToRepository(newFile.toString(), RDFFormat.NTRIPLES);
            } else {
              q.addFileToRepository(newFile.toString(), RDFFormat.NTRIPLES,
                  NTriplesUtil.parseResource("<" + domain + ">",
                      new MemValueFactory()));
            }
          }
        } catch (RDFParseException e) {
          // Invalid output RDF => print the stack
          logger.error("", e);
          throw new SesameBackendException(e);
        } catch (RepositoryException e) {
          logger.warn("Repository not found\n");
          logger.error("", e);
          throw new SesameBackendException(e);
        } catch (IOException e) {
          logger.warn("File not found or not readable\n");
          logger.error("", e);
          throw new SesameBackendException(e);
        }
      }
    }

    // Change the pagination
    if (options.has(PAGINATION)) {
      q.setPagination((Integer) options.valueOf(PAGINATION));
    } else {
      q.setPagination(0);
    }

    // launch the query
    try {
      // get the outputfile no need more check
      String outputfile = options.valueOf("outputfile").toString();

      // Initialize DUMP
      q.initDump(outputfile);

      // Launch query for getting node type and node cardinality
      q.computeName();

      // Launch query for getting predicates
      q.computePredicate();
    } catch (Exception e) {
      logger.error("Wrong query", e);
      try {
        q.stopConnexion();
      } catch (Exception e1) {
        logger.error("", e1);
      }
      throw new SesameBackendException("Wrong query", e);
    } finally {
      try {
        q.stopConnexion();
      } catch (Exception e1) {
        logger.error("", e1);
      }
    }
  }

  /**
   * Parse the STDIN input, then launch the query ans the dump.
   * 
   * @param options
   *          The valid options from STDIN
   * @throws SesameBackendException
   * @throws IllegalArgumentException
   */
  private static void feedInput(OptionSet options)
      throws IllegalArgumentException, SesameBackendException {
    AbstractQuery q = null;

    try {
      q = getSingleLabelledQueryInstance(options);
    } catch (Exception e) {
      // Fail at the connection, need to print the stack
      logger.error("", e);
      try {
        q.stopConnexion();
      } catch (Exception e1) {
        logger.error("", e1);
      }
      throw new SesameBackendException("Connection failed");
    }

    // Set the domain
    String domain = "";
    if (options.has("domain") && options.hasArgument("domain")) {
      domain = options.valueOf("domain").toString();
      q.setGraph(domain);
    }

    // Add file to the repository
    if (options.has(ADD) && options.hasArgument(ADD)) {
      for (Object newFile : options.valuesOf(ADD)) {
        try {
          if (options.has(ADDFORMAT) && options.hasArgument(ADDFORMAT)) {
            if (domain.equals("")) {
              q.addFileToRepository(newFile.toString(),
                  (RDFFormat) options.valueOf(ADDFORMAT));
            } else {
              q.addFileToRepository(newFile.toString(), (RDFFormat) options
                  .valueOf(ADDFORMAT), NTriplesUtil.parseResource("<" + domain
                  + ">", new MemValueFactory()));
            }
          } else {
            // Ntriples format
            if (domain.equals("")) {
              q.addFileToRepository(newFile.toString(), RDFFormat.NTRIPLES);
            } else {
              q.addFileToRepository(newFile.toString(), RDFFormat.NTRIPLES,
                  NTriplesUtil.parseResource("<" + domain + ">",
                      new MemValueFactory()));
            }
          }
        } catch (RDFParseException e) {
          // Invalid output RDF => print the stack
          logger.error("", e);
          throw new SesameBackendException(e);
        } catch (RepositoryException e) {
          logger.warn("Repository not found");
          logger.error("Repository not found", e);
          throw new SesameBackendException(e);
        } catch (IOException e) {
          logger.warn("File not found or not readable");
          logger.error("", e);
          throw new SesameBackendException("File not found or not readable", e);
        }
      }
    }

    try {
      q.stopConnexion();
    } catch (Exception e1) {
      logger.error("", e1);
    }
  }

}
