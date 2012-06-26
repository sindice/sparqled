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
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonpCallbackFilter
implements Filter {

  private static Logger log = LoggerFactory
                            .getLogger(JsonpCallbackFilter.class);

  public void init(FilterConfig fConfig)
  throws ServletException {}

  public void doFilter(ServletRequest request,
                       ServletResponse response,
                       FilterChain chain)
  throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    @SuppressWarnings("unchecked")
    Map<String, String[]> parms = httpRequest.getParameterMap();

    if (parms.containsKey("callback")) {
      if (log.isDebugEnabled()) {
        log.debug("Wrapping response with JSONP callback '" + parms.get("callback")[0] + "'");
      }

      BufferedHttpResponseWrapper wrapper = new BufferedHttpResponseWrapper(httpResponse);

      chain.doFilter(request, wrapper);

      wrapper.flushBuffer();

      OutputStream out = response.getOutputStream();
      out.write(new String(parms.get("callback")[0] + "(").getBytes());
      out.write(wrapper.getBuffer());
      out.write(new String(");").getBytes());

      wrapper.setContentType("text/javascript;charset=UTF-8");

      out.close();
    } else {
      chain.doFilter(request, response);
    }
  }

  public void destroy() {}

}
