package com.citytechinc.aem.bedrock.core.node.impl;

import com.citytechinc.aem.bedrock.api.link.Link;
import com.citytechinc.aem.bedrock.api.link.builders.LinkBuilder;
import com.citytechinc.aem.bedrock.api.node.BasicNode;
import com.citytechinc.aem.bedrock.api.node.ComponentNode;
import com.citytechinc.aem.bedrock.api.page.PageDecorator;
import com.citytechinc.aem.bedrock.api.page.PageManagerDecorator;
import com.citytechinc.aem.bedrock.core.node.predicates.ComponentNodePropertyExistsPredicate;
import com.citytechinc.aem.bedrock.core.node.predicates.ComponentNodePropertyValuePredicate;
import com.citytechinc.aem.bedrock.core.node.predicates.ComponentNodeResourceTypePredicate;
import com.day.cq.commons.DownloadResource;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.citytechinc.aem.bedrock.core.constants.ComponentConstants.DEFAULT_IMAGE_NAME;
import static com.citytechinc.aem.bedrock.core.link.impl.LinkFunctions.LINK_TO_HREF;
import static com.citytechinc.aem.bedrock.core.node.impl.NodeFunctions.RESOURCE_TO_BASIC_NODE;
import static com.citytechinc.aem.bedrock.core.node.impl.NodeFunctions.RESOURCE_TO_COMPONENT_NODE;
import static com.google.common.base.Preconditions.checkNotNull;

public final class DefaultComponentNode extends AbstractNode implements ComponentNode {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultComponentNode.class);

    private final BasicNode delegate;

    private final InheritanceValueMap properties;

    public DefaultComponentNode(final Resource resource) {
        super(resource);

        delegate = new DefaultBasicNode(resource);
        properties = new HierarchyNodeInheritanceValueMap(resource);
    }

    @Override
    public ValueMap asMap() {
        return delegate.asMap();
    }

    @Override
    public Optional<ComponentNode> findAncestor(final Predicate<ComponentNode> predicate) {
        return findAncestorForPredicate(predicate);
    }

    @Override
    public List<ComponentNode> findDescendants(final Predicate<ComponentNode> predicate) {
        final List<ComponentNode> descendantNodes = Lists.newArrayList();

        for (final ComponentNode node : getComponentNodes()) {
            if (predicate.apply(node)) {
                descendantNodes.add(node);
            }

            descendantNodes.addAll(node.findDescendants(predicate));
        }

        return descendantNodes;
    }

    @Override
    public Optional<ComponentNode> findAncestorWithProperty(final String propertyName) {
        return findAncestorForPredicate(new ComponentNodePropertyExistsPredicate(propertyName));
    }

    @Override
    public <V> Optional<ComponentNode> findAncestorWithPropertyValue(final String propertyName, final V propertyValue) {
        return findAncestorForPredicate(new ComponentNodePropertyValuePredicate<V>(propertyName, propertyValue));
    }

    @Override
    public <T> T get(final String propertyName, final T defaultValue) {
        return delegate.get(propertyName, defaultValue);
    }

    @Override
    public <T> Optional<T> get(final String propertyName, final Class<T> type) {
        return delegate.get(propertyName, type);
    }

    @Override
    public Optional<String> getAsHref(final String propertyName) {
        return delegate.getAsHref(propertyName);
    }

    @Override
    public Optional<String> getAsHref(final String propertyName, final boolean strict) {
        return delegate.getAsHref(propertyName, strict);
    }

    @Override
    public Optional<String> getAsHref(final String propertyName, final boolean strict, final boolean mapped) {
        return delegate.getAsHref(propertyName, strict, mapped);
    }

    @Override
    public Optional<String> getAsHrefInherited(final String propertyName) {
        return getAsHrefInherited(propertyName, false);
    }

    @Override
    public Optional<String> getAsHrefInherited(final String propertyName, final boolean strict) {
        return getAsHrefInherited(propertyName, strict, false);
    }

    @Override
    public Optional<String> getAsHrefInherited(final String propertyName, final boolean strict, final boolean mapped) {
        return getAsLinkInherited(propertyName, strict, mapped).transform(LINK_TO_HREF);
    }

    @Override
    public Optional<Link> getAsLink(final String propertyName) {
        return delegate.getAsLink(propertyName);
    }

    @Override
    public Optional<Link> getAsLink(final String propertyName, final boolean strict) {
        return delegate.getAsLink(propertyName, strict);
    }

    @Override
    public Optional<Link> getAsLink(final String propertyName, final boolean strict, final boolean mapped) {
        return delegate.getAsLink(propertyName, strict, mapped);
    }

    @Override
    public <T> List<T> getAsList(final String propertyName, final Class<T> type) {
        return delegate.getAsList(propertyName, type);
    }

    @Override
    public Optional<Link> getAsLinkInherited(final String propertyName) {
        return getAsLinkInherited(propertyName, false);
    }

    @Override
    public Optional<Link> getAsLinkInherited(final String propertyName, final boolean strict) {
        return getAsLinkInherited(propertyName, strict, false);
    }

    @Override
    public Optional<Link> getAsLinkInherited(final String propertyName, final boolean strict, final boolean mapped) {
        return getLinkOptional(getInherited(propertyName, String.class), strict, mapped);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getAsListInherited(final String propertyName, final Class<T> type) {
        final T[] defaultValue = (T[]) Array.newInstance(type, 0);

        return Arrays.asList(properties.getInherited(checkNotNull(propertyName), defaultValue));
    }

    @Override
    public Optional<PageDecorator> getAsPage(final String propertyName) {
        return delegate.getAsPage(propertyName);
    }

    @Override
    public Optional<PageDecorator> getAsPageInherited(final String propertyName) {
        return getPageOptional(properties.getInherited(checkNotNull(propertyName), ""));
    }

    @Override
    public Optional<ComponentNode> getComponentNode(final String relativePath) {
        checkNotNull(relativePath);

        return Optional.fromNullable(resource.getChild(relativePath)).transform(RESOURCE_TO_COMPONENT_NODE);
    }

    @Override
    public List<ComponentNode> getComponentNodes() {
        return FluentIterable.from(resource.getChildren()).transform(RESOURCE_TO_COMPONENT_NODE).toList();
    }

    @Override
    public List<ComponentNode> getComponentNodes(final Predicate<ComponentNode> predicate) {
        checkNotNull(predicate);

        return FluentIterable.from(resource.getChildren()).transform(RESOURCE_TO_COMPONENT_NODE).filter(predicate)
            .toList();
    }

    @Override
    public List<ComponentNode> getComponentNodes(final String relativePath) {
        checkNotNull(relativePath);

        final Resource child = resource.getChild(relativePath);

        final List<ComponentNode> nodes;

        if (child == null) {
            nodes = Collections.emptyList();
        } else {
            nodes = FluentIterable.from(child.getChildren()).transform(RESOURCE_TO_COMPONENT_NODE).toList();
        }

        return nodes;
    }

    @Override
    public List<ComponentNode> getComponentNodes(final String relativePath, final String resourceType) {
        return getComponentNodes(relativePath, new ComponentNodeResourceTypePredicate(resourceType));
    }

    @Override
    public List<ComponentNode> getComponentNodes(final String relativePath, final Predicate<ComponentNode> predicate) {
        checkNotNull(relativePath);
        checkNotNull(predicate);

        return FluentIterable.from(getComponentNodes(relativePath)).filter(predicate).toList();
    }

    @Override
    public Optional<BasicNode> getDesignNode() {
        final ResourceResolver resourceResolver = resource.getResourceResolver();

        final Designer designer = resourceResolver.adaptTo(Designer.class);
        final Style style = designer.getStyle(resource);

        final Resource styleResource = resourceResolver.getResource(style.getPath());

        return Optional.fromNullable(styleResource).transform(RESOURCE_TO_BASIC_NODE);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getHref() {
        return delegate.getHref();
    }

    @Override
    public String getHref(final boolean mapped) {
        return delegate.getHref(mapped);
    }

    @Override
    public Optional<String> getImageReference() {
        return delegate.getImageReference();
    }

    @Override
    public Optional<String> getImageReference(final String name) {
        return delegate.getImageReference(name);
    }

    @Override
    public Optional<String> getImageReferenceInherited() {
        return getImageReferenceInherited(DEFAULT_IMAGE_NAME);
    }

    @Override
    public Optional<String> getImageReferenceInherited(final String name) {
        return Optional.fromNullable(properties.getInherited(name + "/" + DownloadResource.PN_REFERENCE, String.class));
    }

    @Override
    public Optional<String> getImageRendition(final String renditionName) {
        return delegate.getImageRendition(renditionName);
    }

    @Override
    public Optional<String> getImageRendition(final String name, final String renditionName) {
        return delegate.getImageRendition(name, renditionName);
    }

    @Override
    public Optional<String> getImageSource() {
        return delegate.getImageSource();
    }

    @Override
    public Optional<String> getImageSource(final int width) {
        return delegate.getImageSource(width);
    }

    @Override
    public Optional<String> getImageSource(final String name) {
        return delegate.getImageSource(name);
    }

    @Override
    public Optional<String> getImageSource(final String name, final int width) {
        return delegate.getImageSource(name, width);
    }

    @Override
    public Optional<String> getImageSourceInherited() {
        return getImageSourceInherited(DEFAULT_IMAGE_NAME);
    }

    @Override
    public Optional<String> getImageSourceInherited(final int width) {
        return getImageSourceInherited(DEFAULT_IMAGE_NAME, width);
    }

    @Override
    public Optional<String> getImageSourceInherited(final String name) {
        return getImageSourceInherited(name, -1);
    }

    @Override
    public Optional<String> getImageSourceInherited(final String name, final int width) {
        final Predicate<ComponentNode> predicate = new Predicate<ComponentNode>() {
            @Override
            public boolean apply(final ComponentNode componentNode) {
                return componentNode.isHasImage(name);
            }
        };

        return findAncestor(predicate).transform(new Function<ComponentNode, String>() {
            @Override
            public String apply(final ComponentNode componentNode) {
                return componentNode.getImageSource(name, width).get();
            }
        });
    }

    @Override
    public int getIndex() {
        return delegate.getIndex();
    }

    @Override
    public int getIndex(final String resourceType) {
        return delegate.getIndex(resourceType);
    }

    @Override
    public <T> T getInherited(final String propertyName, final T defaultValue) {
        return properties.getInherited(propertyName, defaultValue);
    }

    @Override
    public <T> Optional<T> getInherited(final String propertyName, final Class<T> type) {
        return Optional.fromNullable(properties.getInherited(propertyName, type));
    }

    @Override
    public Link getLink() {
        return delegate.getLink();
    }

    @Override
    public Link getLink(final boolean mapped) {
        return delegate.getLink(mapped);
    }

    @Override
    public LinkBuilder getLinkBuilder() {
        return delegate.getLinkBuilder();
    }

    @Override
    public LinkBuilder getLinkBuilder(final boolean mapped) {
        return delegate.getLinkBuilder(mapped);
    }

    @Override
    public Optional<Node> getNode() {
        return delegate.getNode();
    }

    @Override
    public List<BasicNode> getNodesInherited(final String relativePath) {
        final Optional<Resource> childOptional = findChildResourceInherited(relativePath);

        final List<BasicNode> nodes;

        if (childOptional.isPresent()) {
            final Resource child = childOptional.get();

            nodes = FluentIterable.from(child.getChildren()).transform(RESOURCE_TO_BASIC_NODE).toList();
        } else {
            nodes = Collections.emptyList();
        }

        return nodes;
    }

    @Override
    public Optional<ComponentNode> getParent() {
        return Optional.fromNullable(resource.getParent()).transform(RESOURCE_TO_COMPONENT_NODE);
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public List<Property> getProperties(final Predicate<Property> predicate) {
        return delegate.getProperties(predicate);
    }

    @Override
    public Resource getResource() {
        return delegate.getResource();
    }

    @Override
    public boolean isHasImage() {
        return delegate.isHasImage();
    }

    @Override
    public boolean isHasImage(final String name) {
        return delegate.isHasImage(name);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("path", getPath()).add("properties", Maps.newHashMap(asMap()))
            .toString();
    }

    private Optional<ComponentNode> findAncestorForPredicate(final Predicate<ComponentNode> predicate) {
        final PageManagerDecorator pageManager = resource.getResourceResolver().adaptTo(PageManagerDecorator.class);
        final PageDecorator containingPage = pageManager.getContainingPage(resource);

        final String path = resource.getPath();

        final String relativePath = resource.getName().equals(JcrConstants.JCR_CONTENT) ? "" : path.substring(
            containingPage.getContentResource().getPath().length() + 1);

        LOG.debug("relative path = {}", relativePath);

        final Function<PageDecorator, Optional<ComponentNode>> componentNodeFunction = new Function<PageDecorator, Optional<ComponentNode>>() {
            @Override
            public Optional<ComponentNode> apply(final PageDecorator page) {
                return relativePath.isEmpty() ? page.getComponentNode() : page.getComponentNode(relativePath);
            }
        };

        final Predicate<PageDecorator> pagePredicate = new Predicate<PageDecorator>() {
            @Override
            public boolean apply(final PageDecorator page) {
                final Optional<ComponentNode> componentNodeOptional = componentNodeFunction.apply(page);

                return componentNodeOptional.isPresent() && predicate.apply(componentNodeOptional.get());
            }
        };

        return containingPage.findAncestor(pagePredicate).transform(new Function<PageDecorator, ComponentNode>() {
            @Override
            public ComponentNode apply(final PageDecorator page) {
                return componentNodeFunction.apply(page).get();
            }
        });
    }

    private Optional<Resource> findChildResourceInherited(final String relativePath) {
        final PageManagerDecorator pageManager = resource.getResourceResolver().adaptTo(PageManagerDecorator.class);
        final PageDecorator containingPage = pageManager.getContainingPage(resource);

        final StringBuilder builder = new StringBuilder();

        if (resource.getName().equals(JcrConstants.JCR_CONTENT)) {
            builder.append(relativePath);
        } else {
            builder.append(resource.getPath().substring(containingPage.getContentResource().getPath().length() + 1));
            builder.append('/');
            builder.append(relativePath);
        }

        // path relative to jcr:content
        final String resourcePath = builder.toString();

        LOG.debug("findChildResourceInherited() child resource relative path = {}", resourcePath);

        final Predicate<PageDecorator> predicate = new Predicate<PageDecorator>() {
            @Override
            public boolean apply(final PageDecorator page) {
                return page.getContentResource(resourcePath) != null;
            }
        };

        return containingPage.findAncestor(predicate).transform(new Function<PageDecorator, Resource>() {
            @Override
            public Resource apply(final PageDecorator page) {
                return page.getContentResource(resourcePath);
            }
        });
    }
}
