package eu.semagrow.stack.modules;

import com.fluidops.fedx.FedX;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.cache.Cache;
import com.fluidops.fedx.structures.Endpoint;
import eu.semagrow.stack.modules.api.source.SourceMetadata;
import eu.semagrow.stack.modules.api.source.SourceSelector;
import org.aksw.simba.hibiscus.HibiscusConfig;
import org.aksw.simba.hibiscus.HibiscusSourceSelection;
import org.aksw.sparql.query.algebra.helpers.BGPGroupGenerator;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.repository.RepositoryException;

import java.util.HashMap;
import java.util.List;

/**
 * Created by angel on 15/6/2015.
 */
public class HibiscusSourceSelector implements SourceSelector {

    private HibiscusSourceSelection impl;
    private Cache cache;
    private List<Endpoint> members;

    private String mode = "Index_dominant";  //{ASK_dominant, Index_dominant}

    private double commonPredThreshold = 0.33 ;  //considered a predicate as common predicate if it is presenet in 33% available data sources


    public HibiscusSourceSelector(String FedSummaries) throws Exception {
        FedX fed = FederationManager.getInstance().getFederation();
        List<Endpoint> members = fed.getMembers();
        Cache cache =FederationManager.getInstance().getCache();
        HibiscusConfig.initialize(FedSummaries, mode, commonPredThreshold);
    }

    @Override
    public List<SourceMetadata> getSources(StatementPattern pattern, Dataset dataset, BindingSet bindings) {
        return null;
    }

    @Override
    public List<SourceMetadata> getSources(Iterable<StatementPattern> patterns, Dataset dataset, BindingSet bindings) {
        return null;
    }

    @Override
    public List<SourceMetadata> getSources(TupleExpr expr, Dataset dataset, BindingSet bindings)
    {
        String query = null;
        try {
            this.impl = new HibiscusSourceSelection(members, cache, query);
            HashMap<Integer, List<StatementPattern>> bgpGrps =  BGPGroupGenerator.generateBgpGroups(query);
            this.impl.performSourceSelection(bgpGrps);
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }


        return null;
    }
}
