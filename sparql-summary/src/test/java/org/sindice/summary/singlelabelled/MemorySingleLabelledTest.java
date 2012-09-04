package org.sindice.summary.singlelabelled;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.summary.AbstractQuery;
import org.sindice.summary.Dump;
import org.sindice.summary.DumpString;

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
public class MemorySingleLabelledTest {
  protected Logger _logger;

  @Before
  public void initLogger() {
    _logger = Logger.getLogger(MemorySingleLabelledTest.class);
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
  public void testQueryMemory() {
    AbstractQuery q = null;
    try {
      q = new MemorySingleLabelledQuery(new DumpString(),
          "/tmp/testUNIT/memorystoresingle");
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
  public void testAddFileToRepository() {
    AbstractQuery q = null;
    try {
      DumpString d = new DumpString();
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle5");
      try {
        q.addFileToRepository("src/test/resources/unit_test_name.nt",
            RDFFormat.N3);
        q.addFileToRepository("src/test/resources/unit_test_pred.nt",
            RDFFormat.N3);
      } catch (Exception e) {
        _logger.error(e.getMessage());
        fail("Cannot add files.");
      }
      q.computeName();
      String ref = "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"double\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"Human\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
      assertEquals(ref, d.getResult());
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle25");
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
      fail("Cannot compute the query.");
    }

    try {
      String ref = "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"double\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle6");
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
      String ref = "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"double\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle8");
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
      String ref = "http://opengraphprotocol.org/schema/firstName\t\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Human\"\"\n"
          + "http://opengraphprotocol.org/schema/think_at\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Animal\"\"\n"
          + "http://opengraphprotocol.org/schema/firstName\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"type\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"type\"\"\tNothing\n";

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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle14");
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
      String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d,
          "/tmp/testUNIT/memorystoresingle14a");
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
      String ref = "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"double\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d,
          "/tmp/testUNIT/memorystoresingle14b");
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

      String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d,
          "/tmp/testUNIT/memorystoresingle14c");
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
      String ref = "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"double\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle15");
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
      String ref = "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"double\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle16");
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
      String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\tNothing\tNothing\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle16");
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
      String ref = "http://opengraphprotocol.org/schema/firstName\t\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Human\"\"\n"
          + "http://opengraphprotocol.org/schema/think_at\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Animal\"\"\n"
          + "http://opengraphprotocol.org/schema/firstName\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"type\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"type\"\"\tNothing\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d,
          "/tmp/testUNIT/memorystoresingle16a");
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
      String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\tNothing\tNothing\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d,
          "/tmp/testUNIT/memorystoresingle16b");
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
      String ref = "http://opengraphprotocol.org/schema/firstName\t\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Human\"\"\n"
          + "http://opengraphprotocol.org/schema/think_at\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Animal\"\"\n"
          + "http://opengraphprotocol.org/schema/firstName\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"type\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"type\"\"\tNothing\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d,
          "/tmp/testUNIT/memorystoresingle17c");
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
      String ref = "http://opengraphprotocol.org/schema/firstName\t\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Human\"\"\n"
          + "http://opengraphprotocol.org/schema/think_at\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Animal\"\"\n"
          + "http://opengraphprotocol.org/schema/firstName\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"type\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"type\"\"\tNothing\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle18");
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
      String ref = "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"double\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle19");
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
      String ref = "http://opengraphprotocol.org/schema/firstName\t\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Human\"\"\n"
          + "http://opengraphprotocol.org/schema/think_at\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Animal\"\"\n"
          + "http://opengraphprotocol.org/schema/firstName\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"type\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"type\"\"\tNothing\n";

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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle20");
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
      String ref = "\"{\"Thing\",1}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"Human\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
          + "\"{\"Thing\",5}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle21");
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
      String ref = "http://ogp.me/ns#type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\"\"\tNothing\n"
          + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\"\"\tNothing\n"
          + "http://purl.org/dc/elements/1.1/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\"\"\t\"\"Human\"\"\n"
          + "http://purl.org/dc/elements/1.1/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\"\"\t\"\"Thing\"\"\n"
          + "http://ogp.me/ns#like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Thing\"\"\n"
          + "http://ogp.me/ns#like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\"\"\t\"\"Thing\"\"\n"
          + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\"\"\tNothing\n"
          + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n";

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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle22");
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
      String ref = "\"{\"Thing\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
      assertEquals(ref, d.getResult());
    } catch (Exception e) {
      _logger.error(d.getResult());
      _logger.error(e.getMessage());
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle23");
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
      String ref = "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/link\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\"\"\tNothing\n";
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
  public void testNameMultipleDomainOneCA() {
    String[] type = { "http://opengraphprotocol.org/schema/type" };
    AnalyticsClassAttributes.initClassAttributes(type);
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle25");
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
      String ref = "\"{\"Human\",0}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle26");
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
      String ref = "http://ogp.me/ns#like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n"
          + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\tNothing\n";

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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle27");
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
      String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
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
    AbstractQuery q = null;
    DumpString d = new DumpString();
    try {
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle28");
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
      String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\tNothing\tNothing\n";

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
  public void testStopConnexion() {
    AbstractQuery q;
    try {
      Dump d = new DumpString();
      q = new MemorySingleLabelledQuery(d, "/tmp/testUNIT/memorystoresingle24");
      q.stopConnexion();
    } catch (Exception e) {
      _logger.error(e.getMessage());
      fail("Cannot close the connection");
    }
  }

}
