/**
 * Copyright (c) 2013 National University of Ireland, Galway. All Rights Reserved.
 */
package org.sindice.analytics.ranking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.sindice.analytics.queryProcessor.QueryProcessor;

/**
 * 
 */
public class CardinalityRanking
implements LabelsRanking {

  private static final int       TOP        = 1000;

  private final Map<Label, Recommendation> labels     = new LinkedHashMap<Label, Recommendation>();

  /**
   * Sort the {@link #labels} in reverse order of the cardinality
   */
  private final Map<Label, Recommendation> sortLabels = new TreeMap<Label, CardinalityRanking.Recommendation>(
    new Comparator<Label>() {
      @Override
      public int compare(Label o1, Label o2) {
        final Recommendation l1 = labels.get(o1);
        final Recommendation l2 = labels.get(o2);
        int c = (int) (l2.cardinality - l1.cardinality);
        if (c == 0) {
          return o1.getLabel().compareTo(o2.getLabel());
        }
        return c;
      }
    }
  );

  public class Recommendation {
    /** The number of the {@link Label} occurrences */
    private long              cardinality;
    /** The {@link Set} of URIs associated to the same {@link Label} */
    private final Set<String> pofResources = new HashSet<String>();
    private final Label       label;

    public Recommendation(Label label) {
      final Map<String, Object> context = label.getContext();
      final String pofResource = (String) context.get(QueryProcessor.POF_RESOURCE);

      pofResources.add(pofResource);
      this.label = label;
      cardinality = label.getCardinality();
      if (context.containsKey(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR) &&
          context.containsKey(QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR)) {
        final String ca = (String) context.get(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR);
        final long count = Long.valueOf(context.get(QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR).toString());
        final Map<String, Long> map = new HashMap<String, Long>();
        map.put(ca, count);
        context.put(QueryProcessor.CLASS_ATTRIBUTE_MAP, map);
      }
    }

    /**
     * @return the cardinality
     */
    public long getCardinality() {
      return cardinality;
    }

    /**
     * @return the label
     */
    public Label getLabel() {
      return label;
    }

    /**
     * Adds the contribution of the {@link Label} if its {@link QueryProcessor#POF_RESOURCE} was not seen already.
     * @param l the {@link Label} to add
     */
    private void addLabel(Label l) {
      final Map<String, Object> context = l.getContext();
      final String pofResource = (String) context.get(QueryProcessor.POF_RESOURCE);

      if (!pofResources.contains(pofResource)) {
        // label cardinality
        cardinality += l.getCardinality();
        // label class attribute
        if (context.containsKey(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR) &&
            context.containsKey(QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR)) {
          final Map<String, Long> classAttributes = (Map) label.getContext().get(QueryProcessor.CLASS_ATTRIBUTE_MAP);
          final String value = (String) context.get(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR);
          final int count = Integer.valueOf(context.get(QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR).toString());
          if (!classAttributes.containsKey(value)) {
            classAttributes.put(value, 0l);
          }
          classAttributes.put(value, classAttributes.get(value) + count);
        }
        pofResources.add(pofResource);
      }
    }

    @Override
    public String toString() {
      return "label=[" + label + "] cardinality=" + cardinality;
    }

  }

  @Override
  public String getName() {
    return "Cardinality";
  }

  @Override
  public void addLabel(Label label) {
    if (!labels.containsKey(label)) {
      labels.put(label, new Recommendation(label));
    } else {
      final Recommendation pair = labels.get(label);
      pair.addLabel(label);
    }
    // Take the TOP most occurring labels
    if (labels.size() >= TOP) {
      rank();
    }
  }

  private void rank() {
    sortLabels.putAll(labels);
    labels.clear();
    final Iterator<Entry<Label, Recommendation>> it = sortLabels.entrySet().iterator();
    for (int i = 0; i < TOP && it.hasNext(); i++) {
      final Entry<Label, Recommendation> lp = it.next();
      labels.put(lp.getKey(), lp.getValue());
    }
    sortLabels.clear();
  }

  @Override
  public Iterable<Recommendation> getLabels() {
    rank();
    return labels.values();
  }

  @Override
  public int size() {
    return labels.size();
  }

}
