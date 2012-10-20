package org.sindice.summary.singlelabelled;

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
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.summary.AbstractQuery;
import org.sindice.summary.Dump;

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
public class DumpSingleLabelledTest {
  protected Logger _logger;

  @Before
  public void initLogger() {
    _logger = Logger.getLogger(DumpSingleLabelledTest.class);
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
  public void testWriteSingleLabelledRDF() {
    AbstractQuery q = null;
    try {
      Dump d = new Dump();
      q = new NativeSingleLabelledQuery(d, "/tmp/testUNIT/dumpstoresingle6");
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
      BufferedReader in = new BufferedReader(
          new InputStreamReader(new GZIPInputStream(new FileInputStream(
              "/tmp/testUNIT/dumpoutput1"))));
      String strLine;
      String str = "";

      while ((strLine = in.readLine()) != null) {
        // Print the content on the console
        str += strLine + "\n";
      }
      String ref = "<http://vocab.sindice.net/analytics#ec332783975807484476> <http://vocab.sindice.net/analytics#label> \"Thing\" .\n"
          + "<http://vocab.sindice.net/analytics#ec332783975807484476> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type4182323214728523352> .\n"
          + "<http://vocab.sindice.net/analytics#type4182323214728523352> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
          + "<http://vocab.sindice.net/analytics#type4182323214728523352> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec332783975807484476> .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/domain> \"sindice.com\" .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/domain_uri> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#ec3220177607457310356> <http://vocab.sindice.net/analytics#label> \"Human\" .\n"
          + "<http://vocab.sindice.net/analytics#ec3220177607457310356> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type7092576437160209772> .\n"
          + "<http://vocab.sindice.net/analytics#type7092576437160209772> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
          + "<http://vocab.sindice.net/analytics#type7092576437160209772> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec3220177607457310356> .\n"
          + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/domain> \"sindice.com\" .\n"
          + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/domain_uri> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
          + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/like> .\n"
          + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node9175872694952342699> .\n"
          + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node8479274785035382848> .\n"
          + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
          + "<http://vocab.sindice.net/analytics#edge1164459644941289088> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
          + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/like> .\n"
          + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node8479274785035382848> .\n"
          + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node8479274785035382848> .\n"
          + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
          + "<http://vocab.sindice.net/analytics#edgen5025709426772691178> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
          + "<http://vocab.sindice.net/analytics#edgen9119901656105001337> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/like> .\n"
          + "<http://vocab.sindice.net/analytics#edgen9119901656105001337> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edgen9119901656105001337> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node8479274785035382848> .\n"
          + "<http://vocab.sindice.net/analytics#edgen9119901656105001337> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node9175872694952342699> .\n"
          + "<http://vocab.sindice.net/analytics#edgen9119901656105001337> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
          + "<http://vocab.sindice.net/analytics#edgen9119901656105001337> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n";

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
    AbstractQuery q = null;
    try {
      q = new NativeSingleLabelledQuery("/tmp/testUNIT/dumpstoresingle7");
      q.setGraph("http://www.testunit.com");
      q.addFileToRepository("src/test/resources/unit_test_no_bc.nt",
          RDFFormat.N3, NTriplesUtil.parseResource(
              "<http://www.testunit.com>", new MemValueFactory()));
    } catch (Exception e) {
      _logger.error(e.getMessage());
      fail("wrong initialisation");
    }

    try {
      q.initDump("/tmp/testUNIT/dumpoutput4");
      q.computeName();
      q.computePredicate();
      q.stopConnexion();
      BufferedReader in = new BufferedReader(
          new InputStreamReader(new GZIPInputStream(new FileInputStream(
              "/tmp/testUNIT/dumpoutput4"))));
      String strLine;
      String str = "";

      while ((strLine = in.readLine()) != null) {
        // Print the content on the console
        str += strLine + "\n";
      }
      String ref = "<http://vocab.sindice.net/analytics#ecn4526382872016103446> <http://vocab.sindice.net/analytics#label> \"Thing\" .\n"
          + "<http://vocab.sindice.net/analytics#ecn4526382872016103446> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#typen2144689109856865388> .\n"
          + "<http://vocab.sindice.net/analytics#typen2144689109856865388> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
          + "<http://vocab.sindice.net/analytics#typen2144689109856865388> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#noden5197337983796850790> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ecn4526382872016103446> .\n"
          + "<http://vocab.sindice.net/analytics#noden5197337983796850790> <http://vocab.sindice.net/domain> \"http://www.testunit.com\" .\n"
          + "<http://vocab.sindice.net/analytics#noden5197337983796850790> <http://vocab.sindice.net/domain_uri> <http://www.testunit.com> .\n"
          + "<http://vocab.sindice.net/analytics#noden5197337983796850790> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#ec2485048267420148715> <http://vocab.sindice.net/analytics#label> \"Human\" .\n"
          + "<http://vocab.sindice.net/analytics#ec2485048267420148715> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#typen9151021515892793100> .\n"
          + "<http://vocab.sindice.net/analytics#typen9151021515892793100> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
          + "<http://vocab.sindice.net/analytics#typen9151021515892793100> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#node6879956730694495624> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec2485048267420148715> .\n"
          + "<http://vocab.sindice.net/analytics#node6879956730694495624> <http://vocab.sindice.net/domain> \"http://www.testunit.com\" .\n"
          + "<http://vocab.sindice.net/analytics#node6879956730694495624> <http://vocab.sindice.net/domain_uri> <http://www.testunit.com> .\n"
          + "<http://vocab.sindice.net/analytics#node6879956730694495624> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/like> .\n"
          + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#noden5197337983796850790> .\n"
          + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node6879956730694495624> .\n"
          + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
          + "<http://vocab.sindice.net/analytics#edge3039431683361620530> <http://vocab.sindice.net/analytics#publishedIn> <http://www.testunit.com> .\n"
          + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/like> .\n"
          + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node6879956730694495624> .\n"
          + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node6879956730694495624> .\n"
          + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
          + "<http://vocab.sindice.net/analytics#edgen148178575938144458> <http://vocab.sindice.net/analytics#publishedIn> <http://www.testunit.com> .\n"
          + "<http://vocab.sindice.net/analytics#edgen4810229117146330009> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/like> .\n"
          + "<http://vocab.sindice.net/analytics#edgen4810229117146330009> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edgen4810229117146330009> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node6879956730694495624> .\n"
          + "<http://vocab.sindice.net/analytics#edgen4810229117146330009> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#noden5197337983796850790> .\n"
          + "<http://vocab.sindice.net/analytics#edgen4810229117146330009> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
          + "<http://vocab.sindice.net/analytics#edgen4810229117146330009> <http://vocab.sindice.net/analytics#publishedIn> <http://www.testunit.com> .\n";

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
    AbstractQuery q = null;
    try {
      Dump d = new Dump();
      q = new NativeSingleLabelledQuery(d, "/tmp/testUNIT/dumpstoresingle8");
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
      BufferedReader in = new BufferedReader(
          new InputStreamReader(new GZIPInputStream(new FileInputStream(
              "/tmp/testUNIT/dumpoutput4"))));
      String strLine;
      String str = "";

      while ((strLine = in.readLine()) != null) {
        // Print the content on the console
        str += strLine + "\n";
      }
      String ref = "<http://vocab.sindice.net/analytics#ecn5758280603750808292> <http://vocab.sindice.net/analytics#label> \"test complexe\" .\n"
          + "<http://vocab.sindice.net/analytics#ecn5758280603750808292> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type5362401009120084618> .\n"
          + "<http://vocab.sindice.net/analytics#type5362401009120084618> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
          + "<http://vocab.sindice.net/analytics#type5362401009120084618> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#noden42961954810497749> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ecn5758280603750808292> .\n"
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
    AbstractQuery q = null;
    try {
      Dump d = new Dump();
      q = new NativeSingleLabelledQuery(d, "/tmp/testUNIT/dumpstoresingle9");
      q.addFileToRepository("src/test/resources/unit_test_multidomain.nt",
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
      BufferedReader in = new BufferedReader(
          new InputStreamReader(new GZIPInputStream(new FileInputStream(
              "/tmp/testUNIT/dumpoutput2"))));
      String strLine;
      String str = "";

      while ((strLine = in.readLine()) != null) {
        // Print the content on the console
        str += strLine + "\n";
      }
      String ref = "<http://vocab.sindice.net/analytics#ec7529220587856463086> <http://vocab.sindice.net/analytics#label> \"Thing\" .\n"
          + "<http://vocab.sindice.net/analytics#ec7529220587856463086> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#typen2335203843773632621> .\n"
          + "<http://vocab.sindice.net/analytics#typen2335203843773632621> <http://vocab.sindice.net/analytics#label> <http://ogp.me/ns#type> .\n"
          + "<http://vocab.sindice.net/analytics#typen2335203843773632621> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec7529220587856463086> .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/domain> \"sindice.com\" .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/domain_uri> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#ec3220177607457310356> <http://vocab.sindice.net/analytics#label> \"Human\" .\n"
          + "<http://vocab.sindice.net/analytics#ec3220177607457310356> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#type7092576437160209772> .\n"
          + "<http://vocab.sindice.net/analytics#type7092576437160209772> <http://vocab.sindice.net/analytics#label> <http://opengraphprotocol.org/schema/type> .\n"
          + "<http://vocab.sindice.net/analytics#type7092576437160209772> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec3220177607457310356> .\n"
          + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/domain> \"sindice.com\" .\n"
          + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/domain_uri> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
          + "<http://vocab.sindice.net/analytics#node8479274785035382848> <http://vocab.sindice.net/analytics#cardinality> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#ec1701502297987654852> <http://vocab.sindice.net/analytics#label> \"Thing\" .\n"
          + "<http://vocab.sindice.net/analytics#ec1701502297987654852> <http://vocab.sindice.net/analytics#type> <http://vocab.sindice.net/analytics#typen5869117135414585920> .\n"
          + "<http://vocab.sindice.net/analytics#typen5869117135414585920> <http://vocab.sindice.net/analytics#label> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> .\n"
          + "<http://vocab.sindice.net/analytics#typen5869117135414585920> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/analytics#label> <http://vocab.sindice.net/analytics#ec1701502297987654852> .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/domain> \"sindice.com\" .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/domain_uri> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
          + "<http://vocab.sindice.net/analytics#node9175872694952342699> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#label> <http://purl.org/dc/elements/1.1/like> .\n"
          + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node9175872694952342699> .\n"
          + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node8479274785035382848> .\n"
          + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
          + "<http://vocab.sindice.net/analytics#edge2096773358028559333> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
          + "<http://vocab.sindice.net/analytics#edge6346862235618451077> <http://vocab.sindice.net/analytics#label> <http://purl.org/dc/elements/1.1/like> .\n"
          + "<http://vocab.sindice.net/analytics#edge6346862235618451077> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edge6346862235618451077> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node9175872694952342699> .\n"
          + "<http://vocab.sindice.net/analytics#edge6346862235618451077> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node9175872694952342699> .\n"
          + "<http://vocab.sindice.net/analytics#edge6346862235618451077> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
          + "<http://vocab.sindice.net/analytics#edge6346862235618451077> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
          + "<http://vocab.sindice.net/analytics#edgen2689452839026328339> <http://vocab.sindice.net/analytics#label> <http://ogp.me/ns#like> .\n"
          + "<http://vocab.sindice.net/analytics#edgen2689452839026328339> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edgen2689452839026328339> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node8479274785035382848> .\n"
          + "<http://vocab.sindice.net/analytics#edgen2689452839026328339> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node9175872694952342699> .\n"
          + "<http://vocab.sindice.net/analytics#edgen2689452839026328339> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
          + "<http://vocab.sindice.net/analytics#edgen2689452839026328339> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n"
          + "<http://vocab.sindice.net/analytics#edge7535851203409138364> <http://vocab.sindice.net/analytics#label> <http://ogp.me/ns#like> .\n"
          + "<http://vocab.sindice.net/analytics#edge7535851203409138364> <http://vocab.sindice.net/analytics#cardinality> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
          + "<http://vocab.sindice.net/analytics#edge7535851203409138364> <http://vocab.sindice.net/analytics#source> <http://vocab.sindice.net/analytics#node9175872694952342699> .\n"
          + "<http://vocab.sindice.net/analytics#edge7535851203409138364> <http://vocab.sindice.net/analytics#target> <http://vocab.sindice.net/analytics#node9175872694952342699> .\n"
          + "<http://vocab.sindice.net/analytics#edge7535851203409138364> <http://vocab.sindice.net/analytics#target> \"dummy class: 4841526962763945421\" .\n"
          + "<http://vocab.sindice.net/analytics#edge7535851203409138364> <http://vocab.sindice.net/analytics#publishedIn> <http://sindice.com/dataspace/default/domain/sindice.com> .\n";

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
  public void testWrongClassAtrribute() {
    String[] type = { "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" };
    AnalyticsClassAttributes.initClassAttributes(type);
    AbstractQuery q = null;
    try {
      Dump d = new Dump();
      q = new NativeSingleLabelledQuery(d, "/tmp/testUNIT/dumpstoresingle10");
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
      BufferedReader in = new BufferedReader(
          new InputStreamReader(new GZIPInputStream(new FileInputStream(
              "/tmp/testUNIT/dumpoutput4"))));
      String strLine;
      String str = "";

      while ((strLine = in.readLine()) != null) {
        // Print the content on the console
        str += strLine + "\n";
      }
      String ref = "";

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
