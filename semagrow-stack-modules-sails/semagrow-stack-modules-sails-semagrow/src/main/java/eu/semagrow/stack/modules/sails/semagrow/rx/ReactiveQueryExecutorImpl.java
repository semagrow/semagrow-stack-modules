package eu.semagrow.stack.modules.sails.semagrow.rx;

import eu.semagrow.stack.modules.sails.semagrow.evaluation.QueryExecutorImpl;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import rx.Observable;

/**
 * Created by angel on 11/25/14.
 */
public class ReactiveQueryExecutorImpl extends QueryExecutorImpl {

    private final Logger logger = LoggerFactory.getLogger(ReactiveQueryExecutorImpl.class);

    private Map<URI,Repository> repoMap = new HashMap<URI,Repository>();

    private boolean rowIdOpt = false;

    public Observable<BindingSet>
        evaluateReactive(final URI endpoint, final TupleExpr expr, final BindingSet bindings)
            throws QueryEvaluationException {

        Observable<BindingSet> result = null;
        try {
            Set<String> freeVars = computeVars(expr);

            List<String> relevant = getRelevantBindingNames(bindings, freeVars);
            final BindingSet relevantBindings = filterRelevant(bindings, relevant);

            freeVars.removeAll(bindings.getBindingNames());

            if (freeVars.isEmpty()) {

                final String sparqlQuery = buildSPARQLQuery(expr, freeVars);

                /*
                result = new DelayedIteration<BindingSet, QueryEvaluationException>() {
                    @Override
                    protected Iteration<? extends BindingSet, ? extends QueryEvaluationException> createIteration()
                            throws QueryEvaluationException {
                        try {
                            boolean askAnswer = sendBooleanQuery(endpoint, sparqlQuery, relevantBindings);
                            if (askAnswer) {
                                return new SingletonIteration<BindingSet, QueryEvaluationException>(bindings);
                            } else {
                                return new EmptyIteration<BindingSet, QueryEvaluationException>();
                            }
                        } catch (QueryEvaluationException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new QueryEvaluationException(e);
                        }
                    }
                };
                */
                result = Observable.just(bindings).flatMap(b -> {
                    try {
                        if (sendBooleanQuery(endpoint, sparqlQuery, relevantBindings))
                            return Observable.just(b);
                        else
                            return Observable.empty();
                    } catch (Exception e) {
                        return Observable.error(e);
                    }
                });

                return result;
            } else {
                String sparqlQuery = buildSPARQLQuery(expr, freeVars);
                //result = sendTupleQuery(endpoint, sparqlQuery, relevantBindings);
                //result = new InsertBindingSetCursor(result, bindings);
                result = sendTupleQueryReactive(endpoint, sparqlQuery, relevantBindings)
                    .map(b -> ReactiveFederatedEvaluationStrategyImpl.joinBindings(bindings, b));
            }

            return result;

        } catch (QueryEvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new QueryEvaluationException(e);
        }
    }

    public Observable<BindingSet>
        evaluateReactive(URI endpoint, TupleExpr expr,
             Observable<BindingSet> bindingIter)
            throws QueryEvaluationException {

        //Observable<BindingSet> result = null;

        try {


            return bindingIter.buffer(15).concatMap(
                     bl ->  { try {
                         return evaluateReactiveInternal(endpoint, expr, bl);
                     } /*catch (QueryEvaluationException e) {
                         logger.debug("Failover to sequential iteration", e);
                         return bindingIter.flatMap( b -> {
                              try {
                                  return evaluateReactive(endpoint, expr, b);
                              }catch(QueryEvaluationException e2) {
                                 return Observable.error(e2);
                              }
                            });
                     }*/ catch (Exception e) {
                            return Observable.error(e);
                     } });


            /*
            return bindingIter.flatMap(b -> {
                try {
                    return evaluateReactive(endpoint, expr, b);
                } catch (QueryEvaluationException e2) {
                    return Observable.error(e2);
                }
            });
            */

        } catch (Exception e) {
            throw new QueryEvaluationException(e);
        }
    }


    protected Observable<BindingSet>
        evaluateReactiveInternal(URI endpoint, TupleExpr expr, List<BindingSet> bindings)
            throws Exception
    {

        if (bindings.size() == 1)
            return evaluateReactive(endpoint, expr, bindings.get(0));

        Observable<BindingSet> result = null;

        Set<String> exprVars = computeVars(expr);

        Set<String> relevant = new HashSet<String>(getRelevantBindingNames(bindings, exprVars));

        String sparqlQuery = buildSPARQLQueryVALUES(expr, bindings, relevant);

        result = sendTupleQueryReactive(endpoint, sparqlQuery, EmptyBindingSet.getInstance());

        if (!relevant.isEmpty()) {
            /*if (rowIdOpt)
                result = new InsertValuesBindingsIteration(result, bindings);
            else {*/

            final Observable<BindingSet> r = result;

            result = Observable.from(bindings)
                    .toMultimap(b -> ReactiveFederatedEvaluationStrategyImpl.calcKey(b, relevant), b1 -> b1)
                    .flatMap(probe ->
                            r.concatMap(b -> {
                                BindingSet k = ReactiveFederatedEvaluationStrategyImpl.calcKey(b, relevant);
                                if (!probe.containsKey(k))
                                    return Observable.empty();
                                else
                                    return Observable.from(probe.get(k))
                                            .join(Observable.just(b),
                                                    b1 -> Observable.never(),
                                                    b1 -> Observable.never(),
                                                    ReactiveFederatedEvaluationStrategyImpl::joinBindings);
                            }));

        }
        else {

            result = result.join(Observable.from(bindings),
                        (b) -> Observable.never(),
                        (b) -> Observable.never(),
                        ReactiveFederatedEvaluationStrategyImpl::joinBindings);
        }

        return result;
    }

    protected Observable<BindingSet>
        sendTupleQueryReactive(URI endpoint, String sparqlQuery, BindingSet bindings)
            throws QueryEvaluationException, MalformedQueryException, RepositoryException {

        RepositoryConnection conn = getConnection(endpoint);
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);

        for (Binding b : bindings)
            query.setBinding(b.getName(), b.getValue());

        return Observable.create(new OnSubscribeTupleResults(query));
    }

    protected boolean
        sendBooleanQueryReactive(URI endpoint, String sparqlQuery, BindingSet bindings)
            throws QueryEvaluationException, MalformedQueryException, RepositoryException {

        RepositoryConnection conn = getConnection(endpoint);
        BooleanQuery query = conn.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQuery);

        for (Binding b : bindings)
            query.setBinding(b.getName(), b.getValue());

        logger.debug("Sending to " + endpoint.stringValue() + " query " + sparqlQuery.replace('\n', ' ') + " with " + query.getBindings());
        return query.evaluate();
    }

}
