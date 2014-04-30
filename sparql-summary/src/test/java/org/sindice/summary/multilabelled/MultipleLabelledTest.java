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
package org.sindice.summary.multilabelled;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import info.aduna.io.FileUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.summary.Dump;
import org.sindice.summary.DumpString;

/**
 * @author Pierre Bailly <pierre.bailly@deri.org>
 */
@RunWith(Parameterized.class)
public class MultipleLabelledTest {

  private final Constructor<? extends AbstractMultiLabelledQuery> qClass;
  private AbstractMultiLabelledQuery q;
  private final static DumpString d = new DumpString();

  public MultipleLabelledTest(Class<? extends AbstractMultiLabelledQuery> qClass)
  throws Exception {
    this.qClass = qClass.getConstructor(Dump.class, String.class);
  }

  @Parameters
  public static Collection<Object[]> data()
  throws Exception {
    Object[][] data = new Object[][] {
      { MemoryMultiLabelledQuery.class },
      { NativeMultiLabelledQuery.class }
    };
    return Arrays.asList(data);
  }

  @Before
  public void init()
  throws Exception {
    q = qClass.newInstance(d, "/tmp/test/store");
    d.clear();
    String[] type = { "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
        "http://opengraphprotocol.org/schema/type", "http://ogp.me/ns#type",
        "http://opengraph.org/schema/type",
        "http://purl.org/dc/elements/1.1/type",
        "http://dbpedia.org/property/type" };
    AnalyticsClassAttributes.initClassAttributes(type);
  }

  @After
  public void clean()
  throws Exception {
    try {
      q.stopConnexion();
    } finally {
      FileUtil.deleteDir(new File("/tmp/test/"));
    }
  }

  @Test
  public void testMakeGroupConcat() {
    String s = q.makeGroupConcat("?init", "?group");
    String ref = " (GROUP_CONCAT(IF(isURI(?init),\n"
        + "                concat('<', str(?init), '>'),\n"
        + "                concat('\"', ENCODE_FOR_URI(?init), '\"'))) AS ?group)\n";
    assertEquals(ref, s);
  }

  @Test
  public void testAddFileToRepository()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_name.nt", RDFFormat.N3);
    q.addFileToRepository("src/test/resources/unit_test_pred.nt", RDFFormat.N3);
    q.computeName();
    String ref = "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"Human\",3} {\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"double\",3} {\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testAddxml()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test.xml", RDFFormat.RDFXML);
    q.computeName();

    String ref = "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"double\",3} {\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testSetPaginationName()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_name.nt", RDFFormat.N3);
    q.setPagination(1);
    q.computeName();
    String ref = "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"double\",3} {\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testSetPaginationPred()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_pred.nt", RDFFormat.N3);
    q.setPagination(1);
    q.computePredicate();

    String ref = "http://opengraphprotocol.org/schema/firstName\t\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Human\"\"\n"
        + "http://opengraphprotocol.org/schema/think_at\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Animal\"\"\n"
        + "http://opengraphprotocol.org/schema/firstName\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\" \"type\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\" \"type\"\"\t\"\"\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testSetGraphName()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_name.nt", RDFFormat.N3);
    q.setGraph("http://sparql.sindice.org");
    q.computeName();

    String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testCreateAndSetGraph()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_name.nt", RDFFormat.N3, NTriplesUtil
    .parseResource("<http://sparql.sindice.org>", new MemValueFactory()));
    q.setGraph("http://sparql.sindice.org");
    q.computeName();

    String ref = "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"double\",3} {\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testCreateAndSetWrongGraphName()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_name.nt", RDFFormat.N3, NTriplesUtil
    .parseResource("<http://sparql.sindice.org>", new MemValueFactory()));
    q.setGraph("http://sparqlINVALID.sindiceINVALID.fr");
    q.computeName();
    String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testCreateGraphAskAllName()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_name.nt",
        RDFFormat.N3, NTriplesUtil.parseResource(
            "<http://sparql.sindice.org>", new MemValueFactory()));
    q.computeName();

    String ref = "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"double\",3} {\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testUnsetGraphName()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_name.nt", RDFFormat.N3);
    q.setGraph("http://sparql.sindice.org");
    q.unsetGraph();
    q.computeName();

    String ref = "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"double\",3} {\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testSetGraphPred()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_pred.nt", RDFFormat.N3);
    q.setGraph("http://sparql.sindice.org");
    q.computePredicate();

    String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\tNothing\tNothing\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testCreateAndSetGraphPred()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_pred.nt", RDFFormat.N3, NTriplesUtil
    .parseResource("<http://sparql.sindice.org>", new MemValueFactory()));
    q.setGraph("http://sparql.sindice.org");
    q.computePredicate();

    String ref = "http://opengraphprotocol.org/schema/firstName\t\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Human\"\"\n"
        + "http://opengraphprotocol.org/schema/think_at\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Animal\"\"\n"
        + "http://opengraphprotocol.org/schema/firstName\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\" \"type\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\" \"type\"\"\t\"\"\n";

    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testCreateAndSetWrongGraphPred()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_pred.nt",
          RDFFormat.N3, NTriplesUtil.parseResource(
              "<http://sparql.sindice.org>", new MemValueFactory()));
    q.setGraph("http://sparql.sindiceINVALID.org");
    q.computePredicate();

    String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\tNothing\tNothing\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testCreateGraphAskAllPred()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_pred.nt",
        RDFFormat.N3, NTriplesUtil.parseResource(
            "<http://sparql.sindice.org>", new MemValueFactory()));
    q.computePredicate();

    String ref = "http://opengraphprotocol.org/schema/firstName\t\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Human\"\"\n"
        + "http://opengraphprotocol.org/schema/think_at\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Animal\"\"\n"
        + "http://opengraphprotocol.org/schema/firstName\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\" \"type\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\" \"type\"\"\t\"\"\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testUnsetGraphPred()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_pred.nt", RDFFormat.N3);
    q.setGraph("http://sparql.sindice.org");
    q.unsetGraph();
    q.computePredicate();

    String ref = "http://opengraphprotocol.org/schema/firstName\t\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
          + "http://opengraphprotocol.org/schema/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Human\"\"\n"
          + "http://opengraphprotocol.org/schema/think_at\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Animal\"\"\n"
          + "http://opengraphprotocol.org/schema/firstName\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
          + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
          + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\" \"type\"\"\t\"\"\n"
          + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\" \"type\"\"\t\"\"\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testGetName()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_name.nt", RDFFormat.N3);
    q.computeName();

    String ref = "\"{\"Animal\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"Thing\",3}\"\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"double\",3} {\"type\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testGetPredicate()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_pred.nt", RDFFormat.N3);
    q.computePredicate();

    String ref = "http://opengraphprotocol.org/schema/firstName\t\"3\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Human\"\"\n"
        + "http://opengraphprotocol.org/schema/think_at\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"Animal\"\"\n"
        + "http://opengraphprotocol.org/schema/firstName\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Animal\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\" \"type\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/test\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"double\" \"type\"\"\t\"\"\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testNameMultipleDomain()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_multidomain.nt", RDFFormat.N3);
    q.computeName();

    String ref = "\"{\"Human\",3} {\"Thing\",5}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n"
        + "\"{\"Thing\",1} {\"Thing\",5}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testPredMultipleDomain()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_multidomain.nt", RDFFormat.N3);
    q.computePredicate();

    String ref = "http://ogp.me/ns#type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\" \"Thing\"\"\t\"\"\n"
        + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\" \"Thing\"\"\t\"\"\n"
        + "http://purl.org/dc/elements/1.1/like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\" \"Thing\"\"\t\"\"Human\" \"Thing\"\"\n"
        + "http://ogp.me/ns#like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\" \"Thing\"\"\t\"\"Thing\" \"Thing\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\" \"Thing\"\"\t\"\"\n"
        + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\" \"Thing\"\"\t\"\"\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testNameBlankNode()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_blank.nt", RDFFormat.N3);
    q.computeName();

    String ref = "\"{\"Thing\",3}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testPredBlankNode()
  throws Exception {
    q.addFileToRepository("src/test/resources/unit_test_blank.nt", RDFFormat.N3);
    q.computePredicate();

    String ref = "http://opengraphprotocol.org/schema/type\t\"2\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/link\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Thing\"\"\t\"\"\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testStopConnexion()
  throws Exception {
    // do nothing
  }

  @Test
  public void testNameMultipleDomainOneCA()
  throws Exception {
    String[] type = { "http://opengraphprotocol.org/schema/type" };
    AnalyticsClassAttributes.initClassAttributes(type);

    q.addFileToRepository("src/test/resources/unit_test_multidomain.nt", RDFFormat.N3);
    q.computeName();

    String ref = "\"{\"Human\",0}\"\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testPredMultipleDomainOneCA()
  throws Exception {
    String[] type = { "http://opengraphprotocol.org/schema/type" };
    AnalyticsClassAttributes.initClassAttributes(type);

    q.addFileToRepository("src/test/resources/unit_test_multidomain.nt", RDFFormat.N3);
    q.computePredicate();

    String ref = "http://ogp.me/ns#like\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
        + "http://opengraphprotocol.org/schema/type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n"
        + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type\t\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>\t\"\"Human\"\"\t\"\"\n";

    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testNameMultipleDomainInvalidCA()
  throws Exception {
    String[] type = { "http://dbpedia.org/property/type" };
    AnalyticsClassAttributes.initClassAttributes(type);

    q.addFileToRepository("src/test/resources/unit_test_multidomain.nt", RDFFormat.N3);
    q.computeName();

    String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

  @Test
  public void testPredMultipleDomainInvalidCA()
  throws Exception {
    String[] type = { "http://dbpedia.org/property/type" };
    AnalyticsClassAttributes.initClassAttributes(type);

    q.addFileToRepository("src/test/resources/unit_test_multidomain.nt", RDFFormat.N3);
    q.computePredicate();

    String ref = "Nothing\t\"0\"^^<http://www.w3.org/2001/XMLSchema#integer>\tNothing\tNothing\n";
    String[] refar = ref.split("\n");
    Arrays.sort(refar);
    assertArrayEquals(refar, d.getResult());
  }

}
