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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.sindice.core.analytics.commons.util.Hash;
import org.sindice.core.analytics.commons.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 */
public class Dump {

  protected static final Logger _logger      = LoggerFactory.getLogger(Dump.class);

  private final ValueFactory    vFactory     = new MemValueFactory();
  private BufferedWriter        _output;
  private int                   _nodeCounter = 0;
  private String                _domain      = "";
  private String                _sndDomain   = "";

  /**
   * Write a triple statement to {@link #_output};
   * @param s the subject {@link Value}
   * @param p the predicate {@link Value}
   * @param o the object {@link Value}
   * @throws IOException if an error occurred while writing the statement
   */
  private void dumpTriple(Value s, Value p, Value o) throws IOException {
    _output.write(NTriplesUtil.toNTriplesString(s));
    _output.write(" ");
    _output.write(NTriplesUtil.toNTriplesString(p));
    _output.write(" ");
    _output.write(NTriplesUtil.toNTriplesString(o));
    _output.write(" .\n");
  }

  /**
   * Create a {@link URI} from the given label
   */
  private Value createURI(String label) {
    return vFactory.createURI(label);
  }

  /**
   * Create a {@link URI} from the given label, after removing dash characters from the label.
   */
  private Value createUriAndReplace(String label) {
    return vFactory.createURI(label.replace('-', 'n'));
  }

  /**
   * Create a {@link Literal} with the given label
   */
  private Value createLiteral(String label) {
    return vFactory.createLiteral(label);
  }

  /**
   * Create a {@link Literal} with the given value, adding the xsd:long datatype tag.
   */
  private Value createLiteral(long l) {
    return vFactory.createLiteral(l);
  }

  /**
   * Parse the type of a node from the function getName(), in order to get the
   * name of the node and the location of this node. Will create the node
   * collection and calculate the hash of the node.
   * 
   * @param nodeType
   *          The type of a node from the function getName()
   * @param domain
   *          The domain of the search
   * @return The hash of the node
   */
  private long dumpRDFNodeCollection(String nodeType, String cardinality, String domain)
  throws IOException {
    Value s = null;
    Value p = null;
    Value o = null;
    StringBuilder idNode = new StringBuilder(0);
    String type;
    String element;
    nodeType = nodeType.substring(1, nodeType.length() - 1);
    Set<String> nodeList = new HashSet<String>();

    // parse
    for (String pair : nodeType.split(" ")) {
      element = pair.substring(1, pair.length() - 3);
      type = pair.substring(pair.length() - 2, pair.length() - 1);

      // an:ecID an:label value .
      s = createUriAndReplace(DataGraphSummaryVocab.DGS_PREFIX + "ec" + Hash.getLong(domain + nodeType + element));
      if (!nodeList.contains(element)) {
        p = createURI(DataGraphSummaryVocab.LABEL);

        try {
          o = NTriplesUtil.parseValue(URLDecoder.decode(element, "UTF-8"), vFactory);
        } catch (Exception e) {
          _logger.info("UTF-8 error", e);
        }
        dumpTriple(s, p, o);
        nodeList.add(element);

      }

      if (idNode.capacity() != 0) {
        idNode.append(" ");
      }
      idNode.append(element);

      // an:ecID an:type an:typeID .
      // still the same s
      p = createURI(DataGraphSummaryVocab.TYPE);
      o = createUriAndReplace(DataGraphSummaryVocab.TYPE + Hash.getLong(domain + nodeType + type + element));

      dumpTriple(s, p, o);

      // an:typeID an:label uri .
      // last o become the new s
      p = createURI(DataGraphSummaryVocab.LABEL);
      int vocabValue = Integer.parseInt(type);

      dumpTriple(o, p, createURI(AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(vocabValue)));
      // an:typeID an:cardinality Cardinality .
      // always the same o as the s
      p = createURI(DataGraphSummaryVocab.CARDINALITY);
      dumpTriple(o, p, createLiteral(Long.valueOf(cardinality)));
    }

    // get the id of this node
    final long hash = Hash.getLong(domain + "\"" + idNode + "\"");

    nodeList.clear();
    for (String pair : nodeType.split(" ")) {
      element = pair.substring(1, pair.length() - 3);

      if (!nodeList.contains(element)) {
        // Create the nodes "nc" and get the ID of this node
        // an:nodeID an:label an:ecID .
        s = createUriAndReplace(DataGraphSummaryVocab.DGS_PREFIX + "node" + hash);
        p = createURI(DataGraphSummaryVocab.LABEL);
        o = createUriAndReplace(DataGraphSummaryVocab.DGS_PREFIX + "ec" + Hash.getLong(domain + nodeType + element));
        dumpTriple(s, p, o);
        nodeList.add(element);
      }
    }

    return hash;
  }

  /**
   * Write a node in the output.
   * 
   * @param BindingSet
   *          Result of the query from computeName()
   */
  public void dumpRDFNode(BindingSet bindingSet)
  throws QueryEvaluationException, IOException {
    Value s = null;
    Value p = null;
    Value o = null;

    // label => new line => new hash
    // make the node
    // Warning, _labelsBag variable modified by this function

    if (bindingSet.hasBinding("label")) {
      // label
      long hash = dumpRDFNodeCollection(bindingSet.getValue("label").toString(),
        bindingSet.getValue("cardinality").stringValue(), _sndDomain);
      // create the id of the node
      s = createUriAndReplace(DataGraphSummaryVocab.DGS_PREFIX + "node" + hash);

      // domain URI
      p = createURI(DataGraphSummaryVocab.DOMAIN_URI);
      if (_domain.equals("sindice.com")) {
        o = createURI(DataGraphSummaryVocab.DOMAIN_URI_PREFIX.toString() + _domain);
      } else {
        o = createURI(_domain);
      }
      dumpTriple(s, p, o);

      // cardinality
      p = createURI(DataGraphSummaryVocab.CARDINALITY);
      o = createLiteral(Long.valueOf(bindingSet.getValue("cardinality").stringValue()));

      dumpTriple(s, p, o);
    } else {
      _logger.info("No result");
    }

  }

  /**
   * Write an edge in the output..
   * 
   * @param bindingSet
   *          Result of the query from computePredicate()
   */
  public void dumpRDFPred(BindingSet bindingSet)
  throws QueryEvaluationException, IOException {
    if (bindingSet.hasBinding("label")) {
      Random randomGenerator = new Random();
      Value s = null;
      Value p = null;
      Value o = null;

      if (!AnalyticsClassAttributes.isClass(bindingSet.getValue("label").toString())) {
        // label => new line => new hash

        s = createUriAndReplace(DataGraphSummaryVocab.DGS_PREFIX + "edge" + Hash.getLong(_domain + _nodeCounter
          + bindingSet.getValue("label").toString()));
        ++_nodeCounter;
        p = createURI(DataGraphSummaryVocab.LABEL);
        o = createURI(bindingSet.getValue("label").stringValue());
        dumpTriple(s, p, o);

        p = createURI(DataGraphSummaryVocab.CARDINALITY);
        o = createLiteral(Long.valueOf(bindingSet.getValue("cardinality").stringValue()));
        dumpTriple(s, p, o);

        p = createURI(DataGraphSummaryVocab.EDGE_SOURCE);
        o = createUriAndReplace(DataGraphSummaryVocab.DGS_PREFIX + "node" + Hash.getLong(_sndDomain +
          bindingSet.getValue("source").toString()));
        dumpTriple(s, p, o);

        // target => can be a blank collection or not
        if ((bindingSet.getValue("target") == null)
            || (bindingSet.getValue("target").toString().equals("\"\""))) {
          // blank collection
          p = createURI(DataGraphSummaryVocab.EDGE_TARGET);
          o = createURI(DataGraphSummaryVocab.DGS_PREFIX + "bc" + randomGenerator.nextInt());
        } else {
          p = createURI(DataGraphSummaryVocab.EDGE_TARGET);
          o = createUriAndReplace(DataGraphSummaryVocab.DGS_PREFIX + "node" + Hash.getLong(_sndDomain +
            bindingSet.getValue("target").toString()));
        }
        dumpTriple(s, p, o);

        // Dummy node
        o = createLiteral(DataGraphSummaryVocab.BLANK_NODE_COLLECTION);
        dumpTriple(s, p, o);

        // published In
        p = createURI(DataGraphSummaryVocab.EDGE_PUBLISHED_IN);
        if (_domain.equals("sindice.com")) {
          o = createURI(DataGraphSummaryVocab.DOMAIN_URI_PREFIX + _domain);
        } else {
          o = createURI(_domain);
        }
        dumpTriple(s, p, o);
      }
    } else {
      _logger.info("No result");
    }

  }

  /**
   * Open a file in order to create a RDF output.
   * 
   * @param outputFile
   *          The file output.
   * @param domain
   *          The domain of the query.
   */
  public void openRDF(String outputFile, String domain) {
    try {
      // Create ancestor directory tree
      final File parent = new File(outputFile).getParentFile();
      if (parent != null) {
        parent.mkdirs();
      }
      // Create file
      _output = new BufferedWriter(new OutputStreamWriter(
          new GZIPOutputStream(new FileOutputStream(outputFile))));
      _domain = domain;
      if (domain.equals("sindice.com")) {
        _sndDomain = domain;
      } else {
        _sndDomain = URIUtil.getSndDomainFromUrl(domain);
      }
      _nodeCounter = 0;

    } catch (Exception e) {// Catch exception if any
      _logger.debug("", e);
    }
  }

  /**
   * close the RDF file.
   */
  public void closeRDF() {
    try {
      if (_output != null) {
        _output.close();
      }
    } catch (Exception e) {// Catch exception if any
      _logger.debug("", e);
    }
  }

}
