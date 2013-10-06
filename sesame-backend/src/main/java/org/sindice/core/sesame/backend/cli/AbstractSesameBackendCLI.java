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
package org.sindice.core.sesame.backend.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.ValueConverter;

import org.openrdf.model.Resource;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSesameBackendCLI {

  private static final Logger                logger   = LoggerFactory.getLogger(AbstractSesameBackendCLI.class);

  private static final String                HELP     = "help";
  private static final String                TYPE     = "type";
  private static final String                ARGS     = "args";

  private static final String                ADD_RDF  = "add-rdf";
  private static final String                FORMAT   = "format";
  private static final String                CONTEXTS = "contexts";
  private static final String                QUERY    = "query";

  private final OptionParser                 parser;
  private SesameBackend<BindingSet> backend;

  public AbstractSesameBackendCLI() {
    parser = initializeOptionParser();
    initializeOptionParser(parser);
  }

  protected abstract void initializeOptionParser(OptionParser parser);
  protected abstract void execute(final OptionSet options);

  private OptionParser initializeOptionParser() {
    final OptionParser parser = new OptionParser() {
      {
        accepts(HELP, "Shows this help message");
        accepts(TYPE, "The Sesame backend type: " + Arrays.toString(BackendType.values())).withRequiredArg().ofType(BackendType.class);
        accepts(ARGS, "The arguments of the Sesame backend").withOptionalArg().ofType(String.class).withValuesSeparatedBy(',');
        accepts(ADD_RDF, "Add RDF to the repository").withRequiredArg().ofType(File.class);
        accepts(QUERY, "Submit the query to the repository").withRequiredArg().ofType(File.class);
        accepts(FORMAT, "The format of the RDF data").withRequiredArg().ofType(RDFFormat.class).defaultsTo(RDFFormat.NTRIPLES);
        accepts(CONTEXTS, "The list of graph names to add the RDF to").withRequiredArg()
        .withValuesSeparatedBy(',').withValuesConvertedBy(new ValueConverter<Resource>() {

          @Override
          public Resource convert(String value) {
            return NTriplesUtil.parseResource("<" + value + ">", new MemValueFactory());
          }

          @Override
          public Class<Resource> valueType() {
            return Resource.class;
          }

          @Override
          public String valuePattern() {
            return null;
          }

        });
      }
    };
    return parser;
  }

  private OptionSet parseArgs(final String[] args, final OptionParser parser)
  throws SesameBackendException, IOException {
    OptionSet options = null;

    try {
      options = parser.parse(args);
    } catch (final OptionException e) {
      logger.error("", e);
      parser.printHelpOn(System.err);
      System.exit(1);
    }

    if (options.has(HELP)) {
      parser.printHelpOn(System.out);
      System.exit(0);
    }
    if (options.has(TYPE)) {
      BackendType type = (BackendType) options.valueOf(TYPE);
      if (options.has(ARGS)) {
        String[] backendArgs = ((List<String>) options.valuesOf(ARGS)).toArray(new String[0]);
        backend = SesameBackendFactory.getDgsBackend(type, backendArgs);
      } else {
        backend = SesameBackendFactory.getDgsBackend(type);
      }
    } else {
      logger.error("Missing type option");
      parser.printHelpOn(System.out);
      System.exit(1);
    }
    if (options.has(ADD_RDF)) {
      addRDF(options);
    }
    if (options.has(QUERY)) {
      runQuery(options);
    }
    return options;
  }

  private void execute(final String[] args, final OptionParser parser)
  throws IOException, SesameBackendException {
    final OptionSet options = parseArgs(args, parser);

    try {
      this.execute(options);
    } catch (final IllegalArgumentException e) {
      logger.error("", e);
      parser.printHelpOn(System.err);
      System.exit(1);
    }
    System.exit(0);
  }

  private void addRDF(OptionSet options)
  throws SesameBackendException {
    backend.initConnection();
    try {
      if (options.has(CONTEXTS)) {
        final List<Resource> c = (List<Resource>) options.valuesOf(CONTEXTS);
        backend.addToRepository((File) options.valueOf(ADD_RDF), (RDFFormat) options.valueOf(FORMAT), c.toArray(new Resource[c.size()]));
      } else {
        backend.addToRepository((File) options.valueOf(ADD_RDF), (RDFFormat) options.valueOf(FORMAT));
      }
    } finally {
      backend.closeConnection();
    }
  }

  private void runQuery(OptionSet options)
  throws SesameBackendException {
    final StringWriter query = new StringWriter();

    try {
      final File qFile = (File) options.valueOf(QUERY);
      final BufferedReader r = new BufferedReader(new FileReader(qFile));
      String line;

      try {
        while ((line = r.readLine()) != null) {
          query.append(line).append('\n');
        }
      } finally {
        query.close();
        r.close();
      }
    } catch (IOException e) {
      logger.error("Unable to read query file", e);
    }

    backend.initConnection();
    QueryIterator<BindingSet> qit = backend.submit(query.toString());
    try {
      while (qit.hasNext()) {
        BindingSet bs = qit.next();
        if (bs != null) {
          Iterator<Binding> it = bs.iterator();
          while (it.hasNext()) {
            Binding b = it.next();
            logger.info("{}: {}", b.getName(), b.getValue());
          }
          logger.info("\n");
        }
      }
    } finally {
      backend.closeConnection();
    }
  }

  public void run(final String args[])
  throws IOException, SesameBackendException {
    this.execute(args, parser);
  }

}
