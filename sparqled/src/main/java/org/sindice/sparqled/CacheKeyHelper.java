package org.sindice.sparqled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CacheKeyHelper {

  private CacheKeyHelper() {
    // hide constructor
  }

  /**
   * Preparing query to be used for computing cache key. Strips the white spaces to avoid cache misses
   * 
   * @param s
   * @return
   */
  public static String prepareQueryForKeyComputation(String s) {
    // First find all variables names
    Map<String, Object> variableOrderMap = new LinkedHashMap<String, Object>();
    List<String> allMatches = new ArrayList<String>();;
    Matcher m = Pattern.compile("\\?[A-Za-z0-9]*").matcher(s);
    int index = 1;
    while (m.find()) {
      if (!allMatches.contains(m.group())) {
        allMatches.add(m.group());
        variableOrderMap.put(m.group(), index);
        index++;
      }
    }

    // sort variable by length
    Collections.sort(allMatches, new Comparator<String>() {

      @Override
      public int compare(String o1, String o2) {
        if (o2.length() > o1.length()) {
          return 1;
        } else if (o2.length() < o1.length()) {
          return -1;
        }
        return 0;
      }

    });

    // now replace all variables starting from longest ones
    for (String var : allMatches) {
      s = s.replaceAll("\\" + var, "?var" + variableOrderMap.get(var));
    }

    // strip white spaces
    return s.replaceAll("\\s", "");
  }

}
