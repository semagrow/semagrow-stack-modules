package eu.semagrow.stack.modules.sails.semagrow.evaluation;

import eu.semagrow.stack.modules.api.evaluation.QueryExecutor;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.QueryEvaluationInterceptor;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.interceptors.InterceptingEvaluationStrategy;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by angel on 6/27/14.
 */
public class InterceptingEvaluationStrategyImpl extends EvaluationStrategyImpl
    implements InterceptingEvaluationStrategy {

    private List<QueryEvaluationInterceptor> interceptors = new LinkedList<QueryEvaluationInterceptor>();

    public InterceptingEvaluationStrategyImpl(QueryExecutor queryExecutor, ValueFactory vf) {
        super(queryExecutor, vf);
    }

    public InterceptingEvaluationStrategyImpl(QueryExecutor queryExecutor) {
        super(queryExecutor);
    }

    public void addEvaluationInterceptor(QueryEvaluationInterceptor interceptor) {
        if (!interceptors.contains(interceptor))
            interceptors.add(interceptor);
    }

    public void removeEvaluationInterceptor(QueryEvaluationInterceptor interceptor) {
        if (interceptors.contains(interceptor))
            interceptors.remove(interceptor);
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException>
        evaluate(TupleExpr expr, BindingSet bindings) throws QueryEvaluationException
    {

        CloseableIteration<BindingSet, QueryEvaluationException> result =
                super.evaluate(expr,bindings);

        if (!interceptors.isEmpty()) {
            for (QueryEvaluationInterceptor interceptor : interceptors)
                result = interceptor.afterEvaluation(expr, bindings, result);
        }

        return result;
    }
}
