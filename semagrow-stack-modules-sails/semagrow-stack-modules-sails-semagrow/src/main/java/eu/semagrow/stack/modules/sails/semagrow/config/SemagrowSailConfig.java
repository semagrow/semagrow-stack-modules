package eu.semagrow.stack.modules.sails.semagrow.config;

import eu.semagrow.stack.modules.sails.config.SEVODInferencerConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.config.SailImplConfigBase;
import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

import java.util.List;

/**
 * Created by angel on 5/29/14.
 */
public class SemagrowSailConfig extends SailImplConfigBase {

    private String metadataRepoId = "semagrow_metadata";

    public SemagrowSailConfig() { super(SemagrowSailFactory.SAIL_TYPE); }

    public SourceSelectorImplConfig getSourceSelectorConfig() {
        return new RepositorySourceSelectorConfig();
    }

    public String getMetadataRepoId() { return metadataRepoId; }

    public void setMetadataRepoId(String metadataId) { metadataRepoId = metadataId; }

    public RepositoryImplConfig getMetadataConfig() {

        SailImplConfig sailConfig = new SEVODInferencerConfig(
                new ForwardChainingRDFSInferencerConfig(
                        new MemoryStoreConfig()));


        return new SailRepositoryConfig(sailConfig);
    }

    public List<String> getInitialFiles() {
        return null;
    }

    public void setInitialFiles(List<String> files) { }
}
