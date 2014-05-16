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
package org.sindice.analytics.ranking;

import java.util.Iterator;

import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.analytics.queryProcessor.QueryProcessor;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor;

public class DGSQueryResultProcessor
implements QueryResultProcessor<Label> {

  @Override
  public Label process(Object o) {
    final Label label;
    final BindingSet set = (BindingSet) o; // The DGS query is a SELECT query

    final Value pof = set.getValue(SyntaxTreeBuilder.PointOfFocus);
    final Value pofCard = set.getValue(QueryProcessor.CARDINALITY_VAR);

    if (pof != null) {
      label = new Label(pof, pofCard == null ? 0 : Long.valueOf(pofCard.stringValue()));
      final Iterator<Binding> it = set.iterator();
      while (it.hasNext()) {
        final Binding binding = it.next();

        if (!binding.getName().equals(SyntaxTreeBuilder.PointOfFocus) &&
            !binding.getName().equals(QueryProcessor.CARDINALITY_VAR)) {
          label.addContext(binding.getName(), binding.getValue().stringValue());
        }
      }
      return label;
    }
    return null;
  }

}
