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

import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.*;

/**
 * 
 */
public class AST2TextTranslator {

  public static String translate(ASTQueryContainer ast)
  throws VisitorException {
    final StringBuilder sb = new StringBuilder();

    final AST2TextVisitor v = new AST2TextVisitor();
    v.visit(ast, sb);
    return sb.toString();
  }

  private static class AST2TextVisitor extends ASTVisitorBase {

    @Override
    public Object visit(ASTConstraint node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      if (node.jjtGetParent() instanceof ASTHavingClause) {
        sb.append("Having ");
      } else {
        sb.append("FILTER ");
      }
      sb.append('(');
      node.childrenAccept(this, data);
      sb.append(')');
      return data;
    }

    @Override
    public Object visit(ASTBaseDecl node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("BASE <").append(node.getIRI()).append(">\n");
      return data;
    }

    @Override
    public Object visit(ASTPrefixDecl node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("PREFIX ").append(node.getPrefix()).append(": ");
      node.childrenAccept(this, data);
      sb.append('\n');
      return data;
    }

    @Override
    public Object visit(ASTSelect node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("SELECT ");
      if (node.isDistinct()) {
        sb.append("DISTINCT ");
      } else if (node.isReduced()) {
        sb.append("REDUCED ");
      }
      if (node.isWildcard()) {
        sb.append("*\n");
      } else {
        for (ASTProjectionElem pe : node.getProjectionElemList()) {
          pe.jjtAccept(this, data);
        }
      }
      return data;
    }

    @Override
    public Object visit(ASTProjectionElem node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      if (node.hasAlias()) { // (Expression AS ?something)
        sb.append('(');
        node.childrenAccept(this, data);
        sb.append(" AS ?").append(node.getAlias()).append(')');
      } else {
        node.childrenAccept(this, data);
      }
      return data;
    }

    @Override
    public Object visit(ASTConstruct node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("CONSTRUCT");
      if (!node.isWildcard()) {
        sb.append(" {");
        node.childrenAccept(this, data);
        sb.append('}');
      }
      sb.append('\n');
      return data;
    }

    @Override
    public Object visit(ASTDescribe node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("DESCRIBE");
      if (node.isWildcard()) {
        sb.append(" *");
      } else {
        node.childrenAccept(this, data);
      }
      sb.append('\n');
      return data;
    }

    @Override
    public Object visit(ASTAskQuery node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("ASK\n");
      return data;
    }

    @Override
    public Object visit(ASTDatasetClause node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("FROM ");
      if (node.isNamed()) {
        sb.append("NAMED ");
      }
      node.childrenAccept(this, data);
      sb.append('\n');
      return data;
    }

    @Override
    public Object visit(ASTWhereClause node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("WHERE ");
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTBindingsClause node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      if (node.jjtGetNumChildren() != 0) {
        sb.append("BINDINGS ");
        for (Node child : node.jjtGetChildren()) {
          if (child instanceof ASTBindingSet) {
            sb.append("{\n");
            node.childrenAccept(this, data);
            sb.append("}\n");
          } else {
            node.childrenAccept(this, data);
          }
        }
        sb.append('\n');
      }
      return data;
    }

    @Override
    public Object visit(ASTBindingSet node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      if (node.jjtGetNumChildren() != 0) {
        sb.append("( ");
        node.childrenAccept(this, data);
        sb.append(")\n");
      } else { // NIL
        sb.append("()\n");
      }
      return data;
    }

    @Override
    public Object visit(ASTBindingValue node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      if (node.jjtGetNumChildren() != 0) {
        node.childrenAccept(this, data);
      } else { // UNDEF
        sb.append("UNDEF\n");
      }
      return data;
    }

    @Override
    public Object visit(ASTGroupClause node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("GROUP BY ");
      node.childrenAccept(this, data);
      sb.append('\n');
      return data;
    }

    @Override
    public Object visit(ASTOrderClause node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("ORDER BY ");
      node.childrenAccept(this, data);
      sb.append('\n');
      return data;
    }

    @Override
    public Object visit(ASTGroupCondition node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append('(');
      node.jjtGetChild(0).jjtAccept(this, data);
      if (node.jjtGetNumChildren() == 2) {
        sb.append(" AS ");
        node.jjtGetChild(1).jjtAccept(this, data);
      }
      sb.append(")\n");
      return data;
    }

    @Override
    public Object visit(ASTHavingClause node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("HAVING ");
      node.childrenAccept(this, data);
      sb.append('\n');
      return data;
    }

    @Override
    public Object visit(ASTOrderCondition node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      if (!node.isAscending()) {
        sb.append("DESC");
      }
      sb.append('(');
      node.childrenAccept(this, data);
      sb.append(")\n");
      return data;
    }

    @Override
    public Object visit(ASTLimit node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("LIMIT ").append(node.getValue()).append('\n');
      return data;
    }

    @Override
    public Object visit(ASTOffset node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("OFFSET ").append(node.getValue()).append('\n');
      return data;
    }

    @Override
    public Object visit(ASTGraphPatternGroup node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("{\n");
      node.childrenAccept(this, data);
      sb.append("}\n");
      return data;
    }

    @Override
    public Object visit(ASTOptionalGraphPattern node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("OPTIONAL {\n");
      node.childrenAccept(this, data);
      sb.append("}\n");
      return data;
    }

    @Override
    public Object visit(ASTGraphGraphPattern node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("GRAPH ");
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTUnionGraphPattern node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      node.jjtGetChild(0).jjtAccept(this, data);
      sb.append("UNION ");
      node.jjtGetChild(1).jjtAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTMinusGraphPattern node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("MINUS ");
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTServiceGraphPattern node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("SERVICE ");
      if (node.isSilent()) {
        sb.append("SILENT ");
      }
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTTriplesSameSubject node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("  ");
      node.childrenAccept(this, data);
      sb.append(".\n");
      return data;
    }

    @Override
    public Object visit(ASTPropertyList node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      while (true) {
        node.jjtGetChild(0).jjtAccept(this, data); // predicate
        // objects
        final List<Node> objects = node.getObjectList().jjtGetChildren();
        for (int i = 0; i < objects.size(); i++) {
          objects.get(i).jjtAccept(this, data);
          if (i + 1 != objects.size()) {
            sb.append(',');
          }
        }
        node = node.getNextPropertyList();
        if (node != null) {
          sb.append(';');
        } else {
          break;
        }
      }
      return data;
    }

    @Override
    public Object visit(ASTTriplesSameSubjectPath node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("  ");
      // subject
      node.jjtGetChild(0).jjtAccept(this, data);
      // property list path
      ASTPropertyListPath plp = node.jjtGetChild(ASTPropertyListPath.class);
      while (true) {
        plp.jjtGetChild(0).jjtAccept(this, data); // predicate(s)
        // objects
        final List<Node> objects = plp.getObjectList().jjtGetChildren();
        for (int i = 0; i < objects.size(); i++) {
          objects.get(i).jjtAccept(this, data);
          if (i + 1 != objects.size()) {
            sb.append(',');
          }
        }
        plp = plp.getNextPropertyList();
        if (plp != null) {
          sb.append(';');
        } else {
          break;
        }
      }
      sb.append(".\n");
      return data;
    }

    @Override
    public Object visit(ASTPathAlternative node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      node.jjtGetChild(0).jjtAccept(this, data);
      for (int i = 1; i < node.jjtGetNumChildren(); i++) {
        sb.append('|');
        node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
    }

    @Override
    public Object visit(ASTPathSequence node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      node.jjtGetChild(0).jjtAccept(this, data);
      for (int i = 1; i < node.jjtGetNumChildren(); i++) {
        sb.append('/');
        node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
    }

    @Override
    public Object visit(ASTPathElt node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      if (node.isInverse()) {
        sb.append('^');
      }
      // PathPrimary
      if (node.isNegatedPropertySet()) {
        sb.append('!');
        final List<ASTPathOneInPropertySet> set = node.jjtGetChildren(ASTPathOneInPropertySet.class);
        if (set.size() == 1) {
          set.get(0).jjtAccept(this, data);
        } else {
          set.get(0).jjtAccept(this, data);
          for (int i = 1; i < set.size(); i++) {
            sb.append('|');
            set.get(i).jjtAccept(this, data);
          }
        }
      } else if (node.isNestedPath()) {
        sb.append('(');
        node.jjtGetChild(ASTPathAlternative.class).jjtAccept(this, data);
        sb.append(')');
      } else {
        node.jjtGetChild(0).jjtAccept(this, data);
      }
      // PathMod
      final ASTPathMod pathMod = node.getPathMod();
      if (pathMod != null) {
        pathMod.jjtAccept(this, data);
      }
      return data;
    }

    @Override
    public Object visit(ASTPathOneInPropertySet node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      if (node.isInverse()) {
        sb.append('^');
      }
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTPathMod node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append('{').append(node.getLowerBound());
      if (node.getUpperBound() != Long.MIN_VALUE) {
        sb.append(',').append(node.getUpperBound());
      }
      sb.append('}');
      return data;
    }

    @Override
    public Object visit(ASTCollection node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append('(');
      node.childrenAccept(this, data);
      sb.append(')');
      return data;
    }

    @Override
    public Object visit(ASTBlankNodePropertyList node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append('[');
      node.childrenAccept(this, data);
      sb.append(']');
      return data;
    }

    @Override
    public Object visit(ASTIRI node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append('<').append(node.getValue()).append("> ");
      return data;
    }

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append('?').append(node.getName()).append(' ');
      return data;
    }

    @Override
    public Object visit(ASTOr node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append(" OR ");
      return data;
    }

    @Override
    public Object visit(ASTAnd node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append(" AND ");
      return data;
    }

    @Override
    public Object visit(ASTCompare node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append(' ').append(node.getOperator().getSymbol()).append(' ');
      return data;
    }

    @Override
    public Object visit(ASTNot node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append('!');
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTNumericLiteral node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append('"').append(node.getValue()).append("\"^^<")
      .append(node.getDatatype().stringValue()).append("> ");
      return data;
    }

    @Override
    public Object visit(ASTCount node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("COUNT(");
      if (node.isDistinct()) {
        sb.append("DISTINCT ");
      }
      if (node.isWildcard()) {
        sb.append('*');
      } else {
        node.childrenAccept(this, data);
      }
      sb.append(") ");
      return data;
    }

    @Override
    public Object visit(ASTSum node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("SUM(");
      if (node.isDistinct()) {
        sb.append("DISTINCT ");
      }
      node.childrenAccept(this, data);
      sb.append(") ");
      return data;
    }

    @Override
    public Object visit(ASTMin node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("MIN(");
      if (node.isDistinct()) {
        sb.append("DISTINCT ");
      }
      node.childrenAccept(this, data);
      sb.append(") ");
      return data;
    }

    @Override
    public Object visit(ASTMax node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("MAX(");
      if (node.isDistinct()) {
        sb.append("DISTINCT ");
      }
      node.childrenAccept(this, data);
      sb.append(") ");
      return data;
    }

    @Override
    public Object visit(ASTAvg node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("AVG(");
      if (node.isDistinct()) {
        sb.append("DISTINCT ");
      }
      node.childrenAccept(this, data);
      sb.append(") ");
      return data;
    }

    @Override
    public Object visit(ASTSample node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("SAMPLE(");
      if (node.isDistinct()) {
        sb.append("DISTINCT ");
      }
      node.childrenAccept(this, data);
      sb.append(") ");
      return data;
    }

    @Override
    public Object visit(ASTGroupConcat node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("GROUP_CONCAT(");
      if (node.isDistinct()) {
        sb.append("DISTINCT ");
      }
      node.jjtGetChild(0).jjtAccept(this, data);
      if (node.jjtGetNumChildren() == 2) {
        sb.append("; separator = ");
        node.jjtGetChild(1).jjtAccept(this, data);
      }
      sb.append(") ");
      return data;
    }

    @Override
    public Object visit(ASTMD5 node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("MD5", node, data);
      return data;
    }

    @Override
    public Object visit(ASTSHA1 node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("SHA1", node, data);
      return data;
    }

    @Override
    public Object visit(ASTSHA224 node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("SHA224", node, data);
      return data;
    }

    @Override
    public Object visit(ASTSHA256 node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("SHA256", node, data);
      return data;
    }

    @Override
    public Object visit(ASTSHA384 node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("SHA384", node, data);
      return data;
    }

    @Override
    public Object visit(ASTSHA512 node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("SHA512", node, data);
      return data;
    }

    @Override
    public Object visit(ASTNow node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("RAND", null, data);
      return data;
    }

    @Override
    public Object visit(ASTYear node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("YEAR", node, data);
      return data;
    }

    @Override
    public Object visit(ASTMonth node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("MONTH", node, data);
      return data;
    }

    @Override
    public Object visit(ASTDay node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("DAY", node, data);
      return data;
    }

    @Override
    public Object visit(ASTHours node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("HOURS", node, data);
      return data;
    }

    @Override
    public Object visit(ASTMinutes node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("MINUTES", node, data);
      return data;
    }

    @Override
    public Object visit(ASTSeconds node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("SECONDS", node, data);
      return data;
    }

    @Override
    public Object visit(ASTTimezone node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("TIMEZONE", node, data);
      return data;
    }

    @Override
    public Object visit(ASTTz node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("TZ", node, data);
      return data;
    }

    @Override
    public Object visit(ASTRand node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("RAND", null, data);
      return data;
    }

    @Override
    public Object visit(ASTAbs node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("ABS", node, data);
      return data;
    }

    @Override
    public Object visit(ASTCeil node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("CEIL", node, data);
      return data;
    }

    @Override
    public Object visit(ASTFloor node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("FLOOR", node, data);
    return data;
    }

    @Override
    public Object visit(ASTRound node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("ROUND", node, data);
      return data;
    }

    @Override
    public Object visit(ASTSubstr node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("SUBSTR", node, data);
      return data;
    }

    @Override
    public Object visit(ASTStrLen node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("STRLEN", node, data);
      return data;
    }

    @Override
    public Object visit(ASTUpperCase node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("UCASE", node, data);
      return data;
    }

    @Override
    public Object visit(ASTLowerCase node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("LCASE", node, data);
      return data;
    }

    @Override
    public Object visit(ASTStrStarts node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("STRSTARTS", node, data);
      return data;
    }

    @Override
    public Object visit(ASTStrEnds node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("STRENDS", node, data);
      return data;
    }

    @Override
    public Object visit(ASTStrBefore node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("STRBEFORE", node, data);
      return data;
    }

    @Override
    public Object visit(ASTStrAfter node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("STRAFTER", node, data);
      return data;
    }

    @Override
    public Object visit(ASTReplace node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("REPLACE", node, data);
      return data;
    }

    @Override
    public Object visit(ASTConcat node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("CONCAT", node, data);
      return data;
    }

    @Override
    public Object visit(ASTContains node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("CONTAINS", node, data);
      return data;
    }

    @Override
    public Object visit(ASTEncodeForURI node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("ENCODE_FOR_URI", node, data);
      return data;
    }

    @Override
    public Object visit(ASTIf node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("IF", node, data);
      return data;
    }

    @Override
    public Object visit(ASTIn node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("IN", node, data);
      return data;
    }

    @Override
    public Object visit(ASTNotIn node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("NOT IN", node, data);
      return data;
    }

    @Override
    public Object visit(ASTCoalesce node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("COALESCE", node, data);
      return data;
    }

    @Override
    public Object visit(ASTStr node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("STR", node, data);
      return data;
    }

    @Override
    public Object visit(ASTLang node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("LANG", node, data);
      return data;
    }

    @Override
    public Object visit(ASTLangMatches node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("LANGMATCHES", node, data);
      return data;
    }

    @Override
    public Object visit(ASTDatatype node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("DATATYPE", node, data);
      return data;
    }

    @Override
    public Object visit(ASTBound node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("BOUND", node, data);
      return data;
    }

    @Override
    public Object visit(ASTSameTerm node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("SAMETERM", node, data);
      return data;
    }

    @Override
    public Object visit(ASTIsIRI node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("isIRI", node, data);
      return data;
    }

    @Override
    public Object visit(ASTIsBlank node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("isBLANK", node, data);
      return data;
    }

    @Override
    public Object visit(ASTIsLiteral node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("isLITERAL", node, data);
      return data;
    }

    @Override
    public Object visit(ASTIsNumeric node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("isNUMERIC", node, data);
      return data;
    }

    @Override
    public Object visit(ASTBNodeFunc node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("BNODE", node, data);
      return data;
    }

    @Override
    public Object visit(ASTIRIFunc node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("IRI", node, data);
      return data;
    }

    @Override
    public Object visit(ASTStrDt node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("STRDT", node, data);
      return data;
    }

    @Override
    public Object visit(ASTStrLang node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("STRLANG", node, data);
      return data;
    }

    @Override
    public Object visit(ASTBind node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("BIND(");
      node.jjtGetChild(0).jjtAccept(this, data);
      sb.append(" AS ");
      node.jjtGetChild(1).jjtAccept(this, data);
      sb.append(")");
      return data;
    }

    @Override
    public Object visit(ASTRegexExpression node, Object data)
    throws VisitorException {
      functionWithCommasSeparator("REGEX", node, data);
      return data;
    }

    @Override
    public Object visit(ASTExistsFunc node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("EXISTS ");
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTNotExistsFunc node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("NOT EXISTS ");
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTRDFLiteral node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append('"').append(node.getLabel().getValue()).append("\" ");
      final ASTIRI datatype = node.getDatatype();
      if (datatype != null) {
        sb.append("^^<").append(datatype.getValue()).append('>');
      } else if (node.getLang() != null) {
        sb.append('@').append(node.getLang());
      }
      return data;
    }

    @Override
    public Object visit(ASTTrue node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("TRUE");
      return data;
    }

    @Override
    public Object visit(ASTFalse node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("FALSE");
      return data;
    }

    @Override
    public Object visit(ASTString node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append(node.getValue()).append(' ');
      return data;
    }

    @Override
    public Object visit(ASTQName node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append(node.getValue()).append(' ');
      return data;
    }

    @Override
    public Object visit(ASTBlankNode node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("_:").append(node.getID()).append(' ');
      return data;
    }

    @Override
    public Object visit(ASTGraphRefAll node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      if (node.jjtGetNumChildren() != 0) { // GraphRef
        sb.append("GRAPH ");
        node.childrenAccept(this, data);
      } else if (node.isDefault()) {
        sb.append("DEFAULT ");
      } else if (node.isNamed()) {
        sb.append("NAMED ");
      } else { // ALL
        sb.append("ALL ");
      }
      return data;
    }

    @Override
    public Object visit(ASTGraphOrDefault node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      if (node.jjtGetNumChildren() != 0) { // GraphRef
        sb.append("GRAPH ");
        node.childrenAccept(this, data);
      } else {
        sb.append("DEFAULT ");
      }
      return data;
    }

    @Override
    public Object visit(ASTQuadsNotTriples node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("GRAPH ");
      node.jjtGetChild(0).jjtAccept(this, data);
      sb.append('{');
      for (int i = 1; i < node.jjtGetNumChildren(); i++) {
        node.jjtGetChild(i).jjtAccept(this, data);
      }
      sb.append("}\n");
      return data;

    }

    @Override
    public Object visit(ASTLoad node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("LOAD ");
      if (node.isSilent()) {
        sb.append("SILENT ");
      }
      node.jjtGetChild(0).jjtAccept(this, data);
      if (node.jjtGetNumChildren() == 2) {
        sb.append("INTO GRAPH ");
        node.jjtGetChild(1).jjtAccept(this, data);
      }
      return data;
    }

    @Override
    public Object visit(ASTClear node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("CLEAR ");
      if (node.isSilent()) {
        sb.append("SILENT ");
      }
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTDrop node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("DROP ");
      if (node.isSilent()) {
        sb.append("SILENT ");
      }
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTAdd node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("ADD ");
      if (node.isSilent()) {
        sb.append("SILENT ");
      }
      node.jjtGetChild(0).jjtAccept(this, data);
      sb.append("TO ");
      node.jjtGetChild(1).jjtAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTMove node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("MOVE ");
      if (node.isSilent()) {
        sb.append("SILENT ");
      }
      node.jjtGetChild(0).jjtAccept(this, data);
      sb.append("TO ");
      node.jjtGetChild(1).jjtAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTCopy node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("COPY ");
      if (node.isSilent()) {
        sb.append("SILENT ");
      }
      node.jjtGetChild(0).jjtAccept(this, data);
      sb.append("TO ");
      node.jjtGetChild(1).jjtAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTCreate node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("CREATE ");
      if (node.isSilent()) {
        sb.append("SILENT ");
      }
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(ASTInsertData node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("INSERT DATA {");
      node.childrenAccept(this, data);
      sb.append("} ");
      return data;
    }

    @Override
    public Object visit(ASTDeleteData node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("DELETE DATA {");
      node.childrenAccept(this, data);
      sb.append("} ");
      return data;
    }

    @Override
    public Object visit(ASTDeleteWhere node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("DELETE WHERE {");
      node.childrenAccept(this, data);
      sb.append("} ");
      return data;
    }

    @Override
    public Object visit(ASTDeleteClause node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("DELETE {");
      node.childrenAccept(this, data);
      sb.append("} ");
      return data;
    }

    @Override
    public Object visit(ASTInsertClause node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append("INSERT {");
      node.childrenAccept(this, data);
      sb.append("} ");
      return data;
    }

    @Override
    public Object visit(ASTModify node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      final ASTIRI withClause = node.getWithClause();
      if (withClause != null) {
        sb.append("WITH ");
        withClause.jjtAccept(this, data);
      }

      final ASTDeleteClause deleteClause = node.getDeleteClause();
      final ASTInsertClause insertClause = node.getInsertClause();
      if (deleteClause != null) {
        deleteClause.jjtAccept(this, data);
      }
      if (insertClause != null) {
        insertClause.jjtAccept(this, data);
      }

      for (ASTDatasetClause dc : node.getDatasetClauseList()) {
        sb.append("USING ");
        if (dc.isNamed()) {
          sb.append("NAMED ");
        }
        dc.childrenAccept(this, data);
      }
      sb.append("WHERE ");
      node.getWhereClause().childrenAccept(this, data);
      return data;
    }

    private void functionWithCommasSeparator(String name, Node node, Object data)
    throws VisitorException {
      final StringBuilder sb = (StringBuilder) data;

      sb.append(name + "(");
      if (node != null) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) { // there can be no child
          node.jjtGetChild(i).jjtAccept(this, data);
          if (i + 1 != node.jjtGetNumChildren()) {
            sb.append(',');
          }
        }
      }
      sb.append(")");
    }

  }

}
