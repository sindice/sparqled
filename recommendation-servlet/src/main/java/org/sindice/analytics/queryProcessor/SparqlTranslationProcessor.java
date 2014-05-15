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

import org.openrdf.model.URI;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.DatasetDeclProcessor;
import org.openrdf.sindice.query.parser.sparql.ast.ASTBasicGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTRDFLiteral;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.Node;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;
import org.sindice.analytics.queryProcessor.RecommendationQuery.Edge;
import org.sindice.analytics.queryProcessor.RecommendationQuery.Type;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.sindice.core.analytics.commons.summary.DatasetLabel;
import org.sindice.core.analytics.commons.util.URIUtil;

/**
 * 
 */
public final class SparqlTranslationProcessor {

  private final static IsLeaf      isLeaf                = new IsLeaf();
  private static ASTQueryContainer astQueryContainer;

  private SparqlTranslationProcessor() {
  }

  /**
   * Translate the SPARQL query to a Summary query.
   * @return 
   */
  public static RecommendationQuery process(POFMetadata meta, ASTQueryContainer ast)
  throws MalformedQueryException, VisitorException {
    // replace any dataset URI (graph and FROM) by their second-level domain name
    final Dataset datasets = DatasetDeclProcessor.process(ast);
    final ASTIRI d;

    astQueryContainer = ast;
    if (datasets != null) {
      if (!datasets.getDefaultGraphs().isEmpty() || !datasets.getNamedGraphs().isEmpty()) {
        if (!datasets.getDefaultGraphs().isEmpty() && !datasets.getNamedGraphs().isEmpty()) {
          throw new DGSException("There can be only one default/named graph");
        }
        if (!datasets.getDefaultGraphs().isEmpty() && datasets.getDefaultGraphs().size() != 1) {
          throw new DGSException("There can be only one default/named graph");
        }
        if (!datasets.getNamedGraphs().isEmpty() && datasets.getNamedGraphs().size() != 1) {
          throw new DGSException("There can be only one default/named graph");
        }
      }
      if (!datasets.getDefaultGraphs().isEmpty() && datasets.getNamedGraphs().isEmpty()) {
        d = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
        final String value = datasets.getDefaultGraphs().toArray(new URI[1])[0].stringValue();
        d.setValue(URIUtil.getSndDomainFromUrl(value));
      } else if (datasets.getDefaultGraphs().isEmpty() && !datasets.getNamedGraphs().isEmpty()) {
        d = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
        final String value = datasets.getNamedGraphs().toArray(new URI[1])[0].stringValue();
        d.setValue(URIUtil.getSndDomainFromUrl(value));
      } else {
        d = null;
      }
    } else {
      d = null;
    }

    String filter = null;
    if (meta != null && meta.pofNode.getMetadata() != null) {
      final Object keyword = meta.pofNode.getMetadata(SyntaxTreeBuilder.Keyword);
      final Object prefix = meta.pofNode.getMetadata(SyntaxTreeBuilder.Prefix);
      final Object qname = meta.pofNode.getMetadata(SyntaxTreeBuilder.Qname);

      if (keyword != null) {
        filter = "regex(str(?" + SyntaxTreeBuilder.PointOfFocus + "), \"" + keyword + "\", \"i\")";
      } else if (prefix != null) {
        filter = "regex(str(?" + SyntaxTreeBuilder.PointOfFocus + "), \"^" + prefix + "\", \"i\")";
      } else if (qname != null) {
        filter = "regex(str(?" + SyntaxTreeBuilder.PointOfFocus + "), \"^" + qname + "\", \"i\")";
      }
    }

    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, filter);
    final SparqlTranslationVisitor v = new SparqlTranslationVisitor(rq, d);

    v.visit(ast, null);
    // Change variable name of the POF ressource
    changeToPofResource(rq, v.pofResourceName);
    return rq;
  }

  private static void changeToPofResource(RecommendationQuery rq, String pofResourceName) {
    for (Edge e : rq.edge()) {
      if (e.getResource().equals(pofResourceName)) {
        e.setResource(QueryProcessor.POF_RESOURCE);
      }
    }
    for (Type t : rq.type()) {
      if (t.getResource().equals(pofResourceName)) {
        t.setResource(QueryProcessor.POF_RESOURCE);
      }
    }
  }

  private static class SparqlTranslationVisitor extends ASTVisitorBase {

    private final ASTIRI              datasets;
    private final RecommendationQuery rq;

    private Node                      graphName;

    private String                    pofResourceName;

    public SparqlTranslationVisitor(RecommendationQuery rq, ASTIRI datasets) {
      this.datasets = datasets;
      this.rq = rq;
    }

    private String toString(Node node) {
      return toString(node, false);
    }

    private String toString(Node node, boolean withVarQuestionMark) {
      if (node == null) {
        return null;
      }
      if (node instanceof ASTVar) {
        return (withVarQuestionMark ? "?" : "") +((ASTVar) node).getName();
      } else if (node instanceof ASTIRI) {
        return "<" + NTriplesUtil.escapeString(((ASTIRI) node).getValue()) + ">";
      } else if (node instanceof ASTRDFLiteral) {
        final ASTRDFLiteral lit = (ASTRDFLiteral) node;
        final StringBuilder sb = new StringBuilder();

        sb.append('"').append(lit.getLabel().getValue()).append("\" ");
        final ASTIRI datatype = lit.getDatatype();
        if (datatype != null) {
          sb.append("^^<").append(datatype.getValue()).append('>');
        } else if (lit.getLang() != null) {
          sb.append('@').append(lit.getLang());
        }
        return sb.toString();
      } else {
        throw new IllegalArgumentException("Received unexpected node: " + node);
      }
    }

    @Override
    public Object visit(ASTGraphGraphPattern node, Object data)
    throws VisitorException {
      // FIXME correct recommendation across graphs

      graphName = null;
      if (node.jjtGetChild(0) instanceof ASTIRI) {
        graphName = (ASTIRI) node.jjtGetChild(0);
      }
      data = super.visit(node, data);
      graphName = null;
      return data;
    }

    @Override
    public Object visit(ASTBasicGraphPattern node, Object data)
    throws VisitorException {
      final Node dataset;

      // set the second-level domain dataset label
      if (graphName != null) {
        dataset = setDatasetLabel(graphName);
      } else {
        dataset = setDatasetLabel(datasets);
      }

      for (ASTTriplesSameSubjectPath triple : node.jjtGetChildren(ASTTriplesSameSubjectPath.class)) {
        final Node verb = triple.jjtGetChild(1).jjtGetChild(0);
        if (!(verb instanceof ASTVar)) {
          final ASTIRI verbIRI = (ASTIRI) verb.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
          if (AnalyticsClassAttributes.isClass(verbIRI.getValue())) {
            classTriplePatternToDGSedge(dataset, triple);
          } else {
            triplePatternToDGSedge(dataset, triple, verbIRI);
          }
        } else {
          triplePatternToDGSedge(dataset, triple, verb);
        }
      }
      return super.visit(node, data);
    }

    private Node setDatasetLabel(Node label)
    throws DGSException {
      if (label instanceof ASTIRI) {
        switch (DataGraphSummaryVocab.DATASET_LABEL_DEF) {
          case SECOND_LEVEL_DOMAIN:
            final ASTIRI astIri = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
            final String iri = ((ASTIRI) label).getValue();
            final String snd = URIUtil.getSndDomainFromUrl(iri);
            astIri.setValue(DataGraphSummaryVocab.DOMAIN_URI_PREFIX + (snd == null ? iri : snd));
            return astIri;
          case PLAIN:
            return label;
          default:
            throw new DGSException(new EnumConstantNotPresentException(DatasetLabel.class,
              DataGraphSummaryVocab.DATASET_LABEL_DEF.toString()));
        }
      }
      return label;
    }

    private void triplePatternToDGSedge(Node dataset, ASTTriplesSameSubjectPath node, Node verb)
    throws VisitorException {
      // Check if the object variable is a leaf or not
      isLeaf.setVar((ASTVar) node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0));
      final boolean isLeafNode = (Boolean) isLeaf.visit(astQueryContainer, true);

      final Edge edge = new Edge();
      final String s = SparqlVarGenerator.getVar("dgs");

      edge.setResource(s);

      final ASTVar source = (ASTVar) node.jjtGetChild(0);
      edge.setSource(source.getName());

      edge.setPredicate(toString(verb, true));

      if (!isLeafNode) {
        final ASTVar target = (ASTVar) node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0);
        edge.setTarget(target.getName());
      }

      // edge origin
      if (dataset != null) {
        edge.setDataset(toString(dataset, true));
        // FIXME: this method constrains that the subject of the triple pattern is a node in the dataset. Why not the object ?
      }

      if (edge.pof()) {
        rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
        rq.addProjection(QueryProcessor.POF_RESOURCE);
        rq.addProjection(QueryProcessor.CARDINALITY_VAR);

        pofResourceName = s;
      }
      rq.edge().add(edge);
    }

    private void classTriplePatternToDGSedge(Node dataset, ASTTriplesSameSubjectPath node) {
      final Type type = new Type();
      final String s = toString(node.jjtGetChild(0));

      type.setResource(s);

      // Class origin
      if (dataset != null) { // the dataset is the POF
        type.setDataset(toString(dataset, true));
        // FIXME: review how the POF on the named graph is handled
      }

      final Node clazz = node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0);
      /*
       * Class Recommendation
       */
      if (clazz instanceof ASTVar && ((ASTVar) clazz).getName().equals(SyntaxTreeBuilder.PointOfFocus)) {
        rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
        rq.addProjection(QueryProcessor.POF_RESOURCE);
        rq.addProjection(QueryProcessor.CARDINALITY_VAR);
        rq.addProjection(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR);
        rq.addProjection(QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR);
        pofResourceName = s;
      }
      type.setType(toString(clazz, true));
      rq.type().add(type);
    }

  }

  private static class IsLeaf extends ASTVisitorBase {

    private ASTVar var;

    public void setVar(ASTVar var) {
      this.var = var;
    }

    @Override
    public Object visit(ASTTriplesSameSubjectPath node, Object data)
        throws VisitorException {
      /*
       * With the content being removed, the object of a triple pattern is a variable.
       * If it is not, then it has to be a class triple pattern. This class works only
       * with the first case
       */
      // subject
      if (node.jjtGetChild(0) instanceof ASTVar &&
          ((ASTVar) node.jjtGetChild(0)).getName().equals(var.getName())) {
        data = false;
      }
      // predicate
      if (node.jjtGetChild(1).jjtGetChild(0) instanceof ASTVar &&
          ((ASTVar) node.jjtGetChild(1).jjtGetChild(0)).getName().equals(var.getName())) {
        data = false;
      }
      // object
      if (node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0) instanceof ASTVar &&
          node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0) != var &&
          ((ASTVar) node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0)).getName().equals(var.getName())) {
        data = false;
      }
      return super.visit(node, data);
    }

  }

}
