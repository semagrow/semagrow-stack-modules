package eu.semagrow.stack.modules.selector;

import com.fluidops.fedx.EndpointManager;
import com.fluidops.fedx.FedX;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.cache.Cache;
import com.fluidops.fedx.structures.Endpoint;
import eu.semagrow.stack.modules.api.source.SourceMetadata;
import eu.semagrow.stack.modules.api.source.SourceSelector;
import org.aksw.simba.hibiscus.HibiscusSourceSelection;
import org.aksw.sparql.query.algebra.helpers.BasicGraphPatternExtractor;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.repository.RepositoryException;

import java.util.*;

/**
 * Created by angel on 15/6/2015.
 */
public class HibiscusSourceSelector implements SourceSelector {

    private HibiscusSourceSelection impl;
    private Cache cache;
    private List<Endpoint> members;

    public HibiscusSourceSelector() {

        FedX fed = FederationManager.getInstance().getFederation();
        members = fed.getMembers();
        cache =FederationManager.getInstance().getCache();

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
            HashMap<Integer, List<StatementPattern>> bgpGrps =  generateBgpGroups(expr);
            return toSourceMetadata(this.impl.performSourceSelection(bgpGrps));
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<SourceMetadata> toSourceMetadata(Map<StatementPattern, List<StatementSource>> lst)
    {
        List<SourceMetadata> metadata = new LinkedList<>();

        for (StatementPattern pattern : lst.keySet()) {
            List<StatementSource> sources = lst.get(pattern);
            if (!sources.isEmpty()) {
                for (StatementSource src : sources) {
                    URI endpoint = toURI(src);
                    metadata.add(new SourceMetadata() {
                        @Override
                        public List<URI> getEndpoints() {
                            return Collections.singletonList(endpoint);
                        }

                        @Override
                        public StatementPattern original() {
                            return pattern;
                        }

                        @Override
                        public StatementPattern target() {
                            return pattern;
                        }

                        @Override
                        public Collection<URI> getSchema(String var) {
                            return null;
                        }

                        @Override
                        public boolean isTransformed() {
                            return false;
                        }

                        @Override
                        public double getSemanticProximity() {
                            return 0;
                        }
                    });
                }
            }
        }
        return metadata;
    }

    private URI toURI(StatementSource src)
    {
        String endpointId = src.getEndpointID();

        return ValueFactoryImpl.getInstance().createURI(
                EndpointManager.getEndpointManager().getEndpoint(endpointId).getEndpoint() );
    }

    private HashMap<Integer, List<StatementPattern>> generateBgpGroups(TupleExpr expr) {

        HashMap<Integer, List<StatementPattern>> bgpGrps = new HashMap<Integer, List<StatementPattern>>();
        int grpNo = 0;

        TupleExpr e = new QueryRoot(expr.clone());

        for (TupleExpr bgp : BasicGraphPatternExtractor.process(e)) {
            List<StatementPattern> patterns = StatementPatternCollector.process(bgp);
            bgpGrps.put(grpNo, patterns );
            grpNo++;
        }
        return bgpGrps;
    }
}