/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sindice.query.parser.sparql;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTOperationContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPrefixDecl;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQName;
import org.openrdf.sindice.query.parser.sparql.ast.ASTServiceGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

/**
 * Processes the prefix declarations in a SPARQL query model.
 * 
 * @author Arjohn Kampman
 */
public class PrefixDeclProcessor {

  /**
   * Processes prefix declarations in queries. This method collects all prefixes
   * that are declared in the supplied query, verifies that prefixes are not
   * redefined and replaces any {@link ASTQName} nodes in the query with
   * equivalent {@link ASTIRI} nodes.
   * 
   * @param qc
   *          The query that needs to be processed.
   * @return A map containing the prefixes that are declared in the query (key)
   *         and the namespace they map to (value).
   * @throws MalformedQueryException
   *           If the query contains redefined prefixes or qnames that use
   *           undefined prefixes.
   */
  public static Map<String, String> process(ASTOperationContainer qc)
  throws MalformedQueryException {
    List<ASTPrefixDecl> prefixDeclList = qc.getPrefixDeclList();

    // Build a prefix --> IRI map
    Map<String, String> prefixMap = new LinkedHashMap<String, String>();
    for (ASTPrefixDecl prefixDecl : prefixDeclList) {
      String prefix = prefixDecl.getPrefix();
      String iri = prefixDecl.getIRI().getValue();

      if (prefixMap.containsKey(prefix)) {
        throw new MalformedQueryException("Multiple prefix declarations for prefix '" +
                                          prefix + "'");
      }

      prefixMap.put(prefix, iri);
    }

    QNameProcessor visitor = new QNameProcessor(prefixMap);
    try {
      qc.jjtAccept(visitor, null);
    } catch (VisitorException e) {
      throw new MalformedQueryException(e);
    }

    return prefixMap;
  }

  private static class QNameProcessor
  extends ASTVisitorBase {

    private Map<String, String> prefixMap;

    public QNameProcessor(Map<String, String> prefixMap) {
      this.prefixMap = prefixMap;
    }

    @Override
    public Object visit(ASTQName qnameNode, Object data)
    throws VisitorException {
      String qname = qnameNode.getValue();

      int colonIdx = qname.indexOf(':');
      assert colonIdx >= 0 : "colonIdx should be >= 0: " + colonIdx;

      String prefix = qname.substring(0, colonIdx);
      String localName = qname.substring(colonIdx + 1);

      String namespace = prefixMap.get(prefix);
      if (namespace == null) {
        throw new VisitorException("QName '" + qname +
                                   "' uses an undefined prefix");
      }

      localName = processEscapesAndHex(localName);

      // Replace the qname node with a new IRI node in the parent node
      ASTIRI iriNode = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
      iriNode.setValue(namespace + localName);
      qnameNode.jjtReplaceWith(iriNode);

      return null;
    }

    private String processEscapesAndHex(String localName) {

      // first process hex-encoded chars.
      StringBuffer unencoded = new StringBuffer();
      Pattern hexPattern = Pattern
      .compile("([^\\\\]|^)(%[A-F\\d][A-F\\d])", Pattern.CASE_INSENSITIVE);
      Matcher m = hexPattern.matcher(localName);
      boolean result = m.find();
      while (result) {
        // we match the previous char because we need to be sure we are not processing an escaped % char rather than
        // an actual hex encoding, for example: 'foo\%bar'.
        String previousChar = m.group(1);
        String encoded = m.group(2);

        int codePoint = Integer.parseInt(encoded.substring(1), 16);
        String decoded = String.valueOf(Character.toChars(codePoint));

        m.appendReplacement(unencoded, previousChar + decoded);
        result = m.find();
      }
      m.appendTail(unencoded);

      // then process escaped special chars.
      StringBuffer unescaped = new StringBuffer();
      Pattern escapedCharPattern = Pattern
      .compile("\\\\[_~\\.\\-!\\$\\&\\'\\(\\)\\*\\+\\,\\;\\=\\:\\/\\?#\\@\\%]");
      m = escapedCharPattern.matcher(unencoded.toString());
      result = m.find();
      while (result) {
        String escaped = m.group();
        m.appendReplacement(unescaped, escaped.substring(1));
        result = m.find();
      }
      m.appendTail(unescaped);

      return unescaped.toString();
    }

    @Override
    public Object visit(ASTServiceGraphPattern node, Object data)
    throws VisitorException {
      node.setPrefixDeclarations(prefixMap);
      return super.visit(node, data);
    }

  }
}
