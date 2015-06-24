package eu.semagrow.stack.modules.config;

import eu.semagrow.stack.modules.api.config.SourceSelectorConfigException;
import eu.semagrow.stack.modules.api.config.SourceSelectorImplConfigBase;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;

/**
 * Created by angel on 16/6/2015.
 */
public class HibiscusSourceSelectorImplConfig extends SourceSelectorImplConfigBase
{
    private String summariesFile;
    private String mode;
    private double commonPredicateThreshold;

    public HibiscusSourceSelectorImplConfig(String type) { super(type); }

    public String getSummariesFile() {
        return summariesFile;
    }

    public void setSummariesFile(String summariesFile) {
        this.summariesFile = summariesFile;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public double getCommonPredicateThreshold() {
        return commonPredicateThreshold;
    }

    public void setCommonPredicateThreshold(double commonPredicateThreshold) {
        this.commonPredicateThreshold = commonPredicateThreshold;
    }


    @Override
    public Resource export(Graph graph) {
        Resource node = super.export(graph);
        ValueFactory vf = graph.getValueFactory();

        if (mode != null)
            graph.add(node, HibiscusSchema.MODE, vf.createLiteral(mode));


        graph.add(node, HibiscusSchema.COMMONPREDTHREASHOLD, vf.createLiteral(commonPredicateThreshold));

        if (summariesFile != null)
            graph.add(node, HibiscusSchema.SUMMARIES, vf.createLiteral(summariesFile));

        return node;
    }

    @Override
    public void parse(Graph graph, Resource resource)
            throws SourceSelectorConfigException
    {
        try {
            Literal summariesLit = GraphUtil.getOptionalObjectLiteral(graph, resource, HibiscusSchema.SUMMARIES);
            if (summariesLit != null) {
                summariesFile = summariesLit.getLabel();
            }

            Literal modeLit = GraphUtil.getOptionalObjectLiteral(graph, resource, HibiscusSchema.MODE);

            if (modeLit != null) {
                mode = modeLit.getLabel();
            }

            Literal commonPredLit = GraphUtil.getOptionalObjectLiteral(graph, resource, HibiscusSchema.COMMONPREDTHREASHOLD);

            if (commonPredLit != null) {
                commonPredicateThreshold = commonPredLit.doubleValue();
            }

        } catch (GraphUtilException e) {
            throw new SourceSelectorConfigException(e);
        }

    }
}
