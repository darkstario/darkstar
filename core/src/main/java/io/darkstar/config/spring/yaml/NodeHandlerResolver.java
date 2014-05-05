package io.darkstar.config.spring.yaml;

/**
 * Used by the {@link DefaultBeanDefinitionDocumentReader} to locate a {@link NodeHandler} implementation for a
 * particular named node.
 *
 * @see NodeHandler
 * @see DefaultBeanDefinitionDocumentReader
 */
public interface NodeHandlerResolver {

    /**
     * Resolve the node name and return the located {@link NodeHandler} implementation.
     *
     * @param nodeName the relevant node name
     * @return the located {@link NodeHandler} (may be {@code null})
     */
    NodeHandler resolve(String nodeName);
}
