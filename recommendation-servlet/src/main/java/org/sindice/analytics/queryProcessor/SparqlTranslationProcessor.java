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
 * @author Campinas Stephane [ 21 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTBasicGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTConstraint;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphPatternGroup;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIsLiteral;
import org.openrdf.sindice.query.parser.sparql.ast.ASTNot;
import org.openrdf.sindice.query.parser.sparql.ast.ASTProjectionElem;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTRDFLiteral;
import org.openrdf.sindice.query.parser.sparql.ast.ASTSelect;
import org.openrdf.sindice.query.parser.sparql.ast.ASTSelectQuery;
import org.openrdf.sindice.query.parser.sparql.ast.ASTString;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.Node;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.AnalyticsVocab;
import org.sindice.core.analytics.commons.summary.DatasetLabel;
import org.sindice.core.analytics.commons.util.Hash;
import org.sindice.core.analytics.commons.util.URIUtil;

/**
 * 
 */
public final class SparqlTranslationProcessor {

  public static final String       BLANK_NODE_COLLECTION = "dummy class: " + Long.toString(Hash.getLong("dummy class")).replace('-', 'n');

  private final static IsLeaf      isLeaf                = new IsLeaf();
  private static ASTQueryContainer astQueryContainer;

  private SparqlTranslationProcessor() {
  }

  /**
   * Translate the SPARQL query to a Summary query.
   * @param ast
   * @return
   * @throws MalformedQueryException
   * @throws VisitorException
   */
  public static void process(ASTQueryContainer ast)
  throws MalformedQueryException, VisitorException {
    // get the default datasets + set the DGS graph in the clause
    // replace any dataset URI (graph and FROM) by their second-level domain name
    final Dataset datasets = DGSDatasetClauseProcessor.process(ast);
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

    final ChangeToPofRessource change = new ChangeToPofRessource();
    final GraphGraphPatternRemoval g = new GraphGraphPatternRemoval();
    final SparqlTranslationVisitor v = new SparqlTranslationVisitor(d);

    final List<String> pofMetadata = (List<String>) v.visit(ast, new ArrayList<String>());
    g.visit(ast, null);
    // Change variable name of the POF ressource
    change.visit(ast, v.pofResourceName);
    // Add POF metadata to the SELECT clause
    addPofMetadata(ast, pofMetadata);
  }

  private static class ChangeToPofRessource extends ASTVisitorBase {

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      final String pofResource = (String) data;

      if (node.getName().equals(pofResource)) {
        node.setName(QueryProcessor.POF_RESOURCE);
      }
      return super.visit(node, data);
    }

  }

  private static void addPofMetadata(ASTQueryContainer ast, final List<String> pofMetadata) {
    if (pofMetadata.isEmpty()) {
      // Shouldn't happen at this stage
      return;
    }

    /*
     * Update the Projection elements with the subject connected to the POF.
     * This is to make sure that, in association with DISTINCT, a node's label
     * appears only one time within the recommendation list.
     * 
     * Add also the variables that get the class attributes in case of a CLASS
     * recommendation
     */
    final ASTSelect select = ((ASTSelectQuery) ast.getQuery()).getSelect();
    select.setDistinct(true);

    // insensitive order, so that writing JUnit tests is easier
    Collections.sort(pofMetadata, String.CASE_INSENSITIVE_ORDER);
    for (String meta : pofMetadata) {
      final ASTProjectionElem p = new ASTProjectionElem(SyntaxTreeBuilderTreeConstants.JJTPROJECTIONELEM);
      final ASTVar var = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
      var.setName(meta);
      p.jjtAppendChild(var);
      select.jjtAppendChild(p);
    }
  }

  private static class GraphGraphPatternRemoval extends ASTVisitorBase {

    @Override
    public Object visit(ASTGraphGraphPattern node, Object data)
    throws VisitorException {
      if (node.jjtGetNumChildren() == 2) {
        node.jjtReplaceWith(node.jjtGetChild(1));
      }
      return super.visit(node, data);
    }

  }

  private static class SparqlTranslationVisitor extends ASTVisitorBase {

    private final ASTIRI datasets;

    private boolean      graphNamePOF = false;
    private Node         graphName    = null;

    private String       pofResourceName;

    public SparqlTranslationVisitor(ASTIRI datasets) {
      this.datasets = datasets;
    }

    @Override
    public Object visit(ASTGraphGraphPattern node, Object data)
    throws VisitorException {
      graphName = null;
      if (node.jjtGetChild(0) instanceof ASTIRI) {
        graphName = (ASTIRI) node.jjtGetChild(0);
      } else if (node.jjtGetChild(0) instanceof ASTVar &&
                 ((ASTVar) node.jjtGetChild(0)).getName().equals(SyntaxTreeBuilder.PointOfFocus)) {
        graphName = (ASTVar) node.jjtGetChild(0);
        graphNamePOF = true;
        if (node.jjtGetNumChildren() != 2) { // pof on the graph name
          final List<String> pofMetadata = (List<String>) data;
          final ASTGraphPatternGroup gpg = new ASTGraphPatternGroup(SyntaxTreeBuilderTreeConstants.JJTGRAPHPATTERNGROUP);
          final ASTBasicGraphPattern bgp = new ASTBasicGraphPattern(SyntaxTreeBuilderTreeConstants.JJTBASICGRAPHPATTERN);

          final ASTIRI label = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
          label.setValue(AnalyticsVocab.LABEL.toString());
          final ASTIRI source = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
          source.setValue(AnalyticsVocab.EDGE_SOURCE.toString());
          final ASTIRI origin = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
          origin.setValue(AnalyticsVocab.EDGE_PUBLISHED_IN.toString());
          final ASTIRI cardinality = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
          cardinality.setValue(AnalyticsVocab.CARDINALITY.toString());
          final ASTVar pofCardinality = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
          pofCardinality.setName(QueryProcessor.CARDINALITY_VAR);

          final ASTVar pofResource = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
          pofResource.setName(QueryProcessor.POF_RESOURCE);

          final ASTTriplesSameSubjectPath t1 = ASTProcessorUtil.createTriple(pofResource, cardinality, pofCardinality);
          final ASTTriplesSameSubjectPath t3 = ASTProcessorUtil.createTriple(pofResource, origin, graphName);

          bgp.jjtAppendChild(t1);
          bgp.jjtAppendChild(t3);
          gpg.jjtAppendChild(bgp);
          node.jjtAppendChild(gpg);

          pofResourceName = QueryProcessor.POF_RESOURCE; // the resource here is a newly created bgp
          pofMetadata.add(QueryProcessor.POF_RESOURCE);
          pofMetadata.add(QueryProcessor.CARDINALITY_VAR);
          return data;
        }
      }
      data = super.visit(node, data);
      graphName = null;
      return data;
    }

    @Override
    public Object visit(ASTBasicGraphPattern node, Object data)
    throws VisitorException {
      final ASTBasicGraphPattern bgpDGS = new ASTBasicGraphPattern(SyntaxTreeBuilderTreeConstants.JJTBASICGRAPHPATTERN);
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
            data = classTriplePatternToDGSedge(data, dataset, bgpDGS, triple);
          } else {
            data = triplePatternToDGSedge(data, dataset, bgpDGS, triple, verbIRI);
          }
        } else {
          data = triplePatternToDGSedge(data, dataset, bgpDGS, triple, verb);
        }
      }
      node.jjtReplaceWith(bgpDGS);
      return super.visit(node, data);
    }

    private Node setDatasetLabel(Node label)
    throws DGSException {
      if (label instanceof ASTIRI) {
        switch (AnalyticsVocab.DATASET_LABEL_DEF) {
          case SECOND_LEVEL_DOMAIN:
            final ASTIRI astIri = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
            final String iri = ((ASTIRI) label).getValue();
            final String snd = URIUtil.getSndDomainFromUrl(iri);
            astIri.setValue(AnalyticsVocab.DOMAIN_URI_PREFIX + (snd == null ? iri : snd));
            return astIri;
          case PLAIN:
            return label;
          default:
            EnumConstantNotPresentException dl = new EnumConstantNotPresentException(DatasetLabel.class, AnalyticsVocab.DATASET_LABEL_DEF.toString());
            throw new DGSException(dl);
        }
      }
      return label;
    }

    private Object triplePatternToDGSedge(Object data, Node dataset, ASTBasicGraphPattern bgp, ASTTriplesSameSubjectPath node, Node verb)
    throws VisitorException {
      final List<String> pofMetadata = (List<String>) data;
      final ASTVar s = ASTVarGenerator.getASTVar("dgs");
      final ASTIRI label = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
      label.setValue(AnalyticsVocab.LABEL.toString());
      final ASTIRI source = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
      source.setValue(AnalyticsVocab.EDGE_SOURCE.toString());
      final ASTIRI target = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
      target.setValue(AnalyticsVocab.EDGE_TARGET.toString());
      final ASTIRI origin = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
      origin.setValue(AnalyticsVocab.EDGE_PUBLISHED_IN.toString());

      // Check if the object variable is a leaf or not
      isLeaf.setVar((ASTVar) node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0));
      final boolean isLeafNode = (Boolean) isLeaf.visit(astQueryContainer, true);

      final ASTTriplesSameSubjectPath t1 = ASTProcessorUtil.createTriple(s, source, node.jjtGetChild(0));
      final ASTTriplesSameSubjectPath t3 = ASTProcessorUtil.createTriple(s, label, verb);
      final ASTTriplesSameSubjectPath t2;
      if (isLeafNode) {
        final ASTRDFLiteral dummyNode = new ASTRDFLiteral(SyntaxTreeBuilderTreeConstants.JJTRDFLITERAL);
        final ASTString dummyString = new ASTString(SyntaxTreeBuilderTreeConstants.JJTSTRING);
        dummyNode.jjtAppendChild(dummyString);
        dummyString.setValue(BLANK_NODE_COLLECTION);
        t2 = ASTProcessorUtil.createTriple(s, target, dummyNode);
      } else {
        final ASTVar targetNode = (ASTVar) node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0);
        t2 = ASTProcessorUtil.createTriple(s, target, targetNode);
        // Add Filters to Dummy TARGET solutions, if the target variable is reused in the query
        final ASTConstraint cst = new ASTConstraint(SyntaxTreeBuilderTreeConstants.JJTCONSTRAINT);
        final ASTIsLiteral isLiteral = new ASTIsLiteral(SyntaxTreeBuilderTreeConstants.JJTISLITERAL);
        final ASTNot isNot = new ASTNot(SyntaxTreeBuilderTreeConstants.JJTNOT);
        cst.jjtAppendChild(isNot);
        isNot.jjtAppendChild(isLiteral);
        isLiteral.jjtAppendChild(targetNode);
        bgp.jjtAppendChild(cst);
      }

      bgp.jjtAppendChild(t1);
      bgp.jjtAppendChild(t2);
      bgp.jjtAppendChild(t3);

      // Predicate recommendation
      if (verb instanceof ASTVar && ((ASTVar) verb).getName().equals(SyntaxTreeBuilder.PointOfFocus)) {
        // the Subject of the POF
        pofMetadata.add(QueryProcessor.POF_RESOURCE);
        pofResourceName = s.getName();

        final ASTIRI cardinality = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
        cardinality.setValue(AnalyticsVocab.CARDINALITY.toString());
        final ASTVar pofCardinality = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
        pofCardinality.setName(QueryProcessor.CARDINALITY_VAR);

        final ASTTriplesSameSubjectPath t5 = ASTProcessorUtil.createTriple(s, cardinality, pofCardinality);
        bgp.jjtAppendChild(t5);
        pofMetadata.add(QueryProcessor.CARDINALITY_VAR);
      }
      // edge origin
      if (dataset != null) {
        final ASTTriplesSameSubjectPath t4 = ASTProcessorUtil.createTriple(s, origin, dataset);
        bgp.jjtAppendChild(t4);
        if (datasetEdgePOF(data, node.jjtGetChild(0), dataset, bgp)) {
          pofMetadata.add(QueryProcessor.POF_RESOURCE);
          pofResourceName = ((ASTVar) node.jjtGetChild(0)).getName();
        }
      }
      return pofMetadata;
    }

    private Object classTriplePatternToDGSedge(Object data, Node dataset, ASTBasicGraphPattern bgp, ASTTriplesSameSubjectPath node) {
      final List<String> pofMetadata = (List<String>) data;
      final ASTIRI label = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
      label.setValue(AnalyticsVocab.LABEL.toString());
      final ASTIRI origin = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
      origin.setValue(AnalyticsVocab.DOMAIN_URI.toString());

      // Class origin
      if (dataset != null) { // the dataset is the POF
        final ASTTriplesSameSubjectPath t1 = ASTProcessorUtil.createTriple(node.jjtGetChild(0), origin, dataset);
        bgp.jjtAppendChild(t1);
        if (datasetClassPOF(data, node.jjtGetChild(0), dataset, bgp)) {
          pofMetadata.add(QueryProcessor.POF_RESOURCE);
          pofResourceName = ((ASTVar) node.jjtGetChild(0)).getName();
        }
      }

      final Node subject = node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0);
      /*
       * Class Recommendation
       */
      if (subject instanceof ASTVar && ((ASTVar) subject).getName().equals(SyntaxTreeBuilder.PointOfFocus)) {
        // the Subject of the POF
        pofMetadata.add(QueryProcessor.POF_RESOURCE);
        pofResourceName = ((ASTVar) node.jjtGetChild(0)).getName();

        final ASTVar varLabel = ASTVarGenerator.getASTVar("dgs");
        // POF on the class URI
        final ASTTriplesSameSubjectPath t1 = ASTProcessorUtil.createTriple(node.jjtGetChild(0), label, varLabel);
        final ASTTriplesSameSubjectPath t2 = ASTProcessorUtil.createTriple(varLabel, label, subject);
        bgp.jjtAppendChild(t1);
        bgp.jjtAppendChild(t2);

        final ASTIRI cardinality = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
        cardinality.setValue(AnalyticsVocab.CARDINALITY.toString());
        final ASTVar pofCardinality = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
        pofCardinality.setName(QueryProcessor.CARDINALITY_VAR);

        final ASTTriplesSameSubjectPath t5 = ASTProcessorUtil.createTriple(node.jjtGetChild(0), cardinality, pofCardinality);
        bgp.jjtAppendChild(t5);

        // Get the class attributes defining this class
        final ASTVar varType = ASTVarGenerator.getASTVar("dgs");
        final ASTIRI type = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
        type.setValue(AnalyticsVocab.TYPE.toString());

        final ASTVar varTypeLabel = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
        varTypeLabel.setName(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR);
        final ASTVar varTypeCard = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
        varTypeCard.setName(QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR);
        final ASTTriplesSameSubjectPath t7 = ASTProcessorUtil.createTriple(varLabel, type, varType);
        final ASTTriplesSameSubjectPath t8 = ASTProcessorUtil.createTriple(varType, cardinality, varTypeCard);
        final ASTTriplesSameSubjectPath t9 = ASTProcessorUtil.createTriple(varType, label, varTypeLabel);
        bgp.jjtAppendChild(t7);
        bgp.jjtAppendChild(t8);
        bgp.jjtAppendChild(t9);

        pofMetadata.add(QueryProcessor.CARDINALITY_VAR);
        pofMetadata.add(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR);
        pofMetadata.add(QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR);
      } else {
        final ASTVar s = ASTVarGenerator.getASTVar("dgs");
        final ASTTriplesSameSubjectPath t1 = ASTProcessorUtil.createTriple(node.jjtGetChild(0), label, s);
        final ASTTriplesSameSubjectPath t2 = ASTProcessorUtil.createTriple(s, label, subject);
        bgp.jjtAppendChild(t1);
        bgp.jjtAppendChild(t2);
      }
      return pofMetadata;
    }

    private boolean datasetEdgePOF(Object data, Node subject, Node dataset, Node bgp) {
      final List<String> pofMetadata = (List<String>) data;

      if (graphNamePOF && dataset != null && dataset instanceof ASTVar &&
          ((ASTVar) dataset).getName().equals(SyntaxTreeBuilder.PointOfFocus)) {
        graphNamePOF = false; // only output it one time

        final ASTIRI cardinality = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
        cardinality.setValue(AnalyticsVocab.CARDINALITY.toString());
        final ASTVar pofCardinality = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
        pofCardinality.setName(QueryProcessor.CARDINALITY_VAR);
        final ASTIRI originClass = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
        originClass.setValue(AnalyticsVocab.DOMAIN_URI.toString());

        final ASTTriplesSameSubjectPath t1 = ASTProcessorUtil.createTriple(subject, originClass, dataset);
        bgp.jjtAppendChild(t1);
        final ASTTriplesSameSubjectPath t2 = ASTProcessorUtil.createTriple(subject, cardinality, pofCardinality);
        bgp.jjtAppendChild(t2);

        pofMetadata.add(QueryProcessor.CARDINALITY_VAR);
        return true;
      }
      return false;
    }

    private boolean datasetClassPOF(Object data, Node subject, Node dataset, Node bgp) {
      final List<String> pofMetadata = (List<String>) data;

      if (graphNamePOF && dataset instanceof ASTVar &&
          ((ASTVar) dataset).getName().equals(SyntaxTreeBuilder.PointOfFocus)) {
        graphNamePOF = false; // only output it one time

        final ASTIRI cardinality = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
        cardinality.setValue(AnalyticsVocab.CARDINALITY.toString());
        final ASTVar pofCardinality = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
        pofCardinality.setName(QueryProcessor.CARDINALITY_VAR);

        final ASTTriplesSameSubjectPath t2 = ASTProcessorUtil.createTriple(subject, cardinality, pofCardinality);
        bgp.jjtAppendChild(t2);

        pofMetadata.add(QueryProcessor.CARDINALITY_VAR);
        return true;
      }
      return false;
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
