/**
 * Copyright (c) 2014 National University of Ireland, Galway. All Rights Reserved.
 */
package org.sindice.analytics.queryProcessor;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ComparisonFailure;
import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQName;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTString;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * 
 */
public class SparqlTranslationHelper {

  private final StringWriter w = new StringWriter();
  private final Mustache     mustache;

  public SparqlTranslationHelper() {
    MustacheFactory mf = new DefaultMustacheFactory();
    mustache = mf.compile(DGSQueryProcessor.TEMPLATE_NAME);
  }

  private String getQuery(RecommendationQuery rq) {
    w.getBuffer().setLength(0);
    return mustache.execute(w, rq).toString();
  }

  private static class SparqlStructure extends ASTVisitorBase {

    final List<String> elements = new ArrayList<String>();

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      elements.add("?" + node.getName());
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTIRI node, Object data)
    throws VisitorException {
      elements.add(node.getValue());
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTString node, Object data)
    throws VisitorException {
      elements.add(node.getValue());
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTQName node, Object data)
    throws VisitorException {
      elements.add(node.getValue());
      return super.visit(node, data);
    }

  }

  public void assertDGSQuery(RecommendationQuery expected, RecommendationQuery actual)
  throws Exception {
    StringWriter w = new StringWriter();

    SparqlStructure expectedStruct = new SparqlStructure();
    ASTQueryContainer expectedAst = SyntaxTreeBuilder.parseQuery(getQuery(expected));
    expectedStruct.visit(expectedAst, null);

    w.getBuffer().setLength(0);
    SparqlStructure actualStruct = new SparqlStructure();
    ASTQueryContainer actualAst = SyntaxTreeBuilder.parseQuery(getQuery(actual));
    actualStruct.visit(actualAst, null);

    if (expectedStruct.elements.size() != actualStruct.elements.size()) {
      throw new ComparisonFailure("Not the same number of elements", expectedAst.dump(""), actualAst.dump(""));
    }
    final Map<String, String> mappings = new HashMap<String, String>();
    for (int i = 0; i < expectedStruct.elements.size(); i++) {
      String v1 = expectedStruct.elements.get(i);
      String v2 = actualStruct.elements.get(i);

      if (v1.startsWith("?") && !v1.equals(v2)) { // compare vars
        if (!mappings.containsKey(v2)) {
          mappings.put(v2, v1);
        } else {
          if (!v1.equals(mappings.get(v2))) {
            throw new ComparisonFailure(v2 + " maps to " + mappings.get(v2) + ", should be " + v1,
              expectedAst.dump(""), actualAst.dump(""));
          }
        }
      } else {
        if (!v1.equals(v2)) {
          throw new ComparisonFailure("Expected: " + v1 + ", Got: " + v2, expectedAst.dump(""), actualAst.dump(""));
        }
      }
    }
  }

}
