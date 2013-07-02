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
package org.sindice.summary.singlelabelled;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.summary.AbstractDumpTest;
import org.sindice.summary.AbstractQuery;
import org.sindice.summary.Dump;

/**
 * @author Pierre Bailly <pierre.bailly@deri.org>
 */
public class DumpSingleLabelledTest
extends AbstractDumpTest {

  @Test
  public void testWriteSingleLabelledRDF()
  throws Exception {
    AbstractQuery q = new NativeSingleLabelledQuery(new Dump(), testOutput + "/dumpstoresingle6");
    q.addFileToRepository("src/test/resources/unit_test_no_bc.nt", RDFFormat.N3);

    _assertDump(q, testOutput + "/dumpoutput6", "testWriteSingleLabelledRDF");
  }

  @Test
  public void testWriteDomainRDF()
  throws Exception {
    AbstractQuery q = new NativeSingleLabelledQuery(testOutput + "/dumpstoresingle7");
    q.setGraph("http://www.testunit.com");
    q.addFileToRepository("src/test/resources/unit_test_no_bc.nt", RDFFormat.N3, NTriplesUtil
    .parseResource("<http://www.testunit.com>", new MemValueFactory()));

    _assertDump(q, testOutput + "/dumpoutput7", "testWriteDomainRDF");
  }

  @Test
  public void testRDFEncode()
  throws Exception {
    AbstractQuery q = new NativeSingleLabelledQuery(new Dump(), testOutput + "/dumpstoresingle8");
    q.addFileToRepository("src/test/resources/unit_test_encode.nt", RDFFormat.N3);

    _assertDump(q, testOutput + "/dumpoutput8", "testRDFEncode");
  }

  @Test
  public void testRDFWithMultipleDomain()
  throws Exception {
    AbstractQuery q = new NativeSingleLabelledQuery(new Dump(), testOutput + "/dumpstoresingle9");
    q.addFileToRepository("src/test/resources/unit_test_multidomain.nt", RDFFormat.N3);

    _assertDump(q, testOutput + "/dumpoutput9", "testRDFWithMultipleDomain");
  }

  @Test
  public void testWrongClassAttribute()
  throws Exception {
    String[] type = { "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" };
    AnalyticsClassAttributes.initClassAttributes(type);
    AbstractQuery q = new NativeSingleLabelledQuery(new Dump(), testOutput + "/dumpstoresingle10");
    q.addFileToRepository("src/test/resources/unit_test_encode.nt", RDFFormat.N3);

    _assertDump(q, testOutput + "/dumpoutput10", "testWrongClassAttribute");
  }

}
