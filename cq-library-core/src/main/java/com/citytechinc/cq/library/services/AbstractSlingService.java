/**
 * Copyright 2013, CITYTECH, Inc.
 * All rights reserved - Do Not Redistribute
 * Confidential and Proprietary
 */
package com.citytechinc.cq.library.services;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Base class for services that require an administrative <code>Session</code> and/or <code>ResourceResolver</code>.
 */
@Component(componentAbstract = true)
public abstract class AbstractSlingService {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private SlingRepository repository;

    private ResourceResolver resourceResolver;

    private Session session;

    /**
     * Close the administrative resource resolver.  This method should be called by the <code>@Deactivate</code> method
     * of the implementing class if the <code>getAdministrativeResourceResolver()</code> method was used at any time.
     */
    protected void closeResourceResolver() {
        if (resourceResolver != null) {
            resourceResolver.close();
        }
    }

    /**
     * Close the administrative session.  This method should be called by the <code>@Deactivate</code> method of the
     * implementing class if the <code>getAdministrativeSession()</code> method was used at any time.
     */
    protected void closeSession() {
        if (session != null) {
            session.logout();
        }
    }

    /**
     * Get an administrative resource resolver.
     *
     * @return resource resolver
     * @throws LoginException if error occurs during authentication
     */
    protected ResourceResolver getAdministrativeResourceResolver() throws LoginException {
        if (resourceResolver == null) {
            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
        }

        return resourceResolver;
    }

    /**
     * Get an administrative JCR session.
     *
     * @return session
     * @throws RepositoryException if error occurs during authentication
     */
    protected Session getAdministrativeSession() throws RepositoryException {
        if (session == null) {
            session = repository.loginAdministrative(null);
        }

        return session;
    }
}