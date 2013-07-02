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

import java.util.List;

import org.openrdf.sindice.query.parser.sparql.ast.ASTBasicGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTConstraint;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphPatternGroup;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTRDFLiteral;
import org.openrdf.sindice.query.parser.sparql.ast.ASTRegexExpression;
import org.openrdf.sindice.query.parser.sparql.ast.ASTStr;
import org.openrdf.sindice.query.parser.sparql.ast.ASTString;
import org.openrdf.sindice.query.parser.sparql.ast.Node;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;

public class PofFilterProcessor {

  private PofFilterProcessor() {
  }

  public static void process(ASTQueryContainer ast, POFMetadata meta) {
    final ASTGraphPatternGroup gpg = ast.getQuery().getWhereClause().getGraphPatternGroup();

    final List<Object> keyword = meta.pofNode.getMetadata() == null ? null : meta.pofNode
    .getMetadata(SyntaxTreeBuilder.Keyword);
    final List<Object> prefix = meta.pofNode.getMetadata() == null ? null : meta.pofNode
    .getMetadata(SyntaxTreeBuilder.Prefix);
    final List<Object> qname = meta.pofNode.getMetadata() == null ? null : meta.pofNode
    .getMetadata(SyntaxTreeBuilder.Qname);

    if (keyword != null) {
      final ASTBasicGraphPattern bgp = addRegexFilter(meta.pofNode, keyword.get(0).toString(), true);
      gpg.jjtAppendChild(bgp);
    } else if (prefix != null) {
      final ASTBasicGraphPattern bgp = addRegexFilter(meta.pofNode, "^" + prefix.get(0).toString().substring(1), true);
      gpg.jjtAppendChild(bgp);
    } else if (qname != null) {
      final ASTBasicGraphPattern bgp = addRegexFilter(meta.pofNode, "^" + qname.get(0).toString(), true);
      gpg.jjtAppendChild(bgp);
    }
  }

  private static ASTBasicGraphPattern addRegexFilter(Node pof, String regex, boolean caseInsensitive) {
    final ASTBasicGraphPattern bgp = new ASTBasicGraphPattern(SyntaxTreeBuilderTreeConstants.JJTBASICGRAPHPATTERN);
    final ASTConstraint cst = new ASTConstraint(SyntaxTreeBuilderTreeConstants.JJTCONSTRAINT);
    final ASTRegexExpression astRegex = new ASTRegexExpression(SyntaxTreeBuilderTreeConstants.JJTREGEXEXPRESSION);

    // variable to test
    final ASTStr str = new ASTStr(SyntaxTreeBuilderTreeConstants.JJTSTR);
    str.jjtAppendChild(pof);
    astRegex.jjtAppendChild(str);
    // regular expression
    final ASTString strRegex = new ASTString(SyntaxTreeBuilderTreeConstants.JJTSTRING);
    strRegex.setValue(regex);
    final ASTRDFLiteral rdfLiteral = new ASTRDFLiteral(SyntaxTreeBuilderTreeConstants.JJTRDFLITERAL);
    rdfLiteral.jjtAppendChild(strRegex);
    astRegex.jjtAppendChild(rdfLiteral);
    // case insensitive or not
    if (caseInsensitive) {
      final ASTRDFLiteral ci = new ASTRDFLiteral(SyntaxTreeBuilderTreeConstants.JJTRDFLITERAL);
      final ASTString cistr = new ASTString(SyntaxTreeBuilderTreeConstants.JJTSTRING);
      cistr.setValue("i");
      ci.jjtAppendChild(cistr);
      astRegex.jjtAppendChild(ci);
    }
    cst.jjtAppendChild(astRegex);
    bgp.jjtAppendChild(cst);
    return bgp;
  }

}
