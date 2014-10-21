package eu.semagrow.stack.modules.sails.semagrow.evaluation;

import java.util.Collection;

import eu.semagrow.stack.modules.api.evaluation.*;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.base.FederatedQueryEvaluationSessionImplBase;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.file.ResultMaterializationManager;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.InterceptingQueryExecutorWrapper;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.QueryExecutionInterceptor;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.qfr.QueryObservingInterceptor;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.qfr.QueryRecordLogFactory;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.qfr.QueryRecordLogHandler;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by angel on 6/11/14.
 */
public class QueryEvaluationImpl implements FederatedQueryEvaluation {

    protected final Logger logger = LoggerFactory.getLogger(QueryEvaluationImpl.class);

    private ResultMaterializationManager materializationManager;

    private QueryRecordLogHandler queryRecordLogHandler;

    public QueryEvaluationImpl(ResultMaterializationManager manager,
                               QueryRecordLogHandler queryRecordLogHandler) {
        this.materializationManager = manager;
        this.queryRecordLogHandler = queryRecordLogHandler;
    }

    public ResultMaterializationManager getMaterializationManager() {
        return materializationManager;
    }

    public QueryRecordLogHandler getQFRHandler() {
        return queryRecordLogHandler;
    }


    public FederatedQueryEvaluationSession
        createSession(TupleExpr expr, Dataset dataset, BindingSet bindings)
    {
        return new FederatedQueryEvaluationSessionImpl();
    }

    /**
     * Handles the lifetime of EvaluationStrategy and QueryExecutor
     * Also handles the injection of available interceptors
     */
    protected class FederatedQueryEvaluationSessionImpl
            extends FederatedQueryEvaluationSessionImplBase {

        protected FederatedEvaluationStrategy getEvaluationStrategyInternal() {
            return new InterceptingEvaluationStrategyImpl(getQueryExecutor());
        }

        protected QueryExecutor getQueryExecutorInternal() {
            return new InterceptingQueryExecutorWrapper(new QueryExecutorImpl());
        }

        protected ResultMaterializationManager getMaterializationManager() {
            return QueryEvaluationImpl.this.getMaterializationManager();
        }

        protected QueryRecordLogHandler getQFRHandler() {
            return QueryEvaluationImpl.this.getQFRHandler();
        }
        
        @Override
        protected Collection<QueryExecutionInterceptor> getQueryExecutorInterceptors() {
        	Collection<QueryExecutionInterceptor> interceptors = super.getQueryExecutorInterceptors();
        	//interceptors.add(new ObservingInterceptor());
            interceptors.add(new QueryObservingInterceptor(getQFRHandler(), this.getMaterializationManager()));
        	return interceptors;
        }

        @Override
        public void closeSession(){
            logger.debug("Session " + getSessionId() + " closed");
        }
    }
}
