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
package org.sindice.analytics.queryProcessor;

import org.openrdf.sindice.query.parser.sparql.ast.ASTObjectList;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathAlternative;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathElt;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathSequence;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPropertyListPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.Node;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;

/**
 * Create a Triple Pattern "node"
 */
public final class ASTProcessorUtil {

  private ASTProcessorUtil() {
  }

  public static ASTTriplesSameSubjectPath createTriple(Node subject,
                                                       Node predicate,
                                                       Node object) {
    final ASTTriplesSameSubjectPath triple = new ASTTriplesSameSubjectPath(SyntaxTreeBuilderTreeConstants.JJTTRIPLESSAMESUBJECTPATH);
    final ASTPropertyListPath pListPath = new ASTPropertyListPath(SyntaxTreeBuilderTreeConstants.JJTPROPERTYLISTPATH);
    final ASTPathAlternative pathAlt = new ASTPathAlternative(SyntaxTreeBuilderTreeConstants.JJTPATHALTERNATIVE);
    final ASTPathSequence pathSeq = new ASTPathSequence(SyntaxTreeBuilderTreeConstants.JJTPATHSEQUENCE);
    final ASTPathElt pathElt = new ASTPathElt(SyntaxTreeBuilderTreeConstants.JJTPATHELT);
    final ASTObjectList objList = new ASTObjectList(SyntaxTreeBuilderTreeConstants.JJTOBJECTLIST);

    // Subject
    triple.jjtAppendChild(subject);
    // Predicate
    if (predicate instanceof ASTVar) { // VerbSimple
      pListPath.jjtAppendChild(predicate);
    } else { // VerbPath
      pListPath.jjtAppendChild(pathAlt);
      pathAlt.jjtAppendChild(pathSeq);
      pathSeq.jjtAppendChild(pathElt);
      pathElt.jjtAppendChild(predicate);
    }
    triple.jjtAppendChild(pListPath);
    // Object
    objList.jjtAppendChild(object);
    pListPath.jjtAppendChild(objList);

    return triple;
  }

}
