package eu.semagrow.stack.modules.api.config;

import eu.semagrow.stack.modules.api.source.SourceSelector;

/**
 * Created by angel on 11/1/14.
 */
public interface SourceSelectorFactory {

    SourceSelectorImplConfig getConfig();

    SourceSelector getSourceSelector(SourceSelectorImplConfig config) throws SourceSelectorConfigException;

    String getType();
}
