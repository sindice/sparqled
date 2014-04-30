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
package org.sindice.sparqled.sparql.preprocessing;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Random;

import org.openrdf.query.parser.sparql.ast.ASTVar;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;

/**
 * 
 */
public class ASTVarGenerator {

  public static long                         SEED    = 42;
  private static final Random                rand    = new Random(SEED);
  private static final LinkedHashSet<String> created = new LinkedHashSet<String>();
  private static final StringBuilder         sb      = new StringBuilder();

  public static void reset() {
    created.clear();
  }

  public static void addVars(Collection<String> vars) {
    for (String v : vars) {
      created.add(v.replace('-', 'n'));
    }
  }

  public static void addVar(String var) {
    created.add(var.replace('-', 'n'));
  }

  public static String[] getCurrentVarNames() {
    return created.toArray(new String[created.size()]);
  }

  public static ASTVar getASTVar(String prefix, String suffix) {
    String vn;

    do {
      sb.setLength(0);
      sb.append(prefix).append(rand.nextInt()).append(suffix);
      vn = sb.toString().replace('-', 'n');
    } while (created.contains(vn));
    created.add(vn);

    final ASTVar var = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
    var.setName(vn);
    return var;
  }

}
