package com.rhc.siteminder;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockSSORestServletFilterPlugin implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(MockSSORestServletFilterPlugin.class);

    public static final String SM_USER = "smuser";
    public static final String GROUPS = "group1^group2";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.debug("init called");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        LOG.debug("doFilter called");
        HttpServletRequest servletRequest = (HttpServletRequest)request;
        MockWrapper wrapper = new MockWrapper(servletRequest);
        wrapper.addHeader(SiteminderConstants.SM_USER_HEADER, SM_USER);
        wrapper.addHeader(SiteminderConstants.SM_GROUP_HEADER, GROUPS);
        LOG.debug("added headers");
        chain.doFilter(wrapper, response);
    }

    @Override
    public void destroy() {
        LOG.debug("doFilter destroy called");
    }
    
    public final class MockWrapper extends HttpServletRequestWrapper {
        private Map<String, String> headerMap = new HashMap<>();
        
        public MockWrapper (HttpServletRequest request) {
            super(request);
        }
        
        public void addHeader (String key, String value) {
            headerMap.put(key, value);
        }
        
        @Override
        public String getHeader(String name) {
            if (headerMap.containsKey(name)) {
                return headerMap.get(name);
            }
            else {
                return super.getHeader(name);
            }
        }
        
        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            headerMap.keySet().forEach(k -> {
                if (!names.contains(k)) {
                    names.add(k);
                }
            });
            return Collections.enumeration(names);
        }
    }
}
