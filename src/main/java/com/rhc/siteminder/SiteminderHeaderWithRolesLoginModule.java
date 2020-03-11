package com.rhc.siteminder;

import static org.jboss.security.SecurityConstants.WEB_REQUEST_KEY;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;

import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;
import org.jboss.security.util.StringPropertyReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This LoginModule automatically authenticates all requests. It is expecting the password passed
 * in to be a list of '^' delimited roles and will perform authorization by using this list
 *
 */
public class SiteminderHeaderWithRolesLoginModule extends UsernamePasswordLoginModule {

    private static final Logger LOG = LoggerFactory.getLogger(SiteminderHeaderWithRolesLoginModule.class);
    
    private static final String ROLE_MAP_PROPERTIES = "role-mapping";
    
    private static final String[] OPTIONS =
    {
            ROLE_MAP_PROPERTIES
    };
    
    private List<String> smGroups = new ArrayList<>();
    
    public SiteminderHeaderWithRolesLoginModule() {
        useFirstPass = true;
    }

    /**
     * Initialize this LoginModule.
     *
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        LOG.debug("initialize called");
        addValidOptions(OPTIONS);

        try {
            HttpServletRequest request = (HttpServletRequest) PolicyContext.getContext(WEB_REQUEST_KEY);
            if (SiteminderUtils.validateSMHeaders(request)) {
                final String userName = SiteminderUtils.getSMUser(request);
                LOG.debug("username = " + userName);
                sharedState.put("javax.security.auth.login.name", userName);
                subject.getPrincipals().clear();
                Principal principal = new SimplePrincipal(userName);
                subject.getPrincipals().add(principal);
                LOG.debug("added principal " + principal);

                smGroups = SiteminderUtils.getSMGroups(request);
            }
        } catch (PolicyContextException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        super.initialize(subject, callbackHandler, sharedState, options);
        loginOk = true;
    }
    
    /**
     * do not populate roles, as this will be done by the ldap login module
     */
    protected Group[] getRoleSets() throws LoginException {
        if (smGroups.isEmpty()) {
            return new Group[0];
        }
        SimpleGroup sr = new SimpleGroup("Roles");
        for (String group : smGroups) {
            String mappedRole = smGroupToRole(group);
            SimplePrincipal r = new SimplePrincipal(mappedRole);
            LOG.debug("added role " + mappedRole);
            sr.addMember(r);
        }
        Group[] roleSets = {sr};
        return roleSets;
    }
    
    @Override
    protected boolean validatePassword(String inputPassword,
            String expectedPassword) {
        return true;
    }

    @Override
    protected String getUsersPassword() throws LoginException {
        return "noop";
    }
    
    private String smGroupToRole (String smGroup) {
        String roleMapFile = (String)options.get(ROLE_MAP_PROPERTIES);
        roleMapFile = StringPropertyReplacer.replaceProperties(roleMapFile);
        LOG.debug("role map file is " + roleMapFile);
        
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(roleMapFile)) {
            props.load(fis);
            String mappedRole = props.getProperty(smGroup);
            if (mappedRole != null) {
                return mappedRole;
            }
        } catch (FileNotFoundException e) {
            LOG.error("File " + roleMapFile + " not found");
        } catch (IOException e) {
            LOG.error("Cannot open " + roleMapFile);
        }
        
        return smGroup;
    }
}
