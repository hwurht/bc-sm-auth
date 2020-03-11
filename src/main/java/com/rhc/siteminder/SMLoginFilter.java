package com.rhc.siteminder;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMLoginFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(SMLoginFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.debug("init called");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        LOG.debug("doFilter called");
        HttpServletRequest servletRequest = (HttpServletRequest)request;
        if (LOG.isDebugEnabled()) {
            List<String> headers = Collections.list(servletRequest.getHeaderNames());
            headers.forEach(v -> LOG.debug("header name: " + v));
        }
        if (SiteminderUtils.validateSMHeaders(servletRequest)) {
            String user = SiteminderUtils.getSMUser(servletRequest);
            LOG.debug("User is " + user);
            if (!isLoggedIn(servletRequest)) {
                try {
                    LOG.debug("calling login");
                    servletRequest.login(user, "noop");
                } catch (ServletException e) {
                    LOG.error("login failed - " + e.getMessage());
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOG.debug("destroy called");
    }
    
    public static boolean isLoggedIn (HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return principal != null;
    }
}
