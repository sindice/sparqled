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
package org.sindice.analytics.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQName;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.analytics.queryProcessor.QueryProcessor;
import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;
import org.sindice.analytics.queryProcessor.QueryProcessor.RecommendationType;
import org.sindice.analytics.ranking.Label;
import org.sindice.analytics.ranking.LabelsRanking;
import org.sindice.analytics.ranking.ScoreLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonResponseWriter
implements ResponseWriter<String> {

  private static final Logger logger   = LoggerFactory.getLogger(JsonResponseWriter.class);
  private final ObjectMapper  mapper   = new ObjectMapper();

  @Override
  public String createSuccessAnswer(RecommendationType type,
                                    POFMetadata pofMetadata,
                                    List<LabelsRanking> recommendations) {
    final Map<String, Object> jsonStructure = new HashMap<String, Object>();
    final String json;

    try {
      final boolean isClass = type.equals(RecommendationType.CLASS);

      jsonStructure.put(ResponseStructure.STATUS, ResponseStructure.SUCCESS);
      jsonStructure.put(ResponseStructure.RESULTS, new HashMap<String, Map>());

      // Add bindings to the results
      final ArrayList<Map> lrList = new ArrayList<Map>();
      ((HashMap<String, List<Map>>) jsonStructure.get(ResponseStructure.RESULTS)).put(ResponseStructure.RANKINGS, lrList);

      for (LabelsRanking lr : recommendations) {

        // the total number of recommendations
        ((HashMap<String, Object>) jsonStructure.get(ResponseStructure.RESULTS)).put(ResponseStructure.COUNT, lr.getLabelList().size());

        final HashMap<String, Object> lrJson = new HashMap<String, Object>();
        final ArrayList<Map<String, Object>> bindings = new ArrayList<Map<String, Object>>();

        lrList.add(lrJson);
        lrJson.put(ResponseStructure.NAME, lr.getName());
        lrJson.put(ResponseStructure.BINDINGS, bindings);
        for (ScoreLabel sug : lr.getLabelList()) {
          // Add the recommendations, the value and number of occurrences
          final Map<String, Object> rec = new HashMap<String, Object>();
          rec.put(ResponseStructure.VALUE, sug.getRecommendation());
          rec.put(ResponseStructure.COUNT, sug.getScore());
          rec.put(ResponseStructure.STATUS, sug.getRecommendationType());

          if (isClass) {
            // For classes, the predicates that define them and their counts
            addClassAttributes(sug, rec);
          }
          bindings.add(rec);
        }
      }

      // by default, substitutions are disabled
      jsonStructure.put(ResponseStructure.CA_REPLACE, false);
      jsonStructure.put(ResponseStructure.REC_REPLACE, false);
      // add current class attribute substitution data
      if (isClass) {
        addClassAttributeSubstitution(pofMetadata, jsonStructure);
      }
      // add other substitution data, e.g., for keyword search
      addRecSubstitution(pofMetadata, jsonStructure);

      json = mapper.writeValueAsString(jsonStructure);
    } catch (JsonGenerationException e) {
      logger.error("Unable to create JSON response: {}", e);
      return createErrorAnswer(type, e);
    } catch (JsonMappingException e) {
      logger.error("Unable to create JSON response: {}", e);
      return createErrorAnswer(type, e);
    } catch (IOException e) {
      logger.error("Unable to create JSON response: {}", e);
      return createErrorAnswer(type, e);
    }
    return json;
  }

  private String getValue(SimpleNode n) {
    if (n instanceof ASTIRI) {
      return ((ASTIRI) n).getValue();
    } else if (n instanceof ASTQName) {
      return ((ASTQName) n).getValue();
    }
    return null;
  }

  private void addRecSubstitution(final POFMetadata pofMetadata,
                                  final Map<String, Object> jsonStructure) {
    final List<Object> keyword = pofMetadata.pofNode.getMetadata() == null ? null : pofMetadata.pofNode
    .getMetadata(SyntaxTreeBuilder.Keyword);
    final List<Object> prefix = pofMetadata.pofNode.getMetadata() == null ? null : pofMetadata.pofNode
    .getMetadata(SyntaxTreeBuilder.Prefix);
    final ArrayList<Object> lineStart = pofMetadata.pofNode.getMetadata(SyntaxTreeBuilder.BeginLine);
    final ArrayList<Object> lineEnd = pofMetadata.pofNode.getMetadata(SyntaxTreeBuilder.EndLine);
    final ArrayList<Object> columnStart = pofMetadata.pofNode.getMetadata(SyntaxTreeBuilder.BeginColumn);
    final ArrayList<Object> columnEnd = pofMetadata.pofNode.getMetadata(SyntaxTreeBuilder.EndColumn);
    final String value;

    if ((keyword != null || prefix != null) &&
        (lineStart == null || lineEnd == null ||
        columnStart == null || columnEnd == null)) {
      logger.error("Unable to get recommendation position. Disabling substitution");
    }
    if (keyword != null) {
      value = (String) keyword.get(0);
      jsonStructure.put(ResponseStructure.REC_REPLACE, true);
      jsonStructure.put(ResponseStructure.REC_SUBSTITUTION, getSubstitutionData(value, (Integer) lineStart
      .get(0), (Integer) lineEnd.get(0), (Integer) columnStart.get(0), (Integer) columnEnd.get(0)));
    } else if (prefix != null) {
      value = (String) prefix.get(0);
      jsonStructure.put(ResponseStructure.REC_REPLACE, true);
      jsonStructure.put(ResponseStructure.REC_SUBSTITUTION, getSubstitutionData(value, (Integer) lineStart
      .get(0), (Integer) lineEnd.get(0), (Integer) columnStart.get(0), (Integer) columnEnd.get(0)));
    }
  }

  private void addClassAttributeSubstitution(final POFMetadata pofMetadata,
                                             final Map<String, Object> jsonStructure) {
    final String value = getValue(pofMetadata.pofClassAttribute);
    final ArrayList<Object> lineStart = pofMetadata.pofClassAttribute.getMetadata(SyntaxTreeBuilder.BeginLine);
    final ArrayList<Object> lineEnd = pofMetadata.pofClassAttribute.getMetadata(SyntaxTreeBuilder.EndLine);
    final ArrayList<Object> columnStart = pofMetadata.pofClassAttribute.getMetadata(SyntaxTreeBuilder.BeginColumn);
    final ArrayList<Object> columnEnd = pofMetadata.pofClassAttribute.getMetadata(SyntaxTreeBuilder.EndColumn);

    if (lineStart == null || lineEnd == null ||
        columnStart == null || columnEnd == null) {
      logger.error("Unable to get the class attribute position. Disabling substitution");
    } else if (value == null) {
      logger.error("Unable to get the class attribute value. Disabling substitution");
    } else {
      final int ls = (Integer) lineStart.get(0);
      final int le = (Integer) lineEnd.get(0);
      final int cs = (Integer) columnStart.get(0);
      final int ce = (Integer) columnEnd.get(0);
      jsonStructure.put(ResponseStructure.CA_REPLACE, true);
      // "a" has been expanded
      jsonStructure.put(ResponseStructure.CA_SUBSTITUTION, getSubstitutionData(cs == ce ? "a" : value, ls, le, cs, ce));
    }
  }

  private Map<String, Object> getSubstitutionData(String value, int lineStart, int lineEnd, int columnStart, int columnEnd) {
    final Map<String, Object> data = new HashMap<String, Object>();
    final Map<String, Integer> start = new HashMap<String, Integer>();
    final Map<String, Integer> end = new HashMap<String, Integer>();

    start.put(ResponseStructure.LINE, lineStart);
    start.put(ResponseStructure.CH, columnStart);
    data.put(ResponseStructure.START, start);

    end.put(ResponseStructure.LINE, lineEnd);
    end.put(ResponseStructure.CH, columnEnd);
    data.put(ResponseStructure.END, end);
    data.put(ResponseStructure.VALUE, value);
    return data;
  }

  private void addClassAttributes(final ScoreLabel sug,
                                final Map<String, Object> rec) {
    final ArrayList<Map<String, Object>> contexts = new ArrayList<Map<String, Object>>();

    for (Label label : sug.getLabels()) {
      final String value = (String) label.getContext().get(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR).get(0);
      final int count = Integer.valueOf(label.getContext().get(QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR).get(0).toString());
      if (contexts.isEmpty()) { // init
        final Map<String, Object> types = new HashMap<String, Object>();
        types.put(ResponseStructure.VALUE, value);
        types.put(ResponseStructure.COUNT, count);
        contexts.add(types);
      } else {
        boolean isPresent = false;
        for (Map<String, Object> ct : contexts) {
          if (ct.containsValue(value)) {
            isPresent = true;
            ct.put(ResponseStructure.COUNT, ((Integer) ct.get(ResponseStructure.COUNT)) + count);
            break;
          }
        }
        if (!isPresent) { // add the new class attribute
          final Map<String, Object> types = new HashMap<String, Object>();
          types.put(ResponseStructure.VALUE, value);
          types.put(ResponseStructure.COUNT, count);
          contexts.add(types);
        }
      }
    }
    rec.put("class_attributes", contexts);
  }

  @Override
  public String createErrorAnswer(RecommendationType type, Throwable e) {
    String msg = e.getLocalizedMessage();
    return "{\"results\":{\"bindings\":[]},\"" + ResponseStructure.STATUS + "\": \"" + ResponseStructure.ERROR +
           "\",\"" + ResponseStructure.MESSAGE + "\":\"" + StringEscapeUtils.escapeJava(msg.substring(msg.indexOf(':') + 2)) +
           "\"}";
  }

  @Override
  public String createEmptyAnswer(String msg) {
    return "{\"results\":{\"bindings\":[]},\"" + ResponseStructure.STATUS + "\": \"" + ResponseStructure.NONE +
           "\",\"" + ResponseStructure.MESSAGE + "\":\"" + StringEscapeUtils.escapeJava(msg) + "\"}";
  }

}
