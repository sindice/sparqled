package org.sindice.summary.multilabelled;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.sindice.summary.Dump;
import org.sindice.summary.multilabelled.AbstractMultiLabelledQuery;
import org.sindice.summary.multilabelled.HTTPVirtuosoMultiLabelledQuery;

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
public class HTTPVirtuosoMultiLabelledTest {
  protected Logger _logger;

  @Before
  public void initLogger() {
    _logger = Logger.getLogger(HTTPVirtuosoMultiLabelledTest.class);
  }

  @Test
  public void testMakeGroupConcat() {
    AbstractMultiLabelledQuery q = null;
    try {
      Dump d = new Dump();
      q = new HTTPVirtuosoMultiLabelledQuery(d);
    } catch (Exception e) {
      // useless
      _logger.error(e.getMessage());
      fail("Invalide query intialization.");
    }
    String s = q.makeGroupConcat("?init", "?group");
    String ref = " (sql:GROUP_CONCAT(IF(isURI(?init),\n"
        + "                bif:concat('<', str(?init), '>'),\n"
        + "                bif:concat('\"', ENCODE_FOR_URI(?init), '\"')), \" \") AS ?group)\n";
    assertEquals(ref, s);
  }
}
