package eu.semagrow.stack.modules.config;

import eu.semagrow.stack.modules.api.config.SourceSelectorConfigException;
import eu.semagrow.stack.modules.api.config.SourceSelectorFactory;
import eu.semagrow.stack.modules.api.config.SourceSelectorImplConfig;
import eu.semagrow.stack.modules.api.source.SourceSelector;
import eu.semagrow.stack.modules.selector.HibiscusSourceSelector;
import org.aksw.simba.hibiscus.HibiscusConfig;

/**
 * Created by angel on 15/6/2015.
 */
public class HibiscusSourceSelectorFactory implements SourceSelectorFactory
{

    public static String SRCSELECTOR_TYPE = "aksw:hibiscus";

    public SourceSelectorImplConfig getConfig()
    {
        return new HibiscusSourceSelectorImplConfig();
    }

    public String getType() { return SRCSELECTOR_TYPE; }

    public SourceSelector getSourceSelector(SourceSelectorImplConfig config)
            throws SourceSelectorConfigException
    {
        if (config instanceof HibiscusSourceSelectorImplConfig)
        {
            HibiscusSourceSelectorImplConfig hibiscusConfig = (HibiscusSourceSelectorImplConfig)config;

            String summaries = hibiscusConfig.getSummariesFile();
            String mode = hibiscusConfig.getMode();
            double commonPredThreshold = hibiscusConfig.getCommonPredicateThreshold();

           try {
               HibiscusConfig.initialize(summaries, mode, commonPredThreshold);
               return new HibiscusSourceSelector();
            } catch (Exception e) {
                throw new SourceSelectorConfigException(e);
            }
        } else {
            throw new SourceSelectorConfigException("SourceSelectorImplConfig is not of appropriate instance");
        }
    }

}
