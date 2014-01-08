package org.whispersystems.bithub.filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Rewrites /v1/* to /service/v1/*.
 *
 * The service path had to be changed.
 * This filter is there for backwards compatibility.
 */
@WebFilter
public class RootPathServiceUrlRewriteFilter implements Filter {

  @Override
  public void init(FilterConfig config) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
    HttpServletRequest request = (HttpServletRequest) req;
    String requestURI = request.getRequestURI();
    if (requestURI.startsWith("/v1/")) {
      req.getRequestDispatcher("/service" + requestURI).forward(req, res);
    } else {
      chain.doFilter(req, res);
    }
  }

  @Override
  public void destroy() {
  }
}
