/*
 * **********************************************************************
 * rtt-api-example
 * %%
 * Copyright (C) 2014 e-Spirit AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * **********************************************************************
 */
package com.espirit.moddev.rtt.examples.filters;


import com.espirit.moddev.rtt.uxp.UxpFilter;
import com.espirit.moddev.rtt.uxp.UxpServletRequest;
import com.espirit.moddev.rtt.uxp.context.RequestContext;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example filter that writes the user ip to the current context.
 */
public class ExampleIpFilter implements Filter, UxpFilter {
    
    /**
   	 * Logger used for logging.
   	 */
    private static final Logger LOGGER = Logger.getLogger(ExampleIpFilter.class.getName());

	/**
     * Name of the session variable that stores the information if this filter has been executed yet or not.
     */
    private static final String FILTER_APPLIED = "_example_ip_filter_applied_";
    
    
    /**
     * Initializes this filter.
     *
     * @param filterConfig the filter configuration
     * @throws ServletException in case of an error
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.log(Level.INFO, "IpFilter init!");
    }

    /**
     * Processes the given request and response.
     *
     * @param servletRequest  the request
     * @param servletResponse the response
     * @param filterChain     the processing chain
     * @throws IOException      if an error occurs
     * @throws ServletException if an error occurs
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        run(servletRequest);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Destroys the filter, use to free up model.
     */
    @Override
    public void destroy() {
    }

    /**
     * Determine if filter should be applied.
     * @return true if filter should be applied.
     */
    @Override
    public boolean shouldFilter(ServletRequest servletRequest) {
		return !((UxpServletRequest)servletRequest).getRequestContext().getBoolean(FILTER_APPLIED);
    }

    /**
     * Get the user ip from request and write it to current context for later use.
     *
     * @return ignored by current implementation.
     */
    @Override
    public Object run(ServletRequest servletRequest) {
        RequestContext ctx = ((UxpServletRequest)servletRequest).getRequestContext();
        if (shouldFilter(servletRequest)) {
            ctx.set(FILTER_APPLIED);
            
            String ipAddress = ((HttpServletRequest)servletRequest).getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = servletRequest.getRemoteAddr();
            }
            ctx.set("rtt.user.ip", ipAddress);
            LOGGER.log(Level.INFO, "IpFilter setting IP to " + ipAddress);
        }
        return null;
    }
}
