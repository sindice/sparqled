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
package org.sindice.sparqled.assist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQName;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.analytics.queryProcessor.QueryProcessor;
import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;
import org.sindice.analytics.queryProcessor.QueryProcessor.RecommendationType;
import org.sindice.analytics.ranking.CardinalityRanking.Recommendation;
import org.sindice.analytics.ranking.Label;
import org.sindice.analytics.ranking.LabelsRanking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonResponseWriter
implements ResponseWriter<String> {

  private static final Logger logger   = LoggerFactory.getLogger(JsonResponseWriter.class);
  private final ObjectMapper  mapper   = new ObjectMapper();

  @Override
  public String createSuccessAnswer(RecommendationType type,
                                    POFMetadata pofMetadata,
                                    LabelsRanking recommendations) {
    final Map<String, Object> jsonStructure = new HashMap<String, Object>();
    final String json;

    try {
      final boolean isClass = type.equals(RecommendationType.CLASS);

      jsonStructure.put(ResponseStructure.STATUS, ResponseStructure.SUCCESS);
      jsonStructure.put(ResponseStructure.RESULTS, new HashMap<String, Map>());

      ((Map<String, Object>) jsonStructure.get(ResponseStructure.RESULTS)).put(ResponseStructure.COUNT, recommendations.size());

      final List<Map<String, Object>> bindings = new ArrayList<Map<String, Object>>();

      ((Map<String, Object>) jsonStructure.get(ResponseStructure.RESULTS)).put(ResponseStructure.BINDINGS, bindings);
      for (Recommendation sug : recommendations.getLabels()) {
        // Add the recommendations, the value and number of occurrences
        final Map<String, Object> rec = new HashMap<String, Object>();
        rec.put(ResponseStructure.VALUE, sug.getLabel().getLabel());
        rec.put(ResponseStructure.COUNT, sug.getCardinality());
        rec.put(ResponseStructure.STATUS, sug.getLabel().getType());

        if (isClass) {
          // For classes, the predicates that define them and their counts
          addClassAttributes(sug.getLabel(), rec);
        }
        bindings.add(rec);
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
    } catch (Exception e) {
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
    final String keyword = (String) pofMetadata.pofNode.getMetadata(SyntaxTreeBuilder.Keyword);
    final String prefix = (String) pofMetadata.pofNode.getMetadata(SyntaxTreeBuilder.Prefix);
    final String value;

    if (keyword != null) {
      value = keyword;
    } else if (prefix != null) {
      value = prefix;
    } else {
      return;
    }
    final Integer lineStart = (Integer) pofMetadata.pofNode.getMetadata(SyntaxTreeBuilder.BeginLine);
    final Integer lineEnd = (Integer) pofMetadata.pofNode.getMetadata(SyntaxTreeBuilder.EndLine);
    final Integer columnStart = (Integer) pofMetadata.pofNode.getMetadata(SyntaxTreeBuilder.BeginColumn);
    final Integer columnEnd = (Integer) pofMetadata.pofNode.getMetadata(SyntaxTreeBuilder.EndColumn);

    if ((keyword != null || prefix != null) && (lineStart == null || lineEnd == null ||
         columnStart == null || columnEnd == null)) {
      logger.error("Unable to get recommendation position. Disabling substitution");
    }
    jsonStructure.put(ResponseStructure.REC_REPLACE, true);
    jsonStructure.put(ResponseStructure.REC_SUBSTITUTION, getSubstitutionData(value, lineStart, lineEnd,
      columnStart, columnEnd));
  }

  private void addClassAttributeSubstitution(final POFMetadata pofMetadata,
                                             final Map<String, Object> jsonStructure) {
    final String value = getValue(pofMetadata.pofClassAttribute);
    final Integer lineStart = (Integer) pofMetadata.pofClassAttribute.getMetadata(SyntaxTreeBuilder.BeginLine);
    final Integer lineEnd = (Integer) pofMetadata.pofClassAttribute.getMetadata(SyntaxTreeBuilder.EndLine);
    final Integer columnStart = (Integer) pofMetadata.pofClassAttribute.getMetadata(SyntaxTreeBuilder.BeginColumn);
    final Integer columnEnd = (Integer) pofMetadata.pofClassAttribute.getMetadata(SyntaxTreeBuilder.EndColumn);

    if (lineStart == null || lineEnd == null ||
        columnStart == null || columnEnd == null) {
      logger.error("Unable to get the class attribute position. Disabling substitution");
    } else if (value == null) {
      logger.error("Unable to get the class attribute value. Disabling substitution");
    } else {
      jsonStructure.put(ResponseStructure.CA_REPLACE, true);
      // "a" has been expanded
      jsonStructure.put(ResponseStructure.CA_SUBSTITUTION,
        getSubstitutionData(columnStart == columnEnd ? "a" : value, lineStart, lineEnd, columnStart, columnEnd));
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

  private void addClassAttributes(final Label sug, final Map<String, Object> rec) {
    final ArrayList<Map<String, Object>> contexts = new ArrayList<Map<String, Object>>();
    final Map<String, Long> classAttributes = (Map<String, Long>) sug.getContext().get(QueryProcessor.CLASS_ATTRIBUTE_MAP);

    for (Entry<String, Long> ca : classAttributes.entrySet()) {
      final String value = ca.getKey();
      final long count = ca.getValue();
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
