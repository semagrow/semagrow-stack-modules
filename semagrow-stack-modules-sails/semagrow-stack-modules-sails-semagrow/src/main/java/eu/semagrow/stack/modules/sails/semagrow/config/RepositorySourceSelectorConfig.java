package eu.semagrow.stack.modules.sails.semagrow.config;

import eu.semagrow.stack.modules.api.config.SourceSelectorImplConfigBase;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.sail.config.ProxyRepositoryConfig;

/**
 * Created by angel on 11/1/14.
 */
public class RepositorySourceSelectorConfig extends SourceSelectorImplConfigBase {

    public RepositoryImplConfig getMetadataConfig() {
        /*
        SailImplConfig sailConfig = new SEVODInferencerConfig(
                                        new ForwardChainingRDFSInferencerConfig(
                                            new MemoryStoreConfig()));


        return new SailRepositoryConfig(sailConfig);
        */
        return new ProxyRepositoryConfig("semagrow_metadata");
    }

}
