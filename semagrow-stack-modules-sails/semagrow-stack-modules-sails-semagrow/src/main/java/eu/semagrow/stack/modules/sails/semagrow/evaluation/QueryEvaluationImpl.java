package eu.semagrow.stack.modules.sails.semagrow.evaluation;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import eu.semagrow.stack.modules.api.evaluation.*;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.base.FederatedQueryEvaluationSessionImplBase;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.file.MaterializationManager;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.InterceptingQueryExecutorWrapper;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.QueryExecutionInterceptor;
import eu.semagrow.stack.modules.querylog.api.QueryLogHandler;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.QueryLogInterceptor;

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

    private MaterializationManager materializationManager;

    private QueryLogHandler queryLogHandler;

    private ExecutorService executorService;

    public QueryEvaluationImpl(MaterializationManager manager,
                               QueryLogHandler queryLogHandler,
                               ExecutorService executor) {
        this.materializationManager = manager;
        this.queryLogHandler = queryLogHandler;
        this.executorService = executor;
    }

    public MaterializationManager getMaterializationManager() {
        return materializationManager;
    }

    public QueryLogHandler getQFRHandler() {
        return queryLogHandler;
    }


    public FederatedQueryEvaluationSession
        createSession(TupleExpr expr, Dataset dataset, BindingSet bindings)
    {
        return new FederatedQueryEvaluationSessionImpl(executorService);
    }

    /**
     * Handles the lifetime of EvaluationStrategy and QueryExecutor
     * Also handles the injection of available interceptors
     */
    protected class FederatedQueryEvaluationSessionImpl
            extends FederatedQueryEvaluationSessionImplBase {

        private ExecutorService executor;

        public FederatedQueryEvaluationSessionImpl(ExecutorService executor) {
            this.executor = executor;
        }

        protected FederatedEvaluationStrategy getEvaluationStrategyInternal() {
            return new InterceptingEvaluationStrategyImpl(getQueryExecutor(), getExecutor());
        }

        protected QueryExecutor getQueryExecutorInternal() {
            return new InterceptingQueryExecutorWrapper(new QueryExecutorImpl());
        }

        protected ExecutorService getExecutor() { return executor; }

        protected MaterializationManager getMaterializationManager() {
            return QueryEvaluationImpl.this.getMaterializationManager();
        }

        protected QueryLogHandler getQFRHandler() {
            return QueryEvaluationImpl.this.getQFRHandler();
        }
        
        @Override
        protected Collection<QueryExecutionInterceptor> getQueryExecutorInterceptors() {
        	Collection<QueryExecutionInterceptor> interceptors = super.getQueryExecutorInterceptors();
        	//interceptors.add(new ObservingInterceptor());
            //interceptors.add(new QueryLogInterceptor(QueryLogRecordFactoryImpl.getInstance(), getQFRHandler(), this.getMaterializationManager()));
            interceptors.add(new QueryLogInterceptor(getQFRHandler(), this.getMaterializationManager()));
        	return interceptors;
        }

        @Override
        public void closeSession(){
            logger.debug("Session " + getSessionId() + " closed");
        }
    }
}
