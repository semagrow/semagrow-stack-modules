package eu.semagrow.stack.modules.sails.semagrow.config;

import org.openrdf.repository.config.RepositoryImplConfigBase;

/**
 * Created by angel on 6/10/14.
 */
public class SemagrowRepositoryConfig extends RepositoryImplConfigBase {

    public SemagrowRepositoryConfig() { super(SemagrowRepositoryFactory.REPOSITORY_TYPE); }

    public SemagrowSailConfig getSemagrowSailConfig() { return new SemagrowSailConfig(); }

}
