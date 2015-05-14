package eu.semagrow.stack.modules.sails.semagrow.evaluation;

import java.util.Collection;

import eu.semagrow.stack.modules.api.evaluation.*;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.base.FederatedQueryEvaluationSessionImplBase;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.file.MaterializationManager;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.InterceptingQueryExecutorWrapper;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.QueryExecutionInterceptor;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.querylog.QueryLogHandler;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.querylog.QueryLogInterceptor;

import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.querylog.impl.QueryLogRecordFactoryImpl;
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

    public QueryEvaluationImpl(MaterializationManager manager,
                               QueryLogHandler queryLogHandler) {
        this.materializationManager = manager;
        this.queryLogHandler = queryLogHandler;
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
