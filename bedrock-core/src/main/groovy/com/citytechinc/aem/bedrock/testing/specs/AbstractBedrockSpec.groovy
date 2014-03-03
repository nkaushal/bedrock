/**
 * Copyright 2014, CITYTECH, Inc.
 * All rights reserved - Do Not Redistribute
 * Confidential and Proprietary
 */
package com.citytechinc.aem.bedrock.testing.specs

import com.citytechinc.aem.bedrock.content.node.ComponentNode
import com.citytechinc.aem.bedrock.content.node.impl.DefaultComponentNode
import com.citytechinc.aem.bedrock.content.page.PageManagerDecorator
import com.citytechinc.aem.bedrock.content.page.impl.DefaultPageManagerDecorator
import com.citytechinc.aem.spock.specs.AbstractSlingRepositorySpec

/**
 * Spock specification for CQ testing.
 */
abstract class AbstractBedrockSpec extends AbstractSlingRepositorySpec {

    @Override
    void addResourceAdapters() {
        addResourceAdapter(ComponentNode, { resource ->
            new DefaultComponentNode(resource)
        })
    }

    @Override
    void addResourceResolverAdapters() {
        addResourceResolverAdapter(PageManagerDecorator, {
            resourceResolver -> new DefaultPageManagerDecorator(resourceResolver)
        })
    }

    def getComponentNode(path) {
        def resource = resourceResolver.getResource(path)

        new DefaultComponentNode(resource)
    }

    def getPage(path) {
        resourceResolver.adaptTo(PageManagerDecorator).getPage(path)
    }
}