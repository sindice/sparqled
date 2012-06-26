/*******************************************************************************
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
 *******************************************************************************/
/**
 * @project sparql-editor-servlet
 * @author Campinas Stephane [ 17 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTBasicGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphPatternGroup;
import org.openrdf.sindice.query.parser.sparql.ast.ASTObjectList;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathAlternative;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathElt;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathSequence;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPropertyListPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTUnionGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.Node;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

/**
 * Transforms triple path into a sequence of basic triple patterns
 * @author Stephane Campinas [17 Jun 2012]
 * @email stephane.campinas@deri.org
 *
 */
public final class DeNormalizeTriplesPaths {

  private static Node subject;

  private DeNormalizeTriplesPaths() {
  }

  public static List<Node> process(ASTTriplesSameSubjectPath node)
  throws VisitorException {
    final ArrayList<Node> group = new ArrayList<Node>();

    // get the subject: already denormalized
    subject = node.jjtGetChild(0);
    // expand the property lists of that subject
    node.jjtAccept(new DeNormalizeTriplesVisitor(), group);
    return group;
  }

  private static class DeNormalizeTriplesVisitor
  extends ASTVisitorBase {

    @Override
    public Object visit(ASTPropertyListPath node, Object data)
    throws VisitorException {
      final ArrayList<Node> group = (ArrayList<Node>) data;
      final SimpleNode verb = (SimpleNode) node.getVerb();

      if (verb instanceof ASTVar) { // VerbSimple
        processObjectList(subject, verb, node.getObjectList(), group);
      } else if (verb instanceof ASTPathAlternative) { // VerbPath
        final ASTPathAlternative pathAlt = (ASTPathAlternative) verb;

        for (int i = 0; i < pathAlt.jjtGetNumChildren(); i++) {
          final int startPathSequenceIndex = group.size();
          final ASTPathSequence pathSeq = (ASTPathSequence) pathAlt.jjtGetChild(i);
          final List<ASTPathElt> pathElts = pathSeq.getPathElements();
          Node interimSubject = subject;

          for (int j = 0; j < pathElts.size() - 1; j++) { // for each predicate defining the path to the object
            final ASTVar interimObject = ASTVarGenerator.getASTVar("pathElt");
            final Node s;
            final Node o;

            if (pathElts.get(j).isNestedPath()) {
              throw new DGSException("Nested Property Path are not supported yet");
            } else if (pathElts.get(j).isNegatedPropertySet()) {
              throw new DGSException("Negated Property Path are not supported yet");
            } else if (pathElts.get(j).isInverse()) {
              s = interimObject;
              o = interimSubject;
            } else {
              s = interimSubject;
              o = interimObject;
            }

            group.add(ASTProcessorUtil.createTriple(s, pathElts.get(j).jjtGetChild(0), o));
            interimSubject = interimObject;
          }
          // connect the objects to the last predicate of the path
          final ASTPathElt p = pathElts.get(pathElts.size() - 1);
          processObjectList(interimSubject, p.jjtGetChild(0), node.getObjectList(), p.isInverse(), group);

          if (pathAlt.jjtGetNumChildren() > 1) {
            // Create a union from the previous patterns (if more than 1 path in the PathSequence)
            createPathSequenceUnion(startPathSequenceIndex, i, pathAlt.jjtGetNumChildren(), group);
          }
        }
      }
      return super.visit(node, data);
    }

    private void createPathSequenceUnion(int startGroupIndex,
                                         int pathSequencePos,
                                         int nPathSequence,
                                         ArrayList<Node> group) {
      final int end = group.size();
      final ASTGraphPatternGroup gpgLhs = new ASTGraphPatternGroup(SyntaxTreeBuilderTreeConstants.JJTGRAPHPATTERNGROUP);
      final ASTBasicGraphPattern bgpLhs = new ASTBasicGraphPattern(SyntaxTreeBuilderTreeConstants.JJTBASICGRAPHPATTERN);

      if (pathSequencePos == 0) { // init UNIONs
        final ASTUnionGraphPattern union = new ASTUnionGraphPattern(SyntaxTreeBuilderTreeConstants.JJTUNIONGRAPHPATTERN);

        union.jjtAppendChild(gpgLhs);
        gpgLhs.jjtAppendChild(bgpLhs);
        for (int i = startGroupIndex; i < end; i++) {
          bgpLhs.jjtAppendChild(group.get(startGroupIndex));
          group.remove(startGroupIndex);
        }
        group.add(union);
      } else if (pathSequencePos + 1 == nPathSequence) { // close the UNIONs
        ASTUnionGraphPattern union = (ASTUnionGraphPattern) group.get(startGroupIndex - 1);

        gpgLhs.jjtAppendChild(bgpLhs);
        for (int i = startGroupIndex; i < end; i++) {
          bgpLhs.jjtAppendChild(group.get(startGroupIndex));
          group.remove(startGroupIndex);
        }
        while (union.jjtGetNumChildren() == 2) { // get down to the last union
          union = (ASTUnionGraphPattern) union.jjtGetChild(1);
        }
        union.jjtAppendChild(gpgLhs);
      } else { // append UNION element
        ASTUnionGraphPattern union = (ASTUnionGraphPattern) group.get(startGroupIndex - 1);
        final ASTUnionGraphPattern unionRhs = new ASTUnionGraphPattern(SyntaxTreeBuilderTreeConstants.JJTUNIONGRAPHPATTERN);

        unionRhs.jjtAppendChild(gpgLhs);
        gpgLhs.jjtAppendChild(bgpLhs);
        for (int i = startGroupIndex; i < end; i++) {
          bgpLhs.jjtAppendChild(group.get(startGroupIndex));
          group.remove(startGroupIndex);
        }
        while (union.jjtGetNumChildren() == 2) { // get down to the last union
          union = (ASTUnionGraphPattern) union.jjtGetChild(1);
        }
        union.jjtAppendChild(unionRhs);
      }
    }

    private void processObjectList(Node subject,
                                   Node verb,
                                   ASTObjectList objList,
                                   ArrayList<Node> group) {
      this.processObjectList(subject, verb, objList, false, group);
    }

    private void processObjectList(Node subject,
                                   Node verb,
                                   ASTObjectList objList,
                                   boolean isInverse,
                                   ArrayList<Node> group) {
      for (int i = 0; i < objList.jjtGetNumChildren(); i++) {
        final Node o = objList.jjtGetChild(i);

        // simple object list
//        if (o instanceof ASTVar || o instanceof ASTQName || o instanceof ASTIRI) {
          if (isInverse) {
            group.add(ASTProcessorUtil.createTriple(o, verb, subject));
          } else {
            group.add(ASTProcessorUtil.createTriple(subject, verb, o));
          }
//        }
      }
    }

  }

}
