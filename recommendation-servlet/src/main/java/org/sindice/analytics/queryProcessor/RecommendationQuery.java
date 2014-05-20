/**
 * Copyright (c) 2014 National University of Ireland, Galway. All Rights Reserved.
 */
package org.sindice.analytics.queryProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.algebra.Filter;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;

import com.github.mustachejava.Mustache;

/**
 * Scope object for translating a SPARQL query into a Graph Summary query using a {@link Mustache} instance.
 * The translation is performed on a triple pattern (TP) basis: each TP in the SPARQL query is translated into a
 * Basic Graph Pattern. Therefore, a section of the template relates to the translation of a single TP, and variables
 * of a section cannot be accessed from another one. A template can have up to 6 sections:
 * <ul>
 * <li><b>projection</b>: the set of projected variables. A {@link Projection} is the scope of this section;</li>
 * <li><b>edge</b>: translation of a triple pattern. An {@link Edge} is the scope of this section;</li>
 * <li><b>type</b>: translation of a triple pattern which predicate is a
 * {@link AnalyticsClassAttributes class attribute}. A {@link Type} is the scope of this section;</li>
 * <li><b>filter</b>: add a {@link Filter} to the translated query, e.g., for performing pattern matching of the
 * {@link SyntaxTreeBuilder#PointOfFocus} of the SPARQL query; and</li>
 * <li><b>limit</b>: add a LIMIT clause to the translated query.</li>
 * </ul>
 */
public class RecommendationQuery {

  /**
   * Projected variable in the translated query.
   */
  public static class Projection {

    final String var;

    /**
     * 
     * @param var the name of the variable
     */
    public Projection(String var) {
      this.var = var;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Projection) {
        return var.equals(((Projection) obj).var);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return 31 + var.hashCode();
    }

    @Override
    public String toString() {
      return var;
    }

  }

  /**
   * Provides variables to the type section.
   * There are three variables:
   * <ol>
   * <li><b>dataset</b>: the dataset name the triple pattern belongs to;</li>
   * <li><b>resource</b>: the name of the variable for the BGP of a node; and</li>
   * <li><b>type</b>: the class URI in the triple pattern, or a variable name if not defined.</li>
   * </ol>
   * If dataset is defined, the subsection <b>origin</b> is called. If type is the Point of Focus in the query,
   * the section <b>pof</b> is called.
   */
  public static class Type {

    private final static String pof = "?" + SyntaxTreeBuilder.PointOfFocus;

    String resource;
    String dataset;
    String type;

    public Type() {}

    public Type(String dataset, String resource, String type) {
      this.dataset = dataset;
      this.resource = resource;
      this.type = type;
    }

    /**
     * @return the resource
     */
    public String getResource() {
      return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(String resource) {
      this.resource = resource;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(String dataset) {
      this.dataset = dataset;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
      this.type = type;
    }

    public boolean origin() {
      return dataset != null;
    }

    public boolean pof() {
      return type.equals(pof);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("dataset=").append(dataset).append(" resource=").append(resource).append(" type=").append(type);
      return sb.toString();
    }

  }

  /**
   * Provides variables to the edge section.
   * There are five variables:
   * <ol>
   * <li><b>dataset</b>: the dataset name the triple pattern belongs to;</li>
   * <li><b>resource</b>: the name of the variable for the BGP of an edge;</li>
   * <li><b>source</b>: the name of the variable at the subject of the triple pattern;</li>
   * <li><b>target</b>: the name of the variable at the object of the triple pattern; and</li>
   * <li><b>predicate</b>: the predicate URI between the source and target, or a variable if node defined.</li>
   * </ol>
   * The subsection <b>origin</b> is called if the dataset is defined; <b>pof</b> is called if
   * the predicate is the Point of Focus of the query; <b>leaf</b> is called if the object of the triple pattern is not
   * reused in the rest of the query.
   */
  public static class Edge {

    private final static String pof = "?" + SyntaxTreeBuilder.PointOfFocus;

    String resource;
    String dataset;
    String source;
    String predicate;
    String target;

    public Edge() {}

    public Edge(String dataset, String resource, String source, String predicate, String target) {
      this.dataset = dataset;
      this.resource = resource;
      this.source = source;
      this.predicate = predicate;
      this.target = target;
    }

    /**
     * @return the resource
     */
    public String getResource() {
      return resource;
    }

    /**
     * @return the source
     */
    public String getSource() {
      return source;
    }

    /**
     * @return the target
     */
    public String getTarget() {
      return target;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(String resource) {
      this.resource = resource;
    }
    /**
     * @param dataset the dataset to set
     */
    public void setDataset(String dataset) {
      this.dataset = dataset;
    }
    /**
     * @param source the source to set
     */
    public void setSource(String source) {
      this.source = source;
    }
    /**
     * @param predicate the predicate to set
     */
    public void setPredicate(String predicate) {
      this.predicate = predicate;
    }
    /**
     * @param target the target to set
     */
    public void setTarget(String target) {
      this.target = target;
    }

    public boolean origin() {
      return dataset != null;
    }

    public boolean leaf() {
      return target == null;
    }

    public boolean pof() {
      return predicate.equals(pof);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("dataset=").append(dataset).append(" resource=").append(resource)
        .append(" source=").append(source).append(" predicate=").append(predicate).append(" target=").append(target);
      return sb.toString();
    }

  }

  private final String          summary;
  private final Set<Projection> projections = new HashSet<Projection>();
  private final List<Type>      types       = new ArrayList<Type>();
  private final List<Edge>      edges       = new ArrayList<Edge>();
  private final String          filter;
  private int                   limit;

  /**
   * Create a {@link RecommendationQuery} instance with summary as the Named Graph of the summary RDF data, and with a
   * possible filter.
   */
  public RecommendationQuery(String summary, String filter) {
    this.summary = summary;
    this.filter = filter;
  }

  /**
   * Add a variable name to the projection list of the translated query
   */
  public void addProjection(String var) {
    projections.add(new Projection(var));
  }

  Set<Projection> projection() {
    return projections;
  }

  String summary() {
    return summary;
  }

  /**
   * The list of {@link Type}s in the translated query.
   */
  public List<Type> type() {
    return types;
  }

  /**
   * The list of {@link Edge}s in the translated query.
   */
  public List<Edge> edge() {
    return edges;
  }

  String filter() {
    return filter;
  }

  /**
   * Set a limit to the translated query.
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  Integer limit() {
    return limit == 0 ? null : limit;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("summary=").append(summary).append(" projections=").append(projections)
      .append(" types=").append(types).append(" edges=").append(edges);
    return sb.toString();
  }

}
