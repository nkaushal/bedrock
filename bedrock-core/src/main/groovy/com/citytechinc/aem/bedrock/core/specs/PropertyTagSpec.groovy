package com.citytechinc.aem.bedrock.core.specs

import com.citytechinc.aem.bedrock.core.adapter.BedrockAdapterFactory
import com.citytechinc.aem.bedrock.core.bindings.ComponentBindings
import com.citytechinc.aem.bedrock.api.node.ComponentNode
import com.citytechinc.aem.prosper.specs.JspTagSpec
import org.apache.sling.api.adapter.AdapterFactory

import javax.servlet.jsp.tagext.TagSupport

/**
 * Spock specification for testing CQ component-based tag support classes.
 */
abstract class PropertyTagSpec<T extends TagSupport> extends JspTagSpec<T> {

    @Override
    Collection<AdapterFactory> addAdapterFactories() {
        [new BedrockAdapterFactory()]
    }

    /**
     * Set a <code>ComponentNode</code> for the given path in the <code>PageContext</code> for the tag under test.
     *
     * @param path node path
     */
    void setupComponentNode(String path) {
        def componentNode = resourceResolver.getResource(path).adaptTo(ComponentNode)

        tag.pageContext.setAttribute ComponentBindings.COMPONENT_NODE, componentNode
    }
}