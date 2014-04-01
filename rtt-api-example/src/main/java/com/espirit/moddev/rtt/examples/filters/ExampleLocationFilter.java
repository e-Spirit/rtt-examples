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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example filter that writes the user country/city to the current session.
 */
public class ExampleLocationFilter implements UxpFilter, Filter {

	/**
	 * Logger used for logging.
	 */
	private static final Logger LOGGER = Logger.getLogger(ExampleLocationFilter.class.getName());

	/**
	 * Configuration of the filter.
	 */
	protected FilterConfig filterConfig;

	/**
	 * Name of the session variable that stores the information if this filter has been executed yet or not.
	 */
	private static final String FILTER_APPLIED = "_example_country_filter_applied_";

	/**
	 * Debug key.
	 */
	private static final String KEY = "GSJqcIQ5qXFbXiUaNyEk";

	/**
	 * Initializes this filter.
	 *
	 * @param filterConfig the filter configuration
	 * @throws ServletException in case of an error
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.log(Level.INFO, "LocationFilter init!");
        this.filterConfig = filterConfig;
	}

	/**
	 * Processes the given request and response.
	 *
	 * @param servletRequest the request
	 * @param servletResponse the response
	 * @param filterChain the processing chain
	 * @throws IOException if an error occurs
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
	 *
	 * @return true if filter should be applied.
	 */
	@Override
	public boolean shouldFilter(ServletRequest servletRequest) {
		return !((UxpServletRequest)servletRequest).getRequestContext().getBoolean(FILTER_APPLIED);
	}

	/**
	 * Get the user ip from the context and use FreeGeoIp-API to get user country/city and write it to current session
	 * for later use.
	 *
	 * @return ignored by current implementation.
	 */
	@Override
	public Object run(ServletRequest servletRequest) {
        RequestContext ctx = ((UxpServletRequest) servletRequest).getRequestContext();
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		
		String ip = (String) ctx.get("rtt.user.ip");
        LOGGER.log(Level.INFO, "LocationFilter Context IP " + ip);

		Object dIp = request.getParameter("ip");
		Object dKey = request.getParameter("key");
        LOGGER.log(Level.INFO, "LocationFilter Debug IP " + dIp);

		// set debug ip
		if (dIp != null && !dIp.toString().equals("") && dKey != null && !dKey.toString().equals("") && dKey.equals(KEY)) {
			ip = dIp.toString();
		}
        LOGGER.log(Level.INFO, "LocationFilter Using IP " + ip);

		if (shouldFilter(servletRequest) && ip != null && !ip.equals("")) {
            LOGGER.log(Level.INFO, "LocationFilter getting Location");
            ctx.set(FILTER_APPLIED);
			String country = null;
			String city = null;
			String line;
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(new URL("http://freegeoip.net/xml/" + ip).openStream(), "UTF-8"));
				line = in.readLine();
				while (line != null) {
					if (line.contains("CountryName")) {
						country = line.substring(line.indexOf('>') + 1, line.indexOf("</CountryName>"));
					}
					if (line.contains("City")) {
						city = line.substring(line.indexOf('>') + 1, line.indexOf("</City>"));
						break;
					}
					line = in.readLine();
				}
				request.getSession().setAttribute("rtt.user.country", country);
				request.getSession().setAttribute("rtt.user.city", city);
                LOGGER.log(Level.INFO, "LocationFilter country: " + country + ", city: " + city);

            } catch (IOException e) {
				LOGGER.log(Level.SEVERE, "A Problem occured while getting the location data!", e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "A Problem occured while reading the geoip service!", e);
					}
				}
			}
		}
		return null;
	}
}
