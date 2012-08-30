/**
 * @project Graph Summary SPARQL
 * @author Pierre Bailly <pierre.bailly@deri.org>
 * @copyright Copyright (C) 2012, All rights reserved.
 */

package org.sindice.summary;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.semanticweb.yars.nx.Resource;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.AnalyticsVocab;
import org.sindice.core.analytics.commons.util.Hash;
import org.sindice.core.analytics.commons.util.URIUtil;

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
public class Dump {
	private BufferedWriter _output;
	private int _nodeCounter = 0;
	private String _domain = "";
	private String _sndDomain = "";
	protected Logger _logger;

	public Dump() {
		_logger = Logger.getLogger(Dump.class);
	}

	private void dumpTriple(String s, String p, String o) throws IOException {
		_output.write(s);
		_output.write(" ");
		_output.write(p);
		_output.write(" ");
		_output.write(o);
		_output.write(" .\n");
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
	 * @throws IOException
	 */
	private long dumpRDFNodeCollection(String nodeType, String cardinality,
	    String domain) throws IOException {
		Resource s = null;
		Resource p = null;
		Resource o = null;
		StringBuilder idNode = new StringBuilder(0);
		String type;
		String element;
		nodeType = nodeType.substring(1, nodeType.length() - 1);
		HashSet<String> nodeList = new HashSet<String>();
		Logger logger = Logger.getLogger("org.sindice.summary.dump");

		// parse
		for (String pair : nodeType.split(" ")) {
			element = pair.substring(1, pair.length() - 3);
			type = pair.substring(pair.length() - 2, pair.length() - 1);

			// an:ecID an:label value .
			s = new Resource(
			    (AnalyticsVocab.ANALYTICS_PREFIX + "ec" + Hash.getLong(domain
			        + nodeType + element)).replace('-', 'n'), false);
			if (!nodeList.contains(element)) {
				p = new Resource(AnalyticsVocab.LABEL.toString(), false);

				try {
					o = new Resource((URLDecoder.decode(element, "UTF-8")), true);
				} catch (Exception e) {
					logger.info("UTF-8 error");
					logger.info(e.toString());
				}
				dumpTriple(s.toN3(), p.toN3(), o.toN3());
				nodeList.add(element);

			}

			if (idNode.capacity() != 0) {
				idNode.append(" ");
			}
			idNode.append(element);

			// an:ecID an:type an:typeID .
			// still the same s
			p = new Resource(AnalyticsVocab.TYPE.toString(), false);
			o = new Resource((AnalyticsVocab.TYPE.toString() + Hash.getLong(domain
			    + nodeType + type + element)).replace('-', 'n'), false);

			dumpTriple(s.toN3(), p.toN3(), o.toN3());

			// an:typeID an:label uri .
			// last o become the new s
			p = new Resource(AnalyticsVocab.LABEL.toString(), false);
			int vocabValue = Integer.parseInt(type);

			dumpTriple(o.toN3(), p.toN3(), "<"
			    + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(vocabValue) + ">");
			// an:typeID an:cardinality Cardinality .
			// always the same o as the s
			p = new Resource(AnalyticsVocab.CARDINALITY.toString(), false);
			dumpTriple(o.toN3(), p.toN3(), cardinality);
		}

		// get the id of this node
		final long hash = Hash.getLong(domain + "\"" + idNode + "\"");

		nodeList.clear();
		for (String pair : nodeType.split(" ")) {
			element = pair.substring(1, pair.length() - 3);

			if (!nodeList.contains(element)) {
				// Create the nodes "nc" and get the ID of this node
				// an:nodeID an:label an:ecID .
				s = new Resource(
				    (AnalyticsVocab.ANALYTICS_PREFIX.toString() + "node" + hash).replace(
				        '-', 'n'), false);
				p = new Resource(AnalyticsVocab.LABEL.toString(), false);
				o = new Resource(
				    (AnalyticsVocab.ANALYTICS_PREFIX.toString() + "ec" + Hash.getLong(domain
				        + nodeType + element)).replace('-', 'n'), false);
				dumpTriple(s.toN3(), p.toN3(), o.toN3());
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
	 * @throws QueryEvaluationException
	 * @throws IOException
	 */
	public void dumpRDFNode(BindingSet bindingSet)
	    throws QueryEvaluationException, IOException {
		Resource s = null;
		Resource p = null;
		Resource o = null;

		// label => new line => new hash
		// make the node
		// Warning, _labelsBag variable modified by this function

		if (bindingSet.hasBinding("label")) {
			// label
			long hash = dumpRDFNodeCollection(bindingSet.getValue("label")
			    .toString(), bindingSet.getValue("cardinality").toString(),
			    _sndDomain);
			// create the id of the node
			s = new Resource(
			    (AnalyticsVocab.ANALYTICS_PREFIX.toString() + "node" + hash).replace(
			        '-', 'n'), false);

			// domain: AnalyticsVocab.DOMAIN_NAME.toString()
			p = new Resource(AnalyticsVocab.DOMAIN_NAME.toString(), false);
			if (_domain.startsWith("\"") && _domain.endsWith("\"")) {
				o = new Resource(_domain, true);
			} else {
				o = new Resource("\"" + _domain + "\"", true);
			}

			dumpTriple(s.toN3(), p.toN3(), o.toN3());

			// domain URI
			p = new Resource(AnalyticsVocab.DOMAIN_URI.toString(), false);
			if (_domain.equals("sindice.com")) {
				o = new Resource(
				    AnalyticsVocab.DOMAIN_URI_PREFIX.toString() + _domain, false);
			} else {
				o = new Resource(_domain, false);
			}
			dumpTriple(s.toN3(), p.toN3(), o.toN3());

			// cardinality
			p = new Resource(AnalyticsVocab.CARDINALITY.toString(), false);
			o = new Resource(bindingSet.getValue("cardinality").toString(), true);

			dumpTriple(s.toN3(), p.toN3(), o.toN3());
		} else {
			_logger.info("No result");
		}

	}

	/**
	 * Write an edge in the output..
	 * 
	 * @param bindingSet
	 *          Result of the query from computePredicate()
	 * @throws QueryEvaluationException
	 * @throws IOException
	 */
	public void dumpRDFPred(BindingSet bindingSet)
	    throws QueryEvaluationException, IOException {
		if (bindingSet.hasBinding("label")) {
			Random randomGenerator = new Random();
			Resource s = null;
			Resource p = null;
			Resource o = null;

			if (!AnalyticsClassAttributes.isClass(bindingSet.getValue("label")
			    .toString())) {
				// label => new line => new hash

				s = new Resource(
				    (AnalyticsVocab.ANALYTICS_PREFIX.toString() + "edge" + Hash.getLong(_domain
				        + _nodeCounter + bindingSet.getValue("label").toString())).replace(
				        '-', 'n'), false);
				++_nodeCounter;
				p = new Resource(AnalyticsVocab.LABEL.toString(), false);
				o = new Resource(bindingSet.getValue("label").toString(), false);
				dumpTriple(s.toN3(), p.toN3(), o.toN3());

				p = new Resource(AnalyticsVocab.CARDINALITY.toString(), false);
				o = new Resource(bindingSet.getValue("cardinality").toString(), true);
				dumpTriple(s.toN3(), p.toN3(), o.toN3());

				p = new Resource(AnalyticsVocab.EDGE_SOURCE.toString(), false);
				o = new Resource(
				    (AnalyticsVocab.ANALYTICS_PREFIX.toString() + "node" + Hash
				        .getLong(_sndDomain + bindingSet.getValue("source").toString()))
				        .replace('-', 'n'),
				    false);
				dumpTriple(s.toN3(), p.toN3(), o.toN3());

				// target => can be a blank collection or not
				if ((bindingSet.getValue("target") == null)
				    || (bindingSet.getValue("target").toString().equals("\"\""))) {
					// blank collection
					p = new Resource(AnalyticsVocab.EDGE_TARGET.toString(), false);
					o = new Resource(
					    (AnalyticsVocab.ANALYTICS_PREFIX.toString() + "bc" + randomGenerator
					        .nextInt()).replace('-', 'n'), false);
				} else {
					p = new Resource(AnalyticsVocab.EDGE_TARGET.toString(), false);
					o = new Resource((AnalyticsVocab.ANALYTICS_PREFIX.toString()
					    + "node" + Hash.getLong(_sndDomain
					    + bindingSet.getValue("target").toString())).replace('-', 'n'),
					    false);
				}
				dumpTriple(s.toN3(), p.toN3(), o.toN3());

				// Dummy node
				o = new Resource("\""
				    + AnalyticsVocab.BLANK_NODE_COLLECTION.toString() + "\"", true);
				dumpTriple(s.toN3(), p.toN3(), o.toN3());

				// published In
				p = new Resource(AnalyticsVocab.EDGE_PUBLISHED_IN.toString(), false);
				if (_domain.equals("sindice.com")) {
					o = new Resource(AnalyticsVocab.DOMAIN_URI_PREFIX.toString()
					    + _domain, false);
				} else {
					o = new Resource(_domain, false);
				}
				dumpTriple(s.toN3(), p.toN3(), o.toN3());
			}
		} else {
			_logger.info("No result");
		}

	}

	/**
	 * Open a file in order to create a RDF ouput.
	 * 
	 * @param outputFile
	 *          The file output.
	 * @param domain
	 *          The domain of the query.
	 */
	public void openRDF(String outputFile, String domain) {
		Logger logger = Logger.getLogger("org.sindice.summary.dump");

		try {
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
			logger.debug("Error: " + e.getMessage());
		}
	}

	/**
	 * close the RDF file.
	 */
	public void closeRDF() {
		try {
			_output.close();
		} catch (Exception e) {// Catch exception if any
			Logger logger = Logger.getLogger("org.sindice.summary.dump");
			logger.debug("Error: " + e.getMessage());
		}
	}
}
