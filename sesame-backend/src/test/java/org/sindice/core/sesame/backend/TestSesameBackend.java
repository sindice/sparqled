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
package org.sindice.core.sesame.backend;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.aduna.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;

@RunWith(value = Parameterized.class)
public class TestSesameBackend {

  private static File                     nativeDataDir;
  private static File                     memoryDataDir;
  private SesameBackend                   backend;

  private static final String             ASK_BINDING = "ask";
  private static final String             CD_BINDING  = "triples";

  private final Map<String, List<String>> expected    = new HashMap<String, List<String>>();

  public TestSesameBackend(SesameBackend backend)
  throws Exception {
    this.backend = backend;
    this.backend.initConnection();
  }

  @After
  public void tearDown() throws Exception {
    expected.clear();
    backend.getConnection().clear();
    backend.closeConnection();
  }

  @AfterClass
  public static void destroy() throws IOException {
    FileUtil.deleteDir(nativeDataDir);
    FileUtil.deleteDir(memoryDataDir);
  }

  @Parameters
  public static Collection<Object[]> data() {
    nativeDataDir = new File("/tmp", "sesameBackend" + Math.random());
    nativeDataDir.mkdir();
    memoryDataDir = new File("/tmp/sesameMemBackend" + Math.random());

    Object[][] data = new Object[4][];

    // In Memory Backend
    data[0] = new Object[] { SesameBackendFactory.getDgsBackend(BackendType.MEMORY) };
    // In Memory Backend with data-dir
    data[1] = new Object[] { SesameBackendFactory.getDgsBackend(BackendType.MEMORY, memoryDataDir.getAbsolutePath()) };
    // In Memory Backend with custom result processing
    data[2] = new Object[] { SesameBackendFactory
    .getDgsBackend(BackendType.MEMORY, new QueryResultProcessor<String>() {

      private final StringBuilder sb = new StringBuilder();

      @Override
      public String process(Object bs) {
        sb.setLength(0);
        if (bs instanceof BindingSet) {
          final Iterator<Binding> it = ((BindingSet) bs).iterator();
          while (it.hasNext()) {
            final Binding b = it.next();
            sb.append(b.getName()).append(" = ").append(b.getValue()).append('\n');
          }
        } else if (bs instanceof Boolean) {
          sb.append(ASK_BINDING + " = " + Boolean.toString((Boolean) bs));
        } else if (bs instanceof Statement) {
          sb.append(CD_BINDING + " = " + ((Statement) bs).toString());
        } else {
          fail("Unknown Value class: " + bs);
        }
        return sb.toString();
      }

    }) };
    // Native Backend
    data[3] = new Object[] { SesameBackendFactory.getDgsBackend(BackendType.NATIVE, nativeDataDir.getAbsolutePath()) };
    return Arrays.asList(data);
  }

  @Test(expected = SesameBackendException.class)
  public void testAddWrongFormat() throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.RDFXML);
  }

  @Test(expected = SesameBackendException.class)
  public void testEmptyQuery() throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);
    backend.submit("");
  }

  @Test
  public void testAddTriplesToRepository() throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);
    assertFalse(backend.getConnection().isEmpty());
    assertEquals(5, backend.getConnection().size());
  }

  @Test
  public void testSparqlQuery() throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);

    final String query1 = "SELECT ?o { ?s <http://www.semanlink.net/2001/00/semanlink-schema#tag> ?o }";
    expected.put("o", Arrays.asList(
      "http://www.semanlink.net/tag/tim_berners_lee",
      "http://www.semanlink.net/tag/httprange_14"
    ));
    QueryIterator<?> qit = backend.submit(query1);
    assertFalse(qit.getBindingNames().isEmpty());
    assertArrayEquals(new String[] { "o" }, qit.getBindingNames().toArray(new String[0]));
    evaluateQuery(qit, expected, 1); // an additional iteration to check there are no more results

    final String query2 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o }";
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));
    qit = backend.submit(query2);
    assertFalse(qit.getBindingNames().isEmpty());
    assertArrayEquals(new String[] { "p" }, qit.getBindingNames().toArray(new String[0]));
    evaluateQuery(qit, expected, 1); // an additional iteration to check there are no more results
  }

  @Test
  public void testBindingNames()
  throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);

    final String query1 = "SELECT distinct ?p ?a { ?a ?p ?o }";
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));
    expected.put("a", Arrays.asList(
      "http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html",
      "http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html",
      "http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html",
      "http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html"
    ));
    QueryIterator<?> qit = backend.submit(query1);
    assertFalse(qit.getBindingNames().isEmpty());
    assertArrayEquals(new String[] { "p", "a" }, qit.getBindingNames().toArray(new String[0]));
    evaluateQuery(qit, expected, 1);

    // wildcard query
    final String query2 = "SELECT distinct * { ?a <http://www.semanlink.net/2001/00/semanlink-schema#tag> ?o }";
    expected.put("o", Arrays.asList(
      "http://www.semanlink.net/tag/tim_berners_lee",
      "http://www.semanlink.net/tag/httprange_14"
    ));
    expected.put("a", Arrays.asList(
      "http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html",
      "http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html"
    ));
    qit = backend.submit(query2);
    assertFalse(qit.getBindingNames().isEmpty());
    assertArrayEquals(new String[] { "a", "o" }, qit.getBindingNames()
        .toArray(new String[0]));
    evaluateQuery(qit, expected, 1);
  }

  @Test
  public void testNoPagination() throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);

    // No LIMIT / OFFSET defined
    final String query1 = "SELECT ?o { ?s <http://www.semanlink.net/2001/00/semanlink-schema#tag> ?o }";
    expected.put("o", Arrays.asList(
      "http://www.semanlink.net/tag/tim_berners_lee",
      "http://www.semanlink.net/tag/httprange_14"
    ));
    QueryIterator<?> qit = backend.submit(query1);
    qit.setPagination(0);
    evaluateQuery(qit, expected, 0);
    // LIMIT only
    final String query2 = "SELECT ?o { ?s <http://www.semanlink.net/2001/00/semanlink-schema#tag> ?o } LIMIT 1";
    expected.put("o", Arrays.asList("http://www.semanlink.net/tag/tim_berners_lee"));
    qit = backend.submit(query2);
    qit.setPagination(0);
    evaluateQuery(qit, expected, 0);
    // Offset only
    final String query3 = "SELECT ?o { ?s <http://www.semanlink.net/2001/00/semanlink-schema#tag> ?o } OFFSET 1";
    expected.put("o", Arrays.asList("http://www.semanlink.net/tag/httprange_14"));
    qit = backend.submit(query3);
    qit.setPagination(0);
    evaluateQuery(qit, expected, 0);
    // Offset and Limit
    final String query4 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o } offset 1 limit 2";
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate"
    ));
    qit = backend.submit(query4);
    qit.setPagination(0);
    evaluateQuery(qit, expected, 0);
  }

  @Test
  public void testWithDefaultPagination() throws Exception {
    final Resource context = NTriplesUtil.parseResource("<http://acme.org/>", new MemValueFactory());
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES, context);

    // with FROM
    final String query1 = "SELECT distinct ?p FROM <http://acme.org/> { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o }";
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));
    QueryIterator<?> qit = backend.submit(query1);
    evaluateQuery(qit, expected, 1);

    // limit inferior to the default pagination
    final String query2 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o } limit 3";
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate"
    ));
    qit = backend.submit(query2);
    evaluateQuery(qit, expected, 1);

    // limit inferior to the default pagination and offset != 0
    final String query3 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o } limit 3 offset 1";
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));
    qit = backend.submit(query3);
    evaluateQuery(qit, expected, 1);

    final String query4 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o } limit 1 offset 2";
    expected.put("p", Arrays.asList("http://www.semanlink.net/2001/00/semanlink-schema#creationDate"));
    qit = backend.submit(query4);
    evaluateQuery(qit, expected, 1);

    final String query5 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o } limit 1 offset 1";
    expected.put("p", Arrays.asList("http://www.semanlink.net/2001/00/semanlink-schema#comment"));
    qit = backend.submit(query5);
    evaluateQuery(qit, expected, 1);
  }

  @Test
  public void testWithPaginationAndInQueryLimitOffset() throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);

    // Offset only
    final String query1 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o } OFFSET 1";
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));
    QueryIterator<?> qit = backend.submit(query1);
    qit.setPagination(1);
    evaluateQuery(qit, expected, 4);
    // Limit only
    final String query2 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o } LIMIT 3";
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate"
    ));
    qit = backend.submit(query2);
    qit.setPagination(1);
    evaluateQuery(qit, expected, 3);
    // Limit and Offset
    final String query3 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o } Offset 1 LIMIT 2";
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate"
    ));
    qit = backend.submit(query3);
    qit.setPagination(1);
    evaluateQuery(qit, expected, 2);
  }

  @Test
  public void testASKQuery() throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);

    // has pattern
    final String query1 = "ASK { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o }";
    expected.put(ASK_BINDING, Arrays.asList("true"));
    QueryIterator<?> qit = backend.submit(query1);
    qit.setPagination(1); // the pagination is disabled by default for ASKQuery
    assertTrue(qit.getBindingNames().isEmpty());
    evaluateQuery(qit, expected, 0);

    // Has not pattern
    final String query2 = "ASK { ?s <http://www.semanlink.net/2001/00/semanlink-schema#Tag> ?o }";
    expected.put(ASK_BINDING, Arrays.asList("false"));
    qit = backend.submit(query2);
    evaluateQuery(qit, expected, 0);
  }

  @Test(expected = SesameBackendException.class)
  public void testASKQueryWithOffset() throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);

    // has pattern
    final String query1 = "ASK { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o } Offset 10";
    QueryIterator<?> qit = backend.submit(query1);
    evaluateQuery(qit, expected, 0);
  }

  @Test
  public void testDescribeQuery() throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);

    // has pattern
    final String query1 = "DESCRIBE * { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o } Limit 1";
    expected.put(CD_BINDING, Arrays.asList(
      "(http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html, "
      + "http://www.semanlink.net/2001/00/semanlink-schema#tag, "
      + "http://www.semanlink.net/tag/tim_berners_lee)"
    ));
    QueryIterator<?> qit = backend.submit(query1);
    qit.setPagination(0);
    evaluateQuery(qit, expected, 0);
  }

  @Test
  public void testConstructQuery() throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);

    final String query1 = "Construct { <http://acme.org/tbl> <http://acme.org/tbl/pred> ?p .} "
        + "{ <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o }";
    expected.put(CD_BINDING, Arrays.asList(
      "(http://acme.org/tbl, http://acme.org/tbl/pred, http://www.semanlink.net/2001/00/semanlink-schema#tag)",
      "(http://acme.org/tbl, http://acme.org/tbl/pred, http://www.semanlink.net/2001/00/semanlink-schema#comment)",
      "(http://acme.org/tbl, http://acme.org/tbl/pred, http://www.semanlink.net/2001/00/semanlink-schema#creationDate)",
      "(http://acme.org/tbl, http://acme.org/tbl/pred, http://purl.org/dc/elements/1.1/title)"
    ));
    QueryIterator<?> qit = backend.submit(query1);
    qit.setPagination(0);
    evaluateQuery(qit, expected, 0);
  }

  @Test
  public void testPagination()
  throws Exception {
    backend.addToRepository(new File("./src/test/resources/tbl.nt.gz"), RDFFormat.NTRIPLES);

    // pagination default
    final String query1 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o }";
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));

    QueryIterator<?> qit = backend.submit(query1);
    evaluateQuery(qit, expected, 1); // 1 iteration, stop because we don't have enough results

    // pagination = 1
    final String query2 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o }";
    QueryIterator<?> qit2 = backend.submit(query2);
    qit2.setPagination(1);
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));
    evaluateQuery(qit2, expected, 5); // 5 iterations to get an empty result during the 5th one.

    // pagination = 2
    final String query3 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o }";
    QueryIterator<?> qit3 = backend.submit(query3);
    qit3.setPagination(2);
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));
    evaluateQuery(qit3, expected, 3); // 3 iterations to get an empty result during the 3rd one.

    // pagination = 3
    final String query4 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o }";
    QueryIterator<?> qit4 = backend.submit(query4);
    qit4.setPagination(3);
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));
    evaluateQuery(qit4, expected, 2); // 2 iterations, stop because we don't have enough results

    // pagination = 4
    final String query5 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o }";
    QueryIterator<?> qit5 = backend.submit(query5);
    qit5.setPagination(4);
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));
    evaluateQuery(qit5, expected, 2);// 2 iterations to get an empty result during the 2nd one.

    // pagination = 5
    final String query6 = "SELECT distinct ?p { <http://lists.w3.org/Archives/Public/www-tag/2007Dec/0024.html> ?p ?o }";
    QueryIterator<?> qit6 = backend.submit(query6);
    qit4.setPagination(5);
    expected.put("p", Arrays.asList(
      "http://www.semanlink.net/2001/00/semanlink-schema#tag",
      "http://www.semanlink.net/2001/00/semanlink-schema#comment",
      "http://www.semanlink.net/2001/00/semanlink-schema#creationDate",
      "http://purl.org/dc/elements/1.1/title"
    ));
    evaluateQuery(qit6, expected, 1);// 1 iteration, stop because we don't have enough results
  }

  private void evaluateQuery(final QueryIterator<?> qit,
                             final Map<String, List<String>> expected,
                             final int expectedNPagination)
  throws Exception {
    final Map<String, List<String>> results = new HashMap<String, List<String>>();
    final Set<String> bindings = expected.keySet();

    while (qit.hasNext()) {
      final Object result = qit.next();

      if (result instanceof BindingSet) {
        /*
         * Select query without a custom value to iterate on
         */
        for (String bindingName : bindings) {
          final BindingSet bs = (BindingSet) result;
          if (bs.hasBinding(bindingName)) {
            if (!results.containsKey(bindingName)) {
              results.put(bindingName, new ArrayList<String>());
            }
            results.get(bindingName).add(
                bs.getValue(bindingName).stringValue());
          }
        }
      } else if (result instanceof Boolean) {
        /*
         * Ask query without a custom value to iterate on
         */
        final boolean bs = (Boolean) result;
        results.put(ASK_BINDING, Arrays.asList(Boolean.toString(bs)));
      } else if (result instanceof Statement) {
        /*
         * Describe / Construct query without a custom value to iterate on
         */
        final Statement bs = (Statement) result;
        if (!results.containsKey(CD_BINDING)) {
          results.put(CD_BINDING, new ArrayList<String>());
        }
        results.get(CD_BINDING).add(bs.toString());
      } else if (result instanceof String[]) {
        /*
         * Any query with the custom String[] value to iterate on
         */
        final String[] r = (String[]) result;
        for (String res : r) {
          for (String bindingName : bindings) {
            if (res.startsWith("DATA" + bindingName + " = ")) {
              if (!results.containsKey(bindingName)) {
                results.put(bindingName, new ArrayList<String>());
              }
              results.get(bindingName).add(
                  res.substring(7 + bindingName.length()));
            }
          }
        }
      } else if (result instanceof String) {
        /*
         * Any query with the custom String value to iterate on
         */
        final String[] r = ((String) result).split("\n");
        for (String res : r) {
          for (String bindingName : bindings) {
            if (res.startsWith(bindingName + " = ")) {
              if (!results.containsKey(bindingName)) {
                results.put(bindingName, new ArrayList<String>());
              }
              results.get(bindingName).add(
                  res.substring(3 + bindingName.length()));
            }
          }
        }
      }
    }
    // Check the results
    final Field f = qit.getClass().getDeclaredField("paginatedOffset");
    f.setAccessible(true);
    assertEquals(expectedNPagination, ((Long) f.get(qit)).intValue());

    for (String bindingName : bindings) {
      final List<String> res = results.get(bindingName);
      final List<String> exp = expected.get(bindingName);

      assertTrue("No Solutions found for " + bindingName, res != null);
      Collections.sort(res);
      Collections.sort(exp);
      assertArrayEquals(exp.toArray(new String[0]), res.toArray(new String[0]));
    }
    expected.clear();
    results.clear();
  }

}
