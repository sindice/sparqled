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
package org.sindice.core.sesame.backend.testHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParseLocationListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

/**
 * 
 */
public class SesameNxParser
implements RDFParser {

  public static final RDFFormat nquadsFormat = new RDFFormat("N-Quads", "text/plain", Charset.forName("US-ASCII"), "nq", false, true);
  private final ValueFactory    valueFactory = new MemValueFactory();
  private RDFHandler            handler;

  @Override
  public ParserConfig getParserConfig() {
    return new ParserConfig();
  }

  @Override
  public RDFFormat getRDFFormat() {
    return nquadsFormat;
  }

  @Override
  public void parse(InputStream in, String baseURI)
  throws IOException, RDFParseException, RDFHandlerException {
    parse(new InputStreamReader(in), baseURI);
  }

  @Override
  public void parse(Reader reader, String baseURI)
  throws IOException, RDFParseException, RDFHandlerException {
    final NxParser nx = new NxParser(reader);

    handler.startRDF();
    try {
      while (nx.hasNext()) {
        final Node[] nodes = nx.next();
        if (nodes.length != 4) {
          throw new IllegalStateException("Not parsing Quads!");
        }
        final Resource s = NTriplesUtil.parseResource(nodes[0].toN3(), valueFactory);
        final URI p = NTriplesUtil.parseURI(nodes[1].toN3(), valueFactory);
        final Value o = NTriplesUtil.parseValue(nodes[2].toN3(), valueFactory);
        final Resource c = NTriplesUtil.parseResource(nodes[3].toN3(), valueFactory);
        final Statement st = valueFactory.createStatement(s, p, o, c);
        handler.handleStatement(st);
      }
    } finally {
      handler.endRDF();
    }
  }

  @Override
  public void setDatatypeHandling(DatatypeHandling datatypeHandling) {
  }

  @Override
  public void setParseErrorListener(ParseErrorListener el) {
  }

  @Override
  public void setParseLocationListener(ParseLocationListener ll) {
  }

  @Override
  public void setParserConfig(ParserConfig config) {
  }

  @Override
  public void setPreserveBNodeIDs(boolean preserveBNodeIDs) {
  }

  @Override
  public void setRDFHandler(RDFHandler handler) {
    this.handler = handler;
  }

  @Override
  public void setStopAtFirstError(boolean stopAtFirstError) {
  }

  @Override
  public void setValueFactory(ValueFactory valueFactory) {
  }

  @Override
  public void setVerifyData(boolean verifyData) {
  }

}
