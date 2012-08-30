package org.sindice.summary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;

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
public class QueryNativeTest {
	protected Logger _logger;

	@Before
	public void initLogger() {
		_logger = Logger.getLogger(QueryNativeTest.class);
		String[] type = { "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
		    "http://opengraphprotocol.org/schema/type", "http://ogp.me/ns#type",
		    "http://opengraph.org/schema/type",
		    "http://purl.org/dc/elements/1.1/type",
		    "http://dbpedia.org/property/type" };
		AnalyticsClassAttributes.initClassAttributes(type);
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
	public void testQueryNative() {
		Query q = null;
		try {
			q = new QueryNative(new DumpString(), "/tmp/testUNIT/nativestore");
			try {
				q.stopConnexion();
			} catch (RepositoryException e) {
				// Nothing for this test
			}
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Something wrong append");
		}
	}

	@Test
	public void testMakeGroupConcat() {
		Query q = null;
		try {
			Dump d = new DumpString();
			q = new QueryNative(d, "/tmp/testUNIT/nativestore3");
		} catch (Exception e) {
		}
		String s = q.makeGroupConcat("?init", "?group");
		String ref = " (GROUP_CONCAT(IF(isURI(?init),\n"
		    + "                concat('<', str(?init), '>'),\n"
		    + "                concat('\"', ENCODE_FOR_URI(?init), '\"'))) AS ?group)\n";
		assertEquals(ref, s);
	}

	@Test
	public void testAddFileToRepository() {
		Query q = null;
		try {
			Dump d = new DumpString();
			q = new QueryNative(d, "/tmp/testUNIT/nativestore5");
			try {
				q.addFileToRepository("src/test/resources/unit_test_name.nt",
				    RDFFormat.N3);
				q.addFileToRepository("src/test/resources/unit_test_pred.nt",
				    RDFFormat.N3);
			} catch (Exception e) {
				_logger.error(e.getMessage());
				fail("Cannot add files.");
			}
			q._queriesResults = new Stack<TupleQueryResult>();
			// q.launchQuery("SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 1");
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Something wrong append");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				// Nothing for this test
			}
		}
	}

	@Test
	public void testAddxml() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore25");
			q.addFileToRepository("src/test/resources/unit_test.xml",
			    RDFFormat.RDFXML);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			System.err.println(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "\"{\"Animal\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"Thing\",3}\"	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"double\",3} {\"type\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testSetPaginationName() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore6");
			q.addFileToRepository("src/test/resources/unit_test_name.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		q.setPagination(1);
		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {

			String ref = "\"{\"Animal\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"Thing\",3}\"	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"double\",3} {\"type\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}

	}

	@Test
	public void testSetPaginationPred() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore8");
			q.addFileToRepository("src/test/resources/unit_test_pred.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}
		q.setPagination(1);
		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "http://opengraphprotocol.org/schema/firstName	\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/like	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"Human\"\"\n"
			    + "http://opengraphprotocol.org/schema/think_at	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"Animal\"\"\n"
			    + "http://opengraphprotocol.org/schema/firstName	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/test	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"double\" \"type\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/test	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"double\" \"type\"\"	\"\"\n";

			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testSetGraphName() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore14");
			q.addFileToRepository("src/test/resources/unit_test_name.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		q.setGraph("http://sparql.sindice.org");

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "Nothing	\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testCreateAndSetGraph() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore14a");
			q.addFileToRepository("src/test/resources/unit_test_name.nt",
			    RDFFormat.N3, NTriplesUtil.parseResource(
			        "<http://sparql.sindice.org>", new MemValueFactory()));
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		q.setGraph("http://sparql.sindice.org");

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {

			String ref = "\"{\"Animal\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"Thing\",3}\"	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"double\",3} {\"type\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}

	}

	@Test
	public void testCreateAndSetWrongGraphName() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore14b");
			q.addFileToRepository("src/test/resources/unit_test_name.nt",
			    RDFFormat.N3, NTriplesUtil.parseResource(
			        "<http://sparql.sindice.org>", new MemValueFactory()));
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		q.setGraph("http://sparqlINVALID.sindiceINVALID.fr");

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {

			String ref = "Nothing	\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}

	}

	@Test
	public void testCreateGraphAskAllName() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore14c");
			q.addFileToRepository("src/test/resources/unit_test_name.nt",
			    RDFFormat.N3, NTriplesUtil.parseResource(
			        "<http://sparql.sindice.org>", new MemValueFactory()));
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "\"{\"Animal\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"Thing\",3}\"	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"double\",3} {\"type\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}

	}

	@Test
	public void testUnsetGraphName() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore15");
			q.addFileToRepository("src/test/resources/unit_test_name.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		q.setGraph("http://sparql.sindice.org");
		q.unsetGraph();

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "\"{\"Animal\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"Thing\",3}\"	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"double\",3} {\"type\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testSetGraphPred() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore16");
			q.addFileToRepository("src/test/resources/unit_test_pred.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		q.setGraph("http://sparql.sindice.org");

		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "Nothing	\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\tNothing\tNothing\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testCreateAndSetGraphPred() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore16");
			q.addFileToRepository("src/test/resources/unit_test_pred.nt",
			    RDFFormat.N3, NTriplesUtil.parseResource(
			        "<http://sparql.sindice.org>", new MemValueFactory()));
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		q.setGraph("http://sparql.sindice.org");

		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "http://opengraphprotocol.org/schema/firstName	\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/like	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"Human\"\"\n"
			    + "http://opengraphprotocol.org/schema/think_at	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"Animal\"\"\n"
			    + "http://opengraphprotocol.org/schema/firstName	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/test	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"double\" \"type\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/test	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"double\" \"type\"\"	\"\"\n";

			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testCreateAndSetWrongGraphPred() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore16a");
			q.addFileToRepository("src/test/resources/unit_test_pred.nt",
			    RDFFormat.N3, NTriplesUtil.parseResource(
			        "<http://sparql.sindice.org>", new MemValueFactory()));
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		q.setGraph("http://sparql.sindiceINVALID.org");

		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "Nothing	\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\tNothing\tNothing\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testCreateGraphAskAllPred() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore16b");
			q.addFileToRepository("src/test/resources/unit_test_pred.nt",
			    RDFFormat.N3, NTriplesUtil.parseResource(
			        "<http://sparql.sindice.org>", new MemValueFactory()));
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "http://opengraphprotocol.org/schema/firstName	\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/like	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"Human\"\"\n"
			    + "http://opengraphprotocol.org/schema/think_at	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"Animal\"\"\n"
			    + "http://opengraphprotocol.org/schema/firstName	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/test	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"double\" \"type\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/test	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"double\" \"type\"\"	\"\"\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testUnsetGraphPred() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore17c");
			q.addFileToRepository("src/test/resources/unit_test_pred.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		q.setGraph("http://sparql.sindice.org");
		q.unsetGraph();

		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "http://opengraphprotocol.org/schema/firstName	\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/like	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"Human\"\"\n"
			    + "http://opengraphprotocol.org/schema/think_at	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"Animal\"\"\n"
			    + "http://opengraphprotocol.org/schema/firstName	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/test	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"double\" \"type\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/test	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"double\" \"type\"\"	\"\"\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testGetName() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore18");
			q.addFileToRepository("src/test/resources/unit_test_name.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "\"{\"Animal\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"Thing\",3}\"	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"double\",3} {\"type\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testGetPredicate() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore19");
			q.addFileToRepository("src/test/resources/unit_test_pred.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "http://opengraphprotocol.org/schema/firstName	\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/like	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"Human\"\"\n"
			    + "http://opengraphprotocol.org/schema/think_at	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"Animal\"\"\n"
			    + "http://opengraphprotocol.org/schema/firstName	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/test	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Animal\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"double\" \"type\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/test	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"double\" \"type\"\"	\"\"\n";

			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testNameMultipleDomain() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore20");
			q.addFileToRepository("src/test/resources/unit_test_multidomain.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "\"{\"Human\",3} {\"Thing\",5}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
			    + "\"{\"Thing\",1} {\"Thing\",5}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testPredMultipleDomain() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore21");
			q.addFileToRepository("src/test/resources/unit_test_multidomain.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "http://ogp.me/ns#type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Thing\" \"Thing\"\"	\"\"\n"
			    + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Thing\" \"Thing\"\"	\"\"\n"
			    + "http://purl.org/dc/elements/1.1/like	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Thing\" \"Thing\"\"	\"\"Human\" \"Thing\"\"\n"
			    + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\" \"Thing\"\"	\"\"\n"
			    + "http://ogp.me/ns#like	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\" \"Thing\"\"	\"\"Thing\" \"Thing\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\" \"Thing\"\"	\"\"\n";

			_logger.info(ref);
			_logger.info(d.getResult());
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testNameBlankNode() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore22");
			q.addFileToRepository("src/test/resources/unit_test_blank.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "\"{\"Thing\",3}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testPredBlankNode() {
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore23");
			q.addFileToRepository("src/test/resources/unit_test_blank.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "http://opengraphprotocol.org/schema/type	\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Thing\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/link	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Thing\"\"	\"\"\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testStopConnexion() {
		Query q;
		try {
			Dump d = new DumpString();
			q = new QueryNative(d, "/tmp/testUNIT/nativestore24");
			q.stopConnexion();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot close the connection");
		}
	}

	@Test
	public void testNameMultipleDomainOneCA() {
		String[] type = { "http://opengraphprotocol.org/schema/type" };
		AnalyticsClassAttributes.initClassAttributes(type);
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore25");
			q.addFileToRepository("src/test/resources/unit_test_multidomain.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "\"{\"Human\",0}\"	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testPredMultipleDomainOneCA() {
		String[] type = { "http://opengraphprotocol.org/schema/type" };
		AnalyticsClassAttributes.initClassAttributes(type);
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore26");
			q.addFileToRepository("src/test/resources/unit_test_multidomain.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://ogp.me/ns#like	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n"
			    + "http://opengraphprotocol.org/schema/type	\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>	\"\"Human\"\"	\"\"\n";

			_logger.info(ref);
			_logger.info(d.getResult());
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testNameMultipleDomainInvalidCA() {
		String[] type = { "http://dbpedia.org/property/type" };
		AnalyticsClassAttributes.initClassAttributes(type);
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore27");
			q.addFileToRepository("src/test/resources/unit_test_multidomain.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computeName();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "Nothing	\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}

	@Test
	public void testPredMultipleDomainInvalidCA() {
		String[] type = { "http://dbpedia.org/property/type" };
		AnalyticsClassAttributes.initClassAttributes(type);
		Query q = null;
		DumpString d = new DumpString();
		try {
			q = new QueryNative(d, "/tmp/testUNIT/nativestore28");
			q.addFileToRepository("src/test/resources/unit_test_multidomain.nt",
			    RDFFormat.N3);
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("wrong initialisation");
		}

		try {
			q.computePredicate();
		} catch (Exception e) {
			_logger.error(e.getMessage());
			fail("Cannot compute the query.");
		}

		try {
			String ref = "Nothing	\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\tNothing\tNothing\n";

			_logger.info(ref);
			_logger.info(d.getResult());
			assertEquals(ref, d.getResult());
		} catch (Exception e) {
			_logger.error(e.getMessage());
			_logger.error(d.getResult());
			fail("Cannot parse the query.");
		} finally {
			try {
				q.stopConnexion();
			} catch (Exception e) {
				_logger.error(e.getMessage());
			}
		}
	}
}
