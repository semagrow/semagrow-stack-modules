package eu.semagrow.stack.modules.api.config;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;

/**
 * Created by angel on 11/1/14.
 */
public class SourceSelectorImplConfigBase implements SourceSelectorImplConfig {

    private String type;

    public SourceSelectorImplConfigBase() { }

    public SourceSelectorImplConfigBase(String type) { this(); setType(type); }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public void validate() throws SourceSelectorConfigException {

    }

    public Resource export(Graph graph) {
        BNode implNode = graph.getValueFactory().createBNode();
        graph.add(implNode, SemagrowSchema.SOURCESELECTOR, graph.getValueFactory().createLiteral(getType()));
        return implNode;
    }

    public void parse(Graph graph, Resource resource) throws SourceSelectorConfigException {

    }
}
