/**
 * Copyright (c) 2014 National University of Ireland, Galway. All Rights Reserved.
 */
package org.sindice.analytics.queryProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;

/**
 * 
 */
public class RecommendationQuery {

  public static class Projection {

    final String var;

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

  public RecommendationQuery(String summary, String filter) {
    this.summary = summary;
    this.filter = filter;
  }

  public void addProjection(String var) {
    projections.add(new Projection(var));
  }

  Set<Projection> projection() {
    return projections;
  }

  String summary() {
    return summary;
  }

  public List<Type> type() {
    return types;
  }

  public List<Edge> edge() {
    return edges;
  }

  String filter() {
    return filter;
  }

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
