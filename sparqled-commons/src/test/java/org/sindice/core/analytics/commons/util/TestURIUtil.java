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
package org.sindice.core.analytics.commons.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author diego
 */
public class TestURIUtil {
  final Logger logger = LoggerFactory.getLogger(TestURIUtil.class);

  @Test
  public void testSecondDomain() {
    String[] testStrings = new String[] {
        "google.com -> google.com",
        "www.webaddict.za -> webaddict.za",
        "wilma     ->       null",
        "wilma.blogSpot.com  -> blogspot.com",
        "repubblica.it  -> repubblica.it",
        "isti.pomino.cnr.it -> cnr.it",
        "thelakersnation.com -> thelakersnation.com",
        "bio2rdf.org -> bio2rdf.org",
        "awakenedmmo.org -> awakenedmmo.org",
        "cheminfov.informatics.indiana.edu -> indiana.edu",
        "data-gov.tw.rpi.edu -> rpi.edu",
        "www.test.webaddict.co.za -> webaddict.co.za",
        "www.webaddict.co.za -> webaddict.co.za",
        "mxr.mozilla.org -> mozilla.org",
        "www.yago.zitgist.com -> zitgist.com",
        "aims.fao.org -> fao.org",
        "en.wikipedia.org -> wikipedia.org",
        "127.0.0.100 -> null",
        "http://en.wikipedia.org -> null"
    };
    for (String s : testStrings) {
      String[] elems = s.split("\\s+->\\s+");
      String domain = elems[0];
      String expectedSecondDomain = elems[1];
      logger.info("Check if sndDomain of {} equals {}", domain, expectedSecondDomain);
      final String sndDomain = URIUtil.getSndDomain(domain);
      assertEquals(sndDomain == null ? "null" : sndDomain, expectedSecondDomain);

    }
    assertEquals(null, URIUtil.getSndDomain(""));
    assertEquals(null, URIUtil.getSndDomain(null));
  }

  @Test
  public void testURL()
  throws Exception {
    final String s1 = "http://downloads.dbpedia.org/3.7/de/mappingbased_properties_de.nq.bz2";
    final String s2 = "http://dbpedia.org/resource/DienvidsusÃ„Â“ja__mouthPosition__1/";

    assertEquals("dbpedia.org", URIUtil.getSndDomainFromUrl(s1));
    assertEquals("dbpedia.org", URIUtil.getSndDomainFromUrl(s2));
  }

}
