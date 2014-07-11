package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring;

import eu.semagrow.stack.modules.api.evaluation.QueryEvaluationSession;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.AbstractEvaluationSessionAwareInterceptor;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.QueryExecutionInterceptor;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.logging.LoggerWithQueue;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by angel on 6/30/14.
 * @author Angelos Charalampidis
 * @author Giannis Mouchakis
 */
public class ObservingInterceptor
        extends AbstractEvaluationSessionAwareInterceptor
        implements QueryExecutionInterceptor {
	
	BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1000000);
    LoggerWithQueue logWritter = new LoggerWithQueue(queue);
    Thread logWritterThread = new Thread(logWritter);
    
    final Logger logger = LoggerFactory.getLogger(ObservingInterceptor.class);
    

    public CloseableIteration<BindingSet, QueryEvaluationException>
        afterExecution(URI endpoint, TupleExpr expr, BindingSet bindings, CloseableIteration<BindingSet, QueryEvaluationException> result) {

        QueryMetadata metadata = createMetadata(endpoint, expr, bindings.getBindingNames());
        return observe(metadata, result);
    }

    public CloseableIteration<BindingSet, QueryEvaluationException>
        afterExecution(URI endpoint, TupleExpr expr, CloseableIteration<BindingSet, QueryEvaluationException> bindingIter, CloseableIteration<BindingSet, QueryEvaluationException> result) {

        List<BindingSet> bindings = Collections.<BindingSet>emptyList();

        try {
            bindings = Iterations.asList(bindingIter);
        } catch (Exception e) {

        }

//        bindingIter = new CollectionIteration<BindingSet, QueryEvaluationException>(bindings);

        Set<String> bindingNames = (bindings.size() == 0) ? new HashSet<String>() : bindings.get(0).getBindingNames();

        QueryMetadata metadata = createMetadata(endpoint, expr, bindingNames);

        return observe(metadata, result);
    }


    public CloseableIteration<BindingSet, QueryEvaluationException>
        observe(QueryMetadata metadata, CloseableIteration<BindingSet, QueryEvaluationException> iter) {

        return new QueryObserver(metadata, iter);
    }


    protected QueryMetadata createMetadata(URI endpoint, TupleExpr expr, Set<String> bindingNames) {
        return new QueryMetadata(this.getQueryEvaluationSession(), endpoint, expr, bindingNames);
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
            if ( ! logWritterThread.isAlive()) {
            	logWritterThread.start();
            }
        }

		@Override
		public void observe(BindingSet bindings) {
			try {
				queue.put(metadata.getSession().getSessionId().toString());
				queue.put(Long.toString(System.currentTimeMillis()).getBytes());
				queue.put(metadata.getEndpoint().toString());
				queue.put("@");
				queue.put(metadata.getQuery().toString());
				queue.put("@");
				queue.put(bindings.getBindingNames().size());
				queue.put(bindings.toString());
				queue.put(metadata.bindingNames.toString());
				for (String name : metadata.bindingNames) {
					queue.put(bindings.getValue(name).stringValue());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void observeExceptionally(QueryEvaluationException x) {
			//logWritter.finish();//TODO:log this? 
			
		}
		
        /*
        @Override
        public void handleClose() throws QueryEvaluationException {
        	super.handleClose();
        	logWritter.finish();
        }
        */
		
    }

}
