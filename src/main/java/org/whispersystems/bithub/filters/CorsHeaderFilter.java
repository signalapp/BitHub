package org.whispersystems.bithub.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CorsHeaderFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException
  {
    if (response instanceof HttpServletResponse) {
      HttpServletResponse httpServletResponse = (HttpServletResponse) response;
      HttpServletRequest httpServletRequest = (HttpServletRequest) request;
      httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
      if ("OPTIONS".equals(httpServletRequest.getMethod())) {
        httpServletResponse.addHeader("Access-Control-Allow-Headers", "Content-Type");
      }
    }

    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

   @Override
   public void destroy() {

   }
}