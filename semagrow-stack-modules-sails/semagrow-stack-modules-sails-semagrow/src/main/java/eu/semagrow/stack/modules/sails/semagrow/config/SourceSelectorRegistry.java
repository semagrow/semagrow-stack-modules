package eu.semagrow.stack.modules.sails.semagrow.config;

import eu.semagrow.stack.modules.api.config.SourceSelectorFactory;
import info.aduna.lang.service.ServiceRegistry;


/**
 * Created by angel on 16/6/2015.
 */
public class SourceSelectorRegistry extends ServiceRegistry<String, SourceSelectorFactory> {

    private static SourceSelectorRegistry defaultRegistry;

    public static synchronized SourceSelectorRegistry getInstance() {
        if(defaultRegistry == null) {
            defaultRegistry = new SourceSelectorRegistry();
        }

        return defaultRegistry;
    }

    public SourceSelectorRegistry() {
        super(SourceSelectorFactory.class);
    }

    @Override
    protected String getKey(SourceSelectorFactory sourceSelectorFactory) {
        return sourceSelectorFactory.getType();
    }
}
