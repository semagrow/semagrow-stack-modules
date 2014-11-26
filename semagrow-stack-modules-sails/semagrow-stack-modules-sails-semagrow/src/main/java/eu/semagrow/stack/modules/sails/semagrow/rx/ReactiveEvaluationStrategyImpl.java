package eu.semagrow.stack.modules.sails.semagrow.rx;

import info.aduna.iteration.Iteration;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.ExternalSet;
import org.openrdf.query.algebra.evaluation.util.OrderComparator;
import org.openrdf.query.algebra.evaluation.util.ValueComparator;
import org.openrdf.util.iterators.Iterators;
import rx.Observable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by angel on 11/22/14.
 */
public class ReactiveEvaluationStrategyImpl extends EvaluationStrategyImpl {


    public ReactiveEvaluationStrategyImpl(TripleSource tripleSource, Dataset dataset) {
        super(tripleSource, dataset);
    }

    public ReactiveEvaluationStrategyImpl(TripleSource tripleSource) {
        super(tripleSource);
    }

    public Observable<BindingSet> evaluateReactive(TupleExpr expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        if (expr instanceof StatementPattern) {
            return evaluateReactive((StatementPattern) expr, bindings);
        }
        else if (expr instanceof UnaryTupleOperator) {
            return evaluateReactive((UnaryTupleOperator) expr, bindings);
        }
        else if (expr instanceof BinaryTupleOperator) {
            return evaluateReactive((BinaryTupleOperator) expr, bindings);
        }
        else if (expr instanceof SingletonSet) {
            return evaluateReactive((SingletonSet) expr, bindings);
        }
        else if (expr instanceof EmptySet) {
            return evaluateReactive((EmptySet) expr, bindings);
        }
        else if (expr instanceof ExternalSet) {
            return evaluateReactive((ExternalSet) expr, bindings);
        }
        else if (expr instanceof ZeroLengthPath) {
            return evaluateReactive((ZeroLengthPath) expr, bindings);
        }
        else if (expr instanceof ArbitraryLengthPath) {
            return evaluateReactive((ArbitraryLengthPath) expr, bindings);
        }
        else if (expr instanceof BindingSetAssignment) {
            return evaluateReactive((BindingSetAssignment) expr, bindings);
        }
        else if (expr == null) {
            throw new IllegalArgumentException("expr must not be null");
        }
        else {
            throw new QueryEvaluationException("Unsupported tuple expr type: " + expr.getClass());
        }
    }


    public Observable<BindingSet> evaluateReactive(UnaryTupleOperator expr, BindingSet bindings)
            throws QueryEvaluationException
    {

        if (expr instanceof Projection) {
            return evaluateReactive((Projection) expr, bindings);
        }
        else if (expr instanceof MultiProjection) {
            return evaluateReactive((MultiProjection) expr, bindings);
        }
        else if (expr instanceof Filter) {
            return evaluateReactive((Filter) expr, bindings);
        }
        else if (expr instanceof Extension) {
            return evaluateReactive((Extension) expr, bindings);
        }
        else if (expr instanceof Group) {
            return evaluateReactive((Group) expr, bindings);
        }
        else if (expr instanceof Order) {
            return evaluateReactive((Order) expr, bindings);
        }
        else if (expr instanceof Slice) {
            return evaluateReactive((Slice) expr, bindings);
        }
        else if (expr instanceof Distinct) {
            return evaluateReactive((Distinct) expr, bindings);
        }
        else if (expr instanceof Reduced) {
            return evaluateReactive((Reduced) expr, bindings);
        }
        else if (expr instanceof Service) {
            return evaluateReactive((Service) expr, bindings);
        }
        else if (expr instanceof QueryRoot) {
            return evaluateReactive(expr.getArg(), bindings);
        }
        else if (expr instanceof DescribeOperator) {
            return evaluateReactive((DescribeOperator) expr, bindings);
        }
        else if (expr == null) {
            throw new IllegalArgumentException("expr must not be null");
        }
        else {
            throw new QueryEvaluationException("Unsupported tuple expr type: " + expr.getClass());
        }
    }

    public Observable<BindingSet> evaluateReactive(BinaryTupleOperator expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        if (expr instanceof Union) {
            return evaluateReactive((Union) expr, bindings);
        }
        else if (expr instanceof Join) {
            return evaluateReactive((Join) expr, bindings);
        }
        else if (expr instanceof LeftJoin) {
            return evaluateReactive((LeftJoin) expr, bindings);
        }
        else if (expr instanceof Intersection) {
            return evaluateReactive((Intersection) expr, bindings);
        }
        else if (expr instanceof Difference) {
            return evaluateReactive((Difference) expr, bindings);
        }
        else if (expr == null) {
            throw new IllegalArgumentException("expr must not be null");
        }
        else {
            throw new QueryEvaluationException("Unsupported tuple expr type: " + expr.getClass());
        }
    }

    public Observable<BindingSet> evaluateReactive(SingletonSet expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return Observable.just(bindings);
    }

    public Observable<BindingSet> evaluateReactive(EmptySet expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return Observable.empty();
    }

    public Observable<BindingSet> evaluateReactive(StatementPattern expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(evaluate(expr, bindings));
    }

    public Observable<BindingSet> evaluateReactive(BindingSetAssignment expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        final Iterator<BindingSet> iter = expr.getBindingSets().iterator();

        final List<BindingSet> blist = new LinkedList();
        Iterators.addAll(iter, blist);

        return Observable.from(blist)
                .map((b) -> {
                    QueryBindingSet bb = new QueryBindingSet(bindings);
                    bb.addAll(b);
                    return bb;
                });
    }

    public Observable<BindingSet> evaluateReactive(ExternalSet expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(expr.evaluate(bindings));
    }


    public Observable<BindingSet> evaluateReactive(ZeroLengthPath expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(this.evaluate(expr, bindings));
    }


    public Observable<BindingSet> evaluateReactive(ArbitraryLengthPath expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(this.evaluate(expr, bindings));
    }

    public Observable<BindingSet> evaluateReactive(Filter expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        QueryBindingSet scopeBindings = new QueryBindingSet(bindings);

        return evaluateReactive(expr.getArg(), bindings)
                    .filter((b) ->  {
                        try {
                            return this.isTrue(expr.getCondition(), scopeBindings);
                        }catch(QueryEvaluationException /*| ValueExprEvaluationException */ e) {
                            return false;
                        } });
    }

    public Observable<BindingSet> evaluateReactive(Projection expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return evaluateReactive(expr.getArg(), bindings)
                .map((b) -> project(expr.getProjectionElemList(), b, bindings));
    }

    public Observable<BindingSet> evaluateReactive(Extension expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return evaluateReactive(expr.getArg(), bindings)
                .concatMap((b) -> {
                    try {
                        return Observable.just(extend(expr.getElements(), b));
                    } catch (Exception e) {
                        return Observable.error(e);
                    }
                }).onErrorResumeNext(Observable::error);
    }

    public Observable<BindingSet> evaluateReactive(Union expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return Observable.merge(
                this.evaluateReactive(expr.getLeftArg(), bindings),
                this.evaluateReactive(expr.getRightArg(), bindings));
    }

    public Observable<BindingSet> evaluateReactive(Join expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return evaluateReactive(expr.getLeftArg(), bindings)
                    .concatMap( (b) -> {
                        try {
                            return this.evaluateReactive(expr.getRightArg(), b);
                        } catch (Exception e) { return Observable.error(e); } });
    }

    public Observable<BindingSet> evaluateReactive(Group expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return null;
    }

    public Observable<BindingSet> evaluateReactive(Order expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        ValueComparator vcmp = new ValueComparator();
        OrderComparator cmp = new OrderComparator(this, expr, vcmp);
        return evaluateReactive(expr.getArg(), bindings)
                .toSortedList(cmp::compare)
                .flatMap(Observable::from);
    }

    public Observable<BindingSet> evaluateReactive(Slice expr, BindingSet bindings)
            throws QueryEvaluationException {
        Observable<BindingSet> result = evaluateReactive(expr.getArg(), bindings);

        if (expr.hasOffset())
            result = result.skip((int) expr.getOffset());

        if (expr.hasLimit())
            result = result.take((int) expr.getLimit());

        return result;
    }

    public Observable<BindingSet> evaluateReactive(Distinct expr, BindingSet bindings)
            throws QueryEvaluationException {

        return evaluateReactive(expr.getArg(), bindings).distinct();
    }

    public Observable<BindingSet> evaluateReactive(Reduced expr, BindingSet bindings)
            throws QueryEvaluationException {

        return evaluateReactive(expr.getArg(), bindings).distinctUntilChanged();
    }

    public Observable<BindingSet> evaluateReactive(DescribeOperator expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(this.evaluate(expr, bindings));
    }

    public Observable<BindingSet> evaluateReactive(Intersection expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(this.evaluate(expr, bindings));
    }


    public Observable<BindingSet> evaluateReactive(Difference expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(this.evaluate(expr, bindings));
    }


    public Observable<BindingSet> evaluateReactive(Service expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(this.evaluate(expr, bindings));
    }

    protected <T> Observable<T> fromIteration(Iteration<? extends T, ? extends Exception> it) {
        return Observable.create(new OnSubscribeFromIteration<T>(it));
    }


    public static BindingSet project(ProjectionElemList projElemList, BindingSet sourceBindings,
                                     BindingSet parentBindings)
    {
        QueryBindingSet resultBindings = new QueryBindingSet(parentBindings);

        for (ProjectionElem pe : projElemList.getElements()) {
            Value targetValue = sourceBindings.getValue(pe.getSourceName());
            if (targetValue != null) {
                // Potentially overwrites bindings from super
                resultBindings.setBinding(pe.getTargetName(), targetValue);
            }
        }

        return resultBindings;
    }

    public BindingSet extend(Collection<ExtensionElem> extElems, BindingSet sourceBindings)
            throws QueryEvaluationException
    {
        QueryBindingSet targetBindings = new QueryBindingSet(sourceBindings);

        for (ExtensionElem extElem : extElems) {
            ValueExpr expr = extElem.getExpr();
            if (!(expr instanceof AggregateOperator)) {
                try {
                    // we evaluate each extension element over the targetbindings, so that bindings from
                    // a previous extension element in this same extension can be used by other extension elements.
                    // e.g. if a projection contains (?a + ?b as ?c) (?c * 2 as ?d)
                    Value targetValue = evaluate(extElem.getExpr(), targetBindings);

                    if (targetValue != null) {
                        // Potentially overwrites bindings from super
                        targetBindings.setBinding(extElem.getName(), targetValue);
                    }
                } catch (ValueExprEvaluationException e) {
                    // silently ignore type errors in extension arguments. They should not cause the
                    // query to fail but just result in no additional binding.
                }
            }
        }

        return targetBindings;
    }
}
