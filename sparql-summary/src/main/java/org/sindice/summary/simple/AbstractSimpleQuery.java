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

import java.util.Iterator;

import org.apache.commons.lang.NotImplementedException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.summary.AbstractQuery;
import org.sindice.summary.Dump;

public abstract class AbstractSimpleQuery
extends AbstractQuery
implements Iterable<AbstractSimpleQuery.Structure> {

  /**
   * This class contains a statement about the structure of a SPARQL endpoint.
   * {@link #getDomain()} returns a class that appears on the subject position.
   * {@link #getRange()} returns a class that appears on the object position, associated
   * to the former class via the {@link #getPredicate() predicate}.
   */
  public static class Structure {
    private String domain;
    private String predicate;
    private String range;
    /**
     * @return the domain in the N-Triple format
     */
    public String getDomain() {
      return domain;
    }
    /**
     * @return the predicate in the N-Triple format
     */
    public String getPredicate() {
      return predicate;
    }
    /**
     * @return the range in the N-Triple format, or <code>null</code> if there is no class.
     */
    public String getRange() {
      return range;
    }

  }

  /**
   * Initialize the queries launcher.
   * 
   * @param d
   *          The Dump object, allows the use to modify the output easily.
   */
  public AbstractSimpleQuery(Dump d) {
    super(d);
  }

  /**
   * Initialize the queries launcher.
   */
  public AbstractSimpleQuery() {
    super();
  }

  @Override
  public void computeName() throws Exception {
  }

  private String getQuery() {
    String query = "SELECT DISTINCT ?source ?predicate ?target\n" + _graphFrom + "WHERE {\n";

    if (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() > 0) {
      query += "       { ?s <" + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(0)
          + "> ?source . }\n";
      for (int i = 1; i < AnalyticsClassAttributes.CLASS_ATTRIBUTES.size(); ++i) {
        query += "       UNION { ?s <"
            + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(i)
            + "> ?source . }\n";
      }
    }

    query += "        ?s ?predicate ?o .\n";

    // OPTIONAL
    query += "        OPTIONAL {\n";

    if (AnalyticsClassAttributes.CLASS_ATTRIBUTES.size() > 0) {
      query += "       { ?o <" + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(0)
          + "> ?target . }\n";
      for (int i = 1; i < AnalyticsClassAttributes.CLASS_ATTRIBUTES.size(); ++i) {
        query += "       UNION { ?o <"
            + AnalyticsClassAttributes.CLASS_ATTRIBUTES.get(i)
            + "> ?target . }\n";
      }
    }
    query += "        }\n" + "}\n";

    _logger.debug(query);
    return query;
  }

  /**
   * Get the name and the cardinality of the predicate/the edge for each node
   * and get the name of the node son/the target if it is possible.
   * 
   * @throws Exception
   */
  @Override
  public void computePredicate() throws Exception {
    launchQueryPred(getQuery());
  }

  @Override
  public Iterator<Structure> iterator() {
    final QueryIterator<BindingSet> queryIt;
    try {
      queryIt = _repository.submit(getQuery());
    } catch (SesameBackendException e) {
      throw new RuntimeException("Failed to run query", e);
    }

    if (_pagination >= 0)
      queryIt.setPagination(_pagination);

    return new Iterator<AbstractSimpleQuery.Structure>() {

      private final Structure st = new Structure();

      @Override
      public void remove() {
        throw new NotImplementedException();
      }

      @Override
      public Structure next() {
        return st;
      }

      @Override
      public boolean hasNext() {
        if (queryIt.hasNext()) {
          final BindingSet bs = queryIt.next();
          final Value s = bs.getValue("source");
          final Value p = bs.getValue("predicate");
          final Value o = bs.hasBinding("target") ? bs.getValue("target") : null;
          if (AnalyticsClassAttributes.isClass(p.stringValue())) {
            return hasNext();
          }
          st.domain = NTriplesUtil.toNTriplesString(s);
          st.predicate = NTriplesUtil.toNTriplesString(p);
          st.range = o == null ? null : NTriplesUtil.toNTriplesString(o);
          return true;
        }
        return false;
      }

    };
  }
}
