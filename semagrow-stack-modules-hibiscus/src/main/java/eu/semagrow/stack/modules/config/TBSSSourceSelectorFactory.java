package eu.semagrow.stack.modules.config;

import com.fluidops.fedx.exception.FedXRuntimeException;
import eu.semagrow.stack.modules.api.config.SourceSelectorConfigException;
import eu.semagrow.stack.modules.api.config.SourceSelectorFactory;
import eu.semagrow.stack.modules.api.config.SourceSelectorImplConfig;
import eu.semagrow.stack.modules.api.source.SourceSelector;
import eu.semagrow.stack.modules.selector.TBSSSourceSelector;
import org.aksw.simba.quetzal.configuration.QuetzalConfig;

/**
 * Created by angel on 24/6/2015.
 */
public class TBSSSourceSelectorFactory implements SourceSelectorFactory
{

    public static String SRCSELECTOR_TYPE = "aksw:tbss";

    public SourceSelectorImplConfig getConfig()
    {
        return new HibiscusSourceSelectorImplConfig(SRCSELECTOR_TYPE);
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
                try {
                    QuetzalConfig.initialize(summaries, mode, commonPredThreshold);
                } catch (FedXRuntimeException e) { }

                return new TBSSSourceSelector();

            } catch (Exception e) {
                throw new SourceSelectorConfigException(e);
            }
        } else {
            throw new SourceSelectorConfigException("SourceSelectorImplConfig is not of appropriate instance");
        }
    }

}
