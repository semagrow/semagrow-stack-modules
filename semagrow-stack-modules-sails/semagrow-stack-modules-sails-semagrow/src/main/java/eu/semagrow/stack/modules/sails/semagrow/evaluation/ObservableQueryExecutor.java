package eu.semagrow.stack.modules.sails.semagrow.evaluation;

import eu.semagrow.stack.modules.api.evaluation.QueryEvaluationSession;
import eu.semagrow.stack.modules.api.evaluation.QueryExecutor;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.iteration.ObservingIteration;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.iterator.CollectionIteration;
import java.util.*;

/**
 * Created by angel on 6/20/14.
 */
public class ObservableQueryExecutor extends QueryExecutorWrapper {

    private QueryEvaluationSession session;

    public ObservableQueryExecutor(QueryEvaluationSession session, QueryExecutor executor) {
        super(executor);
        this.session = session;
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException>
        evaluate(URI endpoint, TupleExpr expr, BindingSet bindings)
            throws QueryEvaluationException {

        QueryMetadata metadata = createMetadata(endpoint, expr, bindings.getBindingNames());
        return observeIteration(metadata, super.evaluate(endpoint, expr, bindings));
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException>
        evaluate(URI endpoint, TupleExpr expr, CloseableIteration<BindingSet, QueryEvaluationException> bindingIter)
            throws QueryEvaluationException {
        List<BindingSet> bindings = Iterations.asList(bindingIter);

        bindingIter = new CollectionIteration<BindingSet, QueryEvaluationException>(bindings);

        Set<String> bindingNames = (bindings.size() == 0) ? new HashSet<String>() : bindings.get(0).getBindingNames();

        QueryMetadata metadata = createMetadata(endpoint, expr, bindingNames);

        return observeIteration(metadata, super.evaluate(endpoint, expr, bindingIter));
    }

    public CloseableIteration<BindingSet, QueryEvaluationException>
        observeIteration(QueryMetadata metadata, CloseableIteration<BindingSet, QueryEvaluationException> iter) {
        return new QueryObserver(metadata, iter);
    }

    protected QueryMetadata createMetadata(URI endpoint, TupleExpr expr, Set<String> bindingNames) {
        return new QueryMetadata(this.session, endpoint, expr, bindingNames);
    }

    private class QueryMetadata {

        private QueryEvaluationSession session;

        private TupleExpr query;

        private URI endpoint;

        private List<String> bindingNames;

        public QueryMetadata(QueryEvaluationSession session, URI endpoint, TupleExpr query) {
            this.session = session;
            this.endpoint = endpoint;
            this.query = query;
            this.bindingNames = new LinkedList<String>();
        }

        public QueryMetadata(QueryEvaluationSession session, URI endpoint, TupleExpr query, Collection<String> bindingNames) {
            this.session = session;
            this.endpoint = endpoint;
            this.query = query;
            this.bindingNames = new LinkedList<String>(bindingNames);
        }

        public URI getEndpoint() { return endpoint; }

        public TupleExpr getQuery() { return query; }

        public QueryEvaluationSession getSession() { return session; }

        public List<String> getBindingNames() { return bindingNames; }
    }

    protected class QueryObserver extends ObservingIteration<BindingSet,QueryEvaluationException> {

        private QueryMetadata metadata;

        public QueryObserver(QueryMetadata metadata, Iteration<BindingSet, QueryEvaluationException> iter) {
            super(iter);
            this.metadata = metadata;
        }

        @Override
        public void observe(BindingSet bindings) {

        }

        @Override
        public void observeExceptionally(QueryEvaluationException e) {

        }
    }
}
