package eu.semagrow.stack.modules.sails.semagrow.evaluation;

import eu.semagrow.stack.modules.api.evaluation.*;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.base.FederatedEvaluationStrategyWrapper;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.base.QueryEvaluationSessionImplBase;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.MeasuringIteration;
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

    public QueryEvaluationSession
        createSession(TupleExpr expr, Dataset dataset, BindingSet bindings)
    {
        return new QueryEvaluationSessionImpl();
    }

    protected class QueryEvaluationSessionImpl extends QueryEvaluationSessionImplBase {

        private MeasuringIteration<BindingSet,QueryEvaluationException> measurement;

        protected FederatedEvaluationStrategy getEvaluationStrategyInternal() {
            QueryExecutor queryExecutor = getQueryExecutor();
            FederatedEvaluationStrategy evaluationStrategy = new EvaluationStrategyImpl(queryExecutor);
            evaluationStrategy = new MonitoringEvaluationStrategy(evaluationStrategy);
            return evaluationStrategy;
        }

        protected QueryExecutor getQueryExecutor() {
            return new QueryExecutorImpl();
        }

        @Override
        public void closeSession(){

            if (measurement != null) {

                logger.info("Total rows: {}", measurement.getCount());
                logger.info("Total execution time: {}", measurement.getRunningTime());
                logger.info("Average consumption rate: {}", measurement.getAverageConsumedRate());
                logger.info("Average production  rate: {}", measurement.getAverageProducedRate());
            }
        }

        protected class MonitoringEvaluationStrategy extends FederatedEvaluationStrategyWrapper {

            public MonitoringEvaluationStrategy(FederatedEvaluationStrategy wrapped) {
                super(wrapped);
            }

            @Override
            public CloseableIteration<BindingSet,QueryEvaluationException>
                evaluate(TupleExpr expr, BindingSet bindings)
                    throws QueryEvaluationException {

                CloseableIteration<BindingSet,QueryEvaluationException> result =
                        super.evaluate(expr,bindings);
                measurement = new MeasuringIteration<BindingSet,QueryEvaluationException>(result);
                result = measurement;
                return result;
            }
        }
    }
}
