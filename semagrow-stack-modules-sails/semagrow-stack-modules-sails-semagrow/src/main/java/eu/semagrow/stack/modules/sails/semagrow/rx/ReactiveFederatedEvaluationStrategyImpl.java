package eu.semagrow.stack.modules.sails.semagrow.rx;

import eu.semagrow.stack.modules.sails.semagrow.algebra.*;
import eu.semagrow.stack.modules.sails.semagrow.optimizer.Plan;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.TripleSource;
import rx.Observable;

import java.util.List;
import java.util.Set;

/**
 * Created by angel on 11/26/14.
 */
public class ReactiveFederatedEvaluationStrategyImpl extends ReactiveEvaluationStrategyImpl {

    public ReactiveQueryExecutorImpl queryExecutor;


    public ReactiveFederatedEvaluationStrategyImpl(ReactiveQueryExecutorImpl queryExecutor, final ValueFactory vf) {
        super(new TripleSource() {
            public CloseableIteration<? extends Statement, QueryEvaluationException>
            getStatements(Resource resource, URI uri, Value value, Resource... resources) throws QueryEvaluationException {
                throw new UnsupportedOperationException("Statement retrieval is not supported");
            }

            public ValueFactory getValueFactory() {
                return vf;
            }
        });
        this.queryExecutor = queryExecutor;
    }

    public ReactiveFederatedEvaluationStrategyImpl(ReactiveQueryExecutorImpl queryExecutor) {
        this(queryExecutor, ValueFactoryImpl.getInstance());
    }

    @Override
    public Observable<BindingSet> evaluateReactive(TupleExpr expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        if (expr instanceof SourceQuery) {
            return evaluateReactive((SourceQuery) expr, bindings);
        }
        else if (expr instanceof Join) {
            return evaluateReactive((Join) expr, bindings);
        }
        else if (expr instanceof Plan) {
            return evaluateReactive(((Plan) expr).getArg(), bindings);
        }
        else if (expr instanceof Transform) {
            return evaluateReactive((Transform) expr, bindings);
        }
        else
            return super.evaluateReactive(expr, bindings);
    }


    @Override
    public Observable<BindingSet> evaluateReactive(Join expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        if (expr instanceof BindJoin) {
            return evaluateReactive((BindJoin)expr, bindings);
        }
        else if (expr instanceof HashJoin) {
            return evaluateReactive((HashJoin)expr, bindings);
        }
        else if (expr instanceof MergeJoin) {
            return evaluateReactive((MergeJoin)expr, bindings);
        }
        else if (expr == null) {
            throw new IllegalArgumentException("expr must not be null");
        }
        else {
            throw new QueryEvaluationException("Unsupported tuple expr type: " + expr.getClass());
        }
    }

    public Observable<BindingSet> evaluateReactive(HashJoin expr, BindingSet bindings)
        throws QueryEvaluationException
    {
        Observable<BindingSet> r = evaluateReactive(expr.getRightArg(), bindings);

        Set<String> joinAttributes = expr.getLeftArg().getBindingNames();
        joinAttributes.retainAll(expr.getRightArg().getBindingNames());

        return evaluateReactive(expr.getLeftArg(), bindings)
                .toMultimap(b -> calcKey(b, joinAttributes), b1 -> b1)
                .flatMap((probe) ->
                    r.concatMap(b -> {
                        if (!probe.containsKey(calcKey(b, joinAttributes)))
                            return Observable.empty();
                        else
                            return Observable.from(probe.get(b))
                                             .join(Observable.just(b),
                                                     b1 -> Observable.never(),
                                                     b1 -> Observable.never(),
                                                     ReactiveFederatedEvaluationStrategyImpl::joinBindings);
                    })
                );
    }

    public static BindingSet joinBindings(BindingSet b1, BindingSet b2) {
        QueryBindingSet result = new QueryBindingSet();

        for (Binding b : b1) {
            if (!result.hasBinding(b.getName()))
                result.addBinding(b);
        }

        for (String name : b2.getBindingNames()) {
            Binding b = b2.getBinding(name);
            if (!result.hasBinding(name)) {
                result.addBinding(b);
            }
        }
        return result;
    }

    public static BindingSet calcKey(BindingSet bindings, Set<String> commonVars) {
        QueryBindingSet q = new QueryBindingSet();
        for (String varName : commonVars) {
            Binding b = bindings.getBinding(varName);
            if (b != null) {
                q.addBinding(b);
            }
        }
        return q;
    }

    public Observable<BindingSet> evaluateReactive(BindJoin expr, BindingSet bindings)
        throws QueryEvaluationException
    {
        return this.evaluateReactive(expr.getLeftArg(), bindings)
                .buffer(10)
                .flatMap((b) -> {
                    try {
                        return evaluateReactive(expr.getRightArg(), b);
                    } catch (Exception e) {
                        return Observable.error(e);
                    } });
    }

    public Observable<BindingSet> evaluateReactive(SourceQuery expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        //return queryExecutor.evaluateReactive(null, expr.getArg(), bindings)
        if (expr.getSources().size() == 0)
            return Observable.empty();
        else if (expr.getSources().size() == 1)
            return evaluateSourceReactive(expr.getSources().get(0), expr.getArg(), bindings);
        else {
            return Observable.from(expr.getSources())
                    .flatMap((s) -> {
                            try {
                                return evaluateSourceReactive(s, expr.getArg(), bindings);
                            } catch (QueryEvaluationException e) { return Observable.error(e); } });
        }
    }


    public Observable<BindingSet> evaluateSourceReactive(URI source, TupleExpr expr, BindingSet bindings)
        throws QueryEvaluationException
    {
        return queryExecutor.evaluateReactive(source, expr, bindings);
    }

    public Observable<BindingSet> evaluateSourceReactive(URI source, TupleExpr expr, List<BindingSet> bindings)
            throws QueryEvaluationException
    {
        return queryExecutor.evaluateReactive(source, expr, Observable.from(bindings));
    }

    public Observable<BindingSet> evaluateReactive(Transform expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return this.evaluateReactive(expr.getArg(), bindings);
    }


    public Observable<BindingSet> evaluateReactive(TupleExpr expr, List<BindingSet> bindingList)
        throws QueryEvaluationException
    {
        if (expr instanceof Plan)
            return evaluateReactive(((Plan) expr).getArg(), bindingList);
        else if (expr instanceof Union)
            return evaluateReactive((Union)expr, bindingList);
        else if (expr instanceof SourceQuery)
            return evaluateReactive((SourceQuery) expr, bindingList);
        else
            return evaluateReactiveDefault(expr, bindingList);
    }

    public Observable<BindingSet> evaluateReactive(SourceQuery expr, List<BindingSet> bindingList)
        throws QueryEvaluationException
    {
        //return queryExecutor.evaluateReactive(null, expr.getArg(), bindings)
        if (expr.getSources().size() == 0)
            return Observable.empty();
        else if (expr.getSources().size() == 1)
            return evaluateSourceReactive(expr.getSources().get(0), expr.getArg(), bindingList);
        else {
            return Observable.from(expr.getSources())
                    .flatMap((s) -> {
                        try {
                            return evaluateSourceReactive(s, expr.getArg(), bindingList);
                        } catch (QueryEvaluationException e) { return Observable.error(e); } });
        }
    }

    public Observable<BindingSet> evaluateReactiveDefault(TupleExpr expr, List<BindingSet> bindingList)
        throws QueryEvaluationException
    {
        return Observable.from(bindingList).flatMap(b -> {
            try {
                return evaluateReactive(expr, b);
            }
            catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

    public Observable<BindingSet> evaluateReactive(Union expr, List<BindingSet> bindingList)
            throws QueryEvaluationException
    {
        return Observable.just(expr.getLeftArg(), expr.getRightArg())
                .flatMap(e -> { try {
                    return evaluateReactive(e, bindingList);
                } catch (Exception x) {
                    return Observable.error(x);
                }});
    }

}
