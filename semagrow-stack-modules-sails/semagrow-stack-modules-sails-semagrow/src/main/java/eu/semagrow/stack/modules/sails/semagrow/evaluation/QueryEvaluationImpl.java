package eu.semagrow.stack.modules.sails.semagrow.evaluation;

import java.util.Collection;

import eu.semagrow.stack.modules.api.evaluation.*;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.base.FederatedEvaluationStrategyWrapper;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.base.FederatedQueryEvaluationSessionImplBase;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.base.QueryEvaluationSessionImplBase;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.InterceptingQueryExecutorWrapper;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.QueryExecutionInterceptor;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.MeasuringIteration;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.ObservingInterceptor;
import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by angel on 6/11/14.
 */
public class QueryEvaluationImpl implements QueryEvaluation {

    protected final Logger logger = LoggerFactory.getLogger(QueryEvaluationImpl.class);

    public FederatedQueryEvaluationSession
        createSession(TupleExpr expr, Dataset dataset, BindingSet bindings) {
        return new FederatedQueryEvaluationSessionImpl();
    }

    protected class FederatedQueryEvaluationSessionImpl extends FederatedQueryEvaluationSessionImplBase {

        protected FederatedEvaluationStrategy getEvaluationStrategyInternal() {
            return new InterceptingEvaluationStrategyImpl(getQueryExecutor());
        }

        protected QueryExecutor getQueryExecutorInternal() {
            return new InterceptingQueryExecutorWrapper(new QueryExecutorImpl());
        }
        
        @Override
        protected Collection<QueryExecutionInterceptor> getQueryExecutorInterceptors() {
        	Collection<QueryExecutionInterceptor> interceptors = super.getQueryExecutorInterceptors();
        	interceptors.add(new ObservingInterceptor());
        	return interceptors;
        }

        @Override
        public void closeSession(){
            logger.debug("Session " + getSessionId() + " closed");
        }
    }
}
