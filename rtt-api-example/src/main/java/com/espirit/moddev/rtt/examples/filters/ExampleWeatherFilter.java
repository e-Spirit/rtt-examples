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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example filter that writes the current weather to the current session.
 */

public class ExampleWeatherFilter implements UxpFilter, Filter {

    /**
     * Logger used for logging.
     */
    private static final Logger LOGGER = Logger.getLogger(ExampleWeatherFilter.class.getName());

    /**
     * Configuration of the filter.
     */
    protected FilterConfig filterConfig;

    /**
     * Name of the session variable that stores the information if this filter has been executed yet or not.
     */
    private static final String FILTER_APPLIED = "_example_weather_filter_applied_";


    /**
     * Initializes this filter.
     *
     * @param filterConfig the filter configuration
     * @throws ServletException in case of an error
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.log(Level.INFO, "WeatherFilter init!");
        this.filterConfig = filterConfig;
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
     * destroys the filter, use to free up model.
     */
    @Override
    public void destroy() {
    }

    /**
     * Determine if filter should be applied.
	 *
     * @param request the request to use.
     * @return true if filter should be applied.
     */
    @Override
    public boolean shouldFilter(ServletRequest request) {
        return !((UxpServletRequest)request).getRequestContext().getBoolean(FILTER_APPLIED);
    }

    /**
     * Get the user location from the context and use OpenWeatherMap-API to get current weather and write it to current session for later use.
     *
     * @return ignored by current implementation.
     */
    @Override
    public Object run(ServletRequest servletRequest) {
        String weather;
        String city = null;
		
		RequestContext ctx = ((UxpServletRequest) servletRequest).getRequestContext();
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		
		city = (String) request.getSession().getAttribute("rtt.user.city");
        LOGGER.log(Level.INFO, "WeatherFilter using city: " + city);

        if (shouldFilter(servletRequest) && city != null && !city.equals("")) {
            ctx.set(FILTER_APPLIED);
            String out = readUrl("http://api.openweathermap.org/data/2.5/weather?q=" + city + "&mode=xml");
            Document document = createDocument(out);
            NodeList nodeList = document.getElementsByTagName("weather");
            if(nodeList.getLength() > 0) {
                weather = nodeList.item(0).getAttributes().getNamedItem("value").getTextContent();
                request.getSession().setAttribute("rtt.user.weather", weather);
                LOGGER.log(Level.INFO, "WeatherFilter weather: " + weather);
            }
        }
        return null;
    }

    /**
     * Reads the weather data from url to xml string.
     *
     * @param url the url to get the data from.
     * @return the weather data as xml.
     */
    private String readUrl(String url) {
        String result = null;
        try {
            Scanner scanner = new Scanner(new URL(url).openStream());
            result = scanner.useDelimiter("\\A").next();
            scanner.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "A Problem occurred while reading the weather data!", e);
        }
        return result;
    }


    /**
     * Creates an xml document from a string.
     *
     * @param sourceString the xml source.
     * @return the xml document.
     */
    private Document createDocument(String sourceString) {
        Document document = null;
        try {
            if(sourceString != null && !sourceString.equals("")) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(sourceString));
                document = builder.parse(is);
            }
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, "A parser configuration error occurred!", e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, "Problem parsing the weather data!", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Problem occurred while creating the weather document!", e);
        }
        return document;
    }

}
