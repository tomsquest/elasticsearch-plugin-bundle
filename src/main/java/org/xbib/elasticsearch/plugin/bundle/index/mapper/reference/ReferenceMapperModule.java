package org.xbib.elasticsearch.plugin.bundle.index.mapper.reference;

import org.elasticsearch.common.inject.AbstractModule;
import org.xbib.elasticsearch.plugin.bundle.common.reference.ReferenceService;

/**
 * Reference field mapper module.
 */
public class ReferenceMapperModule extends AbstractModule {

    private final ReferenceMapperTypeParser typeParser;

    public ReferenceMapperModule(ReferenceMapperTypeParser typeParser) {
        this.typeParser = typeParser;
    }

    @Override
    protected void configure() {
        bind(ReferenceService.class).asEagerSingleton();
        bind(ReferenceMapperTypeParser.class).toInstance(typeParser);
    }
}
