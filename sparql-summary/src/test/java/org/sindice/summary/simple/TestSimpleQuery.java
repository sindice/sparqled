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
package org.sindice.summary.simple;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import info.aduna.io.FileUtil;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.summary.AbstractQuery;
import org.sindice.summary.Dump;
import org.sindice.summary.DumpString;
import org.sindice.summary.simple.AbstractSimpleQuery.Structure;


@RunWith(value = Parameterized.class)
public class TestSimpleQuery {

  private final Logger                               _logger = Logger.getLogger(TestSimpleQuery.class);
  private final Class<? extends AbstractSimpleQuery> clazz;
  private AbstractSimpleQuery                        q       = null;

  /**
   * 
   */
  public TestSimpleQuery(Class<? extends AbstractSimpleQuery> clazz) {
    this.clazz = clazz;
  }

  @Parameters
  public static Collection<Object[]> data() {
    Object[][] data = new Object[][] { { MemorySimpleQuery.class }, { NativeSimpleQuery.class } };
    return Arrays.asList(data);
  }

  @Before
  public void init() {
    String[] type = { "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" };
    AnalyticsClassAttributes.initClassAttributes(type);
  }

  @After
  public void clean()
  throws Exception {
    if (q != null) {
      q.stopConnexion();
    }
    FileUtil.deleteDir(new File("/tmp/testUNIT/"));
  }

  private AbstractSimpleQuery getSimpleQuery(final Writer out)
  throws Exception {
    final Dump dump = new DumpSimple();
    dump.setWriter(out);
    return clazz.getConstructor(Dump.class, String.class).newInstance(dump, "/tmp/testUNIT/store");
  }

  @Test
  public void testQueryMemory() {
    AbstractQuery q = null;
    try {
      q = getSimpleQuery(new StringWriter());
      try {
        q.stopConnexion();
      } catch (RepositoryException e) {
        // Nothing for this test
      }
    } catch (Exception e) {
      _logger.error(e.getMessage());
      fail(e.toString());
    }
  }

  @Test
  public void testClassesRelations()
  throws Exception {
    final List<String> ref = new ArrayList<String>();

    ref.add("<http://example.org/Person> <http://example.org/likes> <http://example.org/Person> .");
    ref.add("<http://example.org/Person> <http://example.org/author> <http://example.org/Document> .");
    assertQuery("testClassesRelations", ref);
  }

  @Test
  public void testClassesProperties()
  throws Exception {
    final List<String> ref = new ArrayList<String>();

    ref.add("<http://example.org/Person> <http://example.org/likes> \"\" .");
    ref.add("<http://example.org/Person> <http://example.org/name> \"\" .");
    ref.add("<http://example.org/Person> <http://example.org/age> \"\" .");
    assertQuery("testClassesProperties", ref);
  }

  @Test
  public void testStopConnexion() {
    AbstractQuery q;
    try {
      Dump d = new DumpString();
      q = new MemorySimpleQuery(d, "/tmp/testUNIT/memorystoresingle24");
      q.stopConnexion();
    } catch (Exception e) {
      _logger.error(e.getMessage());
      fail("Cannot close the connection");
    }
  }

  private void assertQuery(final String folder, final List<String> ref)
  throws Exception {
    final StringWriter w = new StringWriter();

    q = getSimpleQuery(w);
    q.addFileToRepository("src/test/resources/testSimpleQuery/" + folder + "/input.n3", RDFFormat.N3);
    q.computePredicate();

    final String[] actual = w.toString().split("\n");
    Arrays.sort(actual);
    Collections.sort(ref);
    assertArrayEquals(ref.toArray(), actual);

    final List<String> actual2 = new ArrayList<String>();
    for (Structure st : q) {
      actual2.add(st.getDomain() + " " + st.getPredicate() + " " + st.getRange() + " .");
    }
    Collections.sort(actual2);
    assertEquals(ref, actual2);
  }

}
