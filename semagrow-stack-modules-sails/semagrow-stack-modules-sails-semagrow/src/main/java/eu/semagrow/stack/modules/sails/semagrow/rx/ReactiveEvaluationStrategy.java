package eu.semagrow.stack.modules.sails.semagrow.rx;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.reactivestreams.Publisher;

/**
 * Created by angel on 3/26/15.
 */
public interface ReactiveEvaluationStrategy {

    Publisher<BindingSet> evaluateReactive(TupleExpr expr, BindingSet bindings) throws QueryEvaluationException;

    Value evaluate(ValueExpr expr, BindingSet bindings) throws ValueExprEvaluationException, QueryEvaluationException;

    boolean isTrue(ValueExpr expr, BindingSet bindings) throws ValueExprEvaluationException, QueryEvaluationException;

}
