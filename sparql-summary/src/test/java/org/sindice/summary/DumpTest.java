package org.sindice.summary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;

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
public class DumpTest {
	protected Logger _logger;

	@Before
	public void initLogger() {
		_logger = Logger.getLogger(DumpTest.class);
	}

	@After
	public void clean() {
		File path = new File("/tmp/testUNIT/");
		deleteDirectory(path);
	}

	private Boolean deleteDirectory(File path) {
		Boolean resultat = true;
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					resultat &= deleteDirectory(files[i]);
				} else {
					resultat &= files[i].delete();
				}
			}
		}
		resultat &= path.delete();
		return (resultat);
	}

	@Test
	public void testWriteRDF() {
		Query q = null;
		try {
			Dump d = new Dump();
			q = new QueryNative(d, "/tmp/testUNIT/dumpstore1");
			q.addFileToRepository("src/test/resources/unit_test_no_bc.nt",
			        RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.initDump("/tmp/testUNIT/dumpoutput1");
			q.computeName();
			q.computePredicate();
			q.stopConnexion();
			BufferedReader in = new BufferedReader(new InputStreamReader(
			        new GZIPInputStream(new FileInputStream(
			                "/tmp/testUNIT/dumpoutput1"))));
			String strLine;
			String str = "";

			while ((strLine = in.readLine()) != null) {
				// Print the content on the console
				str += strLine + "\n";
			}
			String ref = "<http://vocab.sindice.net/analytics#ecn2632377909699768787> <http://vocab.sindice.net/analytics#label> \"Human\" .\n"
			        + "<http://vocab.sindice.net/analytics#ecn2632377909699768787> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type6050911082534250786> .\n"
			        + "<http://vocab.sindice.net/analytics#type6050911082534250786> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
			        + "<http://vocab.sindice.net/analytics#type6050911082534250786> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#ec6922471454695406043> <http://vocab.sindice.net/analytics#label> \"Thing\" .\n"
			        + "<http://vocab.sindice.net/analytics#ec6922471454695406043> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#typen8129793483941736198> .\n"
			        + "<http://vocab.sindice.net/analytics#typen8129793483941736198> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
			        + "<http://vocab.sindice.net/analytics#typen8129793483941736198> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#noden3957347606309447496> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ecn2632377909699768787> .\n"
			        + "<http://vocab.sindice.net/analytics#noden3957347606309447496> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec6922471454695406043> .\n"
			        + "<http://vocab.sindice.net/analytics#noden3957347606309447496> <http://vocab.sindice.net/domain> \"sindice.com\" .\n"
			        + "<http://vocab.sindice.net/analytics#noden3957347606309447496> <http://vocab.sindice.net/domain_uri> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
			        + "<http://vocab.sindice.net/analytics#noden3957347606309447496> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#ecn9168467491505453220> <http://vocab.sindice.net/analytics#label> \"Human\" .\n"
			        + "<http://vocab.sindice.net/analytics#ecn9168467491505453220> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#typen383307764049858914> .\n"
			        + "<http://vocab.sindice.net/analytics#typen383307764049858914> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
			        + "<http://vocab.sindice.net/analytics#typen383307764049858914> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ecn9168467491505453220> .\n"
			        + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/domain> \"sindice.com\" .\n"
			        + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/domain_uri> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
			        + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/like> .\n"
			        + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#noden3957347606309447496> .\n"
			        + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node8479274785035382848> .\n"
			        + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
			        + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
			        + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/like> .\n"
			        + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node8479274785035382848> .\n"
			        + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#noden3957347606309447496> .\n"
			        + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
			        + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n";

			assertEquals(ref, str);
			in.close();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong computation");

		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e1) {

				_logger.error(e1.getMessage());
			}
		}
	}

	@Test
	public void testWriteDomainRDF() {
		Query q = null;
		try {
			q = new QueryNative("/tmp/testUNIT/dumpstore4");
			q.setGraph("http://www.testunit.com");
			q.addFileToRepository("src/test/resources/unit_test_no_bc.nt",
			        RDFFormat.N3, NTriplesUtil
			                .parseResource("<http://www.testunit.com>",
			                        new MemValueFactory()));
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.initDump("/tmp/testUNIT/dumpoutput4");
			q.computeName();
			q.computePredicate();
			q.stopConnexion();
			BufferedReader in = new BufferedReader(new InputStreamReader(
			        new GZIPInputStream(new FileInputStream(
			                "/tmp/testUNIT/dumpoutput4"))));
			String strLine;
			String str = "";

			while ((strLine = in.readLine()) != null) {
				// Print the content on the console
				str += strLine + "\n";
			}
			String ref = "<http://vocab.sindice.net/analytics#ecn4879940977782842428> <http://vocab.sindice.net/analytics#label> \"Human\" .\n"
			        + "<http://vocab.sindice.net/analytics#ecn4879940977782842428> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type8962431044451625816> .\n"
			        + "<http://vocab.sindice.net/analytics#type8962431044451625816> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
			        + "<http://vocab.sindice.net/analytics#type8962431044451625816> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#ec3325049966733734037> <http://vocab.sindice.net/analytics#label> \"Thing\" .\n"
			        + "<http://vocab.sindice.net/analytics#ec3325049966733734037> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type3316698421729436947> .\n"
			        + "<http://vocab.sindice.net/analytics#type3316698421729436947> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
			        + "<http://vocab.sindice.net/analytics#type3316698421729436947> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#noden4058114687952750473> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ecn4879940977782842428> .\n"
			        + "<http://vocab.sindice.net/analytics#noden4058114687952750473> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec3325049966733734037> .\n"
			        + "<http://vocab.sindice.net/analytics#noden4058114687952750473> <http://vocab.sindice.net/domain> \"http://www.testunit.com\" .\n"
			        + "<http://vocab.sindice.net/analytics#noden4058114687952750473> <http://vocab.sindice.net/domain_uri> <http://www.testunit.com> .\n"
			        + "<http://vocab.sindice.net/analytics#noden4058114687952750473> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#ec3364966105920994942> <http://vocab.sindice.net/analytics#label> \"Human\" .\n"
			        + "<http://vocab.sindice.net/analytics#ec3364966105920994942> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#typen2234149760394822962> .\n"
			        + "<http://vocab.sindice.net/analytics#typen2234149760394822962> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
			        + "<http://vocab.sindice.net/analytics#typen2234149760394822962> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#node6879956730694495624> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec3364966105920994942> .\n"
			        + "<http://vocab.sindice.net/analytics#node6879956730694495624> <http://vocab.sindice.net/domain> \"http://www.testunit.com\" .\n"
			        + "<http://vocab.sindice.net/analytics#node6879956730694495624> <http://vocab.sindice.net/domain_uri> <http://www.testunit.com> .\n"
			        + "<http://vocab.sindice.net/analytics#node6879956730694495624> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/like> .\n"
			        + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#noden4058114687952750473> .\n"
			        + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node6879956730694495624> .\n"
			        + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
			        + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#publishedIn> <http://www.testunit.com> .\n"
			        + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/like> .\n"
			        + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node6879956730694495624> .\n"
			        + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#noden4058114687952750473> .\n"
			        + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
			        + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#publishedIn> <http://www.testunit.com> .\n";

			assertEquals(ref, str);
			in.close();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong computation");

		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e1) {

				_logger.error(e1.getMessage());
			}
		}
	}

	@Test
	public void testRDFEncode() {
		Query q = null;
		try {
			Dump d = new Dump();
			q = new QueryNative(d, "/tmp/testUNIT/dumpstore4");
			q.addFileToRepository("src/test/resources/unit_test_encode.nt",
			        RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.initDump("/tmp/testUNIT/dumpoutput4");
			q.computeName();
			q.computePredicate();
			q.stopConnexion();
			BufferedReader in = new BufferedReader(new InputStreamReader(
			        new GZIPInputStream(new FileInputStream(
			                "/tmp/testUNIT/dumpoutput4"))));
			String strLine;
			String str = "";

			while ((strLine = in.readLine()) != null) {
				// Print the content on the console
				str += strLine + "\n";
			}
			String ref = "<http://vocab.sindice.net/analytics#ecn425465284613948583> <http://vocab.sindice.net/analytics#label> \"test complexe\" .\n"
			        + "<http://vocab.sindice.net/analytics#ecn425465284613948583> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type8420808726175792342> .\n"
			        + "<http://vocab.sindice.net/analytics#type8420808726175792342> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
			        + "<http://vocab.sindice.net/analytics#type8420808726175792342> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#noden42961954810497749> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ecn425465284613948583> .\n"
			        + "<http://vocab.sindice.net/analytics#noden42961954810497749> <http://vocab.sindice.net/domain> \"sindice.com\" .\n"
			        + "<http://vocab.sindice.net/analytics#noden42961954810497749> <http://vocab.sindice.net/domain_uri> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
			        + "<http://vocab.sindice.net/analytics#noden42961954810497749> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n";

			assertEquals(ref, str);
			in.close();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong computation");

		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e1) {
				_logger.error(e1.getMessage());
			}
		}
	}

	@Test
	public void testRDFWithMultipleDomain() {
		Query q = null;
		try {
			Dump d = new Dump();
			q = new QueryNative(d, "/tmp/testUNIT/dumpstore2");
			q.addFileToRepository(
			        "src/test/resources/unit_test_multidomain.nt",
			        RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");

		}

		try {
			q.initDump("/tmp/testUNIT/dumpoutput2");
			q.computeName();
			q.computePredicate();
			q.stopConnexion();
			BufferedReader in = new BufferedReader(new InputStreamReader(
			        new GZIPInputStream(new FileInputStream(
			                "/tmp/testUNIT/dumpoutput2"))));
			String strLine;
			String str = "";

			while ((strLine = in.readLine()) != null) {
				// Print the content on the console
				str += strLine + "\n";
			}
			String ref = "<http://vocab.sindice.net/analytics#ec5508683872620990028> <http://vocab.sindice.net/analytics#label> \"Human\" .\n"
			        + "<http://vocab.sindice.net/analytics#ec5508683872620990028> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type7591235237956244434> .\n"
			        + "<http://vocab.sindice.net/analytics#type7591235237956244434> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
			        + "<http://vocab.sindice.net/analytics#type7591235237956244434> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#ec1788964349007812996> <http://vocab.sindice.net/analytics#label> \"Thing\" .\n"
			        + "<http://vocab.sindice.net/analytics#ec1788964349007812996> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#typen400740466730078072> .\n"
			        + "<http://vocab.sindice.net/analytics#typen400740466730078072> <http://vocab.sindice.net/analytics#label> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> .\n"
			        + "<http://vocab.sindice.net/analytics#typen400740466730078072> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#noden3957347606309447496> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec5508683872620990028> .\n"
			        + "<http://vocab.sindice.net/analytics#noden3957347606309447496> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec1788964349007812996> .\n"
			        + "<http://vocab.sindice.net/analytics#noden3957347606309447496> <http://vocab.sindice.net/domain> \"sindice.com\" .\n"
			        + "<http://vocab.sindice.net/analytics#noden3957347606309447496> <http://vocab.sindice.net/domain_uri> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
			        + "<http://vocab.sindice.net/analytics#noden3957347606309447496> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#ec6913158645553928482> <http://vocab.sindice.net/analytics#label> \"Thing\" .\n"
			        + "<http://vocab.sindice.net/analytics#ec6913158645553928482> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type8898944007109205443> .\n"
			        + "<http://vocab.sindice.net/analytics#type8898944007109205443> <http://vocab.sindice.net/analytics#label> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> .\n"
			        + "<http://vocab.sindice.net/analytics#type8898944007109205443> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#ec6913158645553928482> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type5405188375134122674> .\n"
			        + "<http://vocab.sindice.net/analytics#type5405188375134122674> <http://vocab.sindice.net/analytics#label> <http://ogp.me/ns#type> .\n"
			        + "<http://vocab.sindice.net/analytics#type5405188375134122674> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#noden6477249737249481171> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec6913158645553928482> .\n"
			        + "<http://vocab.sindice.net/analytics#noden6477249737249481171> <http://vocab.sindice.net/domain> \"sindice.com\" .\n"
			        + "<http://vocab.sindice.net/analytics#noden6477249737249481171> <http://vocab.sindice.net/domain_uri> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
			        + "<http://vocab.sindice.net/analytics#noden6477249737249481171> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#label> <http://purl.org/dc/elements/1.1/like> .\n"
			        + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#noden6477249737249481171> .\n"
			        + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#noden3957347606309447496> .\n"
			        + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
			        + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
			        + "<http://vocab.sindice.net/analytics#edge7415651533969219553> <http://vocab.sindice.net/analytics#label> <http://ogp.me/ns#like> .\n"
			        + "<http://vocab.sindice.net/analytics#edge7415651533969219553> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
			        + "<http://vocab.sindice.net/analytics#edge7415651533969219553> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#noden3957347606309447496> .\n"
			        + "<http://vocab.sindice.net/analytics#edge7415651533969219553> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#noden6477249737249481171> .\n"
			        + "<http://vocab.sindice.net/analytics#edge7415651533969219553> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
			        + "<http://vocab.sindice.net/analytics#edge7415651533969219553> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n";

			assertEquals(ref, str);
			in.close();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong computation");

		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e1) {

				_logger.error(e1.getMessage());
			}
		}
	}

}
