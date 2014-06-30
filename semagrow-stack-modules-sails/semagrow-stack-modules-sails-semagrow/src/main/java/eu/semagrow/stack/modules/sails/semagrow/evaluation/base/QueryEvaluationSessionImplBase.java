package eu.semagrow.stack.modules.sails.semagrow.evaluation.base;

import eu.semagrow.stack.modules.api.evaluation.EvaluationStrategy;
import eu.semagrow.stack.modules.api.evaluation.FederatedEvaluationStrategy;
import eu.semagrow.stack.modules.api.evaluation.QueryEvaluationSession;
import eu.semagrow.stack.modules.api.evaluation.SessionId;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.SessionUUID;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.AbstractEvaluationSessionAwareInterceptor;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.QueryEvaluationInterceptor;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.IterationWrapper;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by angel on 6/12/14.
 */
public abstract class QueryEvaluationSessionImplBase implements QueryEvaluationSession {

    private SessionUUID id;

    public QueryEvaluationSessionImplBase() {
        this.id = SessionUUID.createUniqueId();
    }

    public SessionId getSessionId() { return id; }

    public FederatedEvaluationStrategy getEvaluationStrategy() {
        FederatedEvaluationStrategy actualStrategy = getEvaluationStrategyInternal();
        return new SessionAwareEvaluationStrategy(actualStrategy);
    }

    protected abstract FederatedEvaluationStrategy getEvaluationStrategyInternal();

    public void initializeSession() { }

    public void closeSession() { }

    protected class SessionAwareInterceptor
            extends AbstractEvaluationSessionAwareInterceptor
            implements QueryEvaluationInterceptor {

        public CloseableIteration<BindingSet, QueryEvaluationException>
            afterEvaluation(TupleExpr expr, BindingSet bindings, CloseableIteration<BindingSet, QueryEvaluationException> result) {
            return afterEvaluation(expr,result);
        }

        public CloseableIteration<BindingSet, QueryEvaluationException>
            afterEvaluation(TupleExpr expr, CloseableIteration<BindingSet, QueryEvaluationException> bindings, CloseableIteration<BindingSet, QueryEvaluationException> result) {
            return afterEvaluation(expr,result);
        }

        protected CloseableIteration<BindingSet, QueryEvaluationException>
            afterEvaluation(TupleExpr expr, CloseableIteration<BindingSet, QueryEvaluationException> result) {

            if (isRoot(expr))
                result = new SessionAwareIteration(result);

            return result;
        }

        private boolean isRoot(TupleExpr expr) { return expr.getParentNode() == null; }
    }

    protected class SessionAwareIteration extends IterationWrapper<BindingSet,QueryEvaluationException> {

        public SessionAwareIteration(CloseableIteration<BindingSet,QueryEvaluationException> iter) {
            super(iter);
        }

        @Override
        public void handleClose() throws QueryEvaluationException {
            super.handleClose();
            closeSession();
        }
    }

    protected class SessionAwareEvaluationStrategy extends FederatedEvaluationStrategyWrapper {

        public SessionAwareEvaluationStrategy(FederatedEvaluationStrategy evaluationStrategy) {

            super(evaluationStrategy);
        }

        @Override
        public CloseableIteration<BindingSet,QueryEvaluationException>
            evaluate(TupleExpr expr, BindingSet bindings) throws QueryEvaluationException {

            initializeSession();
            return new SessionAwareIteration(super.evaluate(expr,bindings));
        }


    }

}
