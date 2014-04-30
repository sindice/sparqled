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

import static org.junit.Assert.assertArrayEquals;
import info.aduna.io.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for testing {@link Dump}
 */
public abstract class AbstractDumpTest {

  protected final static Logger _logger = LoggerFactory.getLogger(AbstractDumpTest.class);
  protected File testOutput;

  @Before
  public void setUp() {
    String[] type = {
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
      "http://opengraphprotocol.org/schema/type",
      "http://ogp.me/ns#type",
      "http://opengraph.org/schema/type",
      "http://purl.org/dc/elements/1.1/type",
      "http://dbpedia.org/property/type"
    };
    AnalyticsClassAttributes.initClassAttributes(type);
    testOutput = new File("/tmp/testUNIT/");
  }

  @After
  public void clean()
  throws IOException {
    FileUtil.deleteDir(testOutput);
  }

  /**
   * Executes the graph summary and asserts the generated RDF data.
   * @param q the {@link AbstractQuery}
   * @param out the path to the generated data
   * @param folder the name of the current test folder with expected results
   * @throws Exception if an error occurred while asserting the data
   */
  protected void _assertDump(final AbstractQuery q, final String out, final String folder)
  throws Exception {
    BufferedReader actual = null;
    BufferedReader expected = null;

    try {
      q.initDump(out);
      q.computeName();
      q.computePredicate();
      q.stopConnexion();

      // actual results
      String line;
      actual = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(out))));
      final List<String> actualResults = new ArrayList<String>();
      while ((line = actual.readLine()) != null) {
        actualResults.add(line);
      }
      Collections.sort(actualResults);

      // Expected results
      final String name = StringUtils.uncapitalize(this.getClass().getSimpleName());
      final String file = "./src/test/resources/" + name + "/" + folder + "/output.txt";
      expected = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      final List<String> expectedResults = new ArrayList<String>();
      while ((line = expected.readLine()) != null) {
        expectedResults.add(line);
      }
      Collections.sort(expectedResults);

      assertArrayEquals(expectedResults.toArray(new String[0]), actualResults.toArray(new String[0]));
    }
    finally {
      try {
        if (expected != null) {
          expected.close();
        }
      }
      finally {
        try {
          if (actual != null) {
            actual.close();
          }
        }
        finally {
          q.stopConnexion();
        }
      }
    }
  }

}
