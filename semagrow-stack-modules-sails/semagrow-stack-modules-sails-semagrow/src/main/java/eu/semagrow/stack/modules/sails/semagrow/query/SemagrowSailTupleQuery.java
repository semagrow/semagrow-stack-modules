package eu.semagrow.stack.modules.sails.semagrow.query;

import eu.semagrow.stack.modules.api.decomposer.QueryDecompositionException;
import eu.semagrow.stack.modules.api.query.SemagrowTupleQuery;
import eu.semagrow.stack.modules.sails.semagrow.SemagrowSailRepositoryConnection;
import eu.semagrow.stack.modules.sails.semagrow.SemagrowSailConnection;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailQuery;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.repository.sail.SailTupleQuery;
import org.openrdf.sail.SailException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by angel on 6/9/14.
 */
public class SemagrowSailTupleQuery extends SemagrowSailQuery implements SemagrowTupleQuery {

    private boolean includeProvenanceData = false;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SemagrowSailTupleQuery(ParsedTupleQuery query, SailRepositoryConnection connection) {
        super(query,connection);
    }

    public TupleQueryResult evaluate() throws QueryEvaluationException {

        TupleExpr tupleExpr = getParsedQuery().getTupleExpr();

        try {
            CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter;

            SemagrowSailConnection sailCon = (SemagrowSailConnection) getConnection().getSailConnection();

            bindingsIter = sailCon.evaluate(tupleExpr, getActiveDataset(), getBindings(),
                    getIncludeInferred(), getIncludeProvenanceData(),
                    getIncludedSources(), getExcludedSources());

            bindingsIter = enforceMaxQueryTime(bindingsIter);

            return new TupleQueryResultImpl(new ArrayList<String>(tupleExpr.getBindingNames()), bindingsIter);
        }
        catch (SailException e) {
            throw new QueryEvaluationException(e.getMessage(), e);
        }
    }

    public void evaluate(TupleQueryResultHandler handler)
            throws QueryEvaluationException, TupleQueryResultHandlerException
    {

        TupleQueryResult queryResult = evaluate();
        QueryResults.report(queryResult, handler);

        /*
        logger.info("SemaGrow query evaluate with handler");
        TupleExpr tupleExpr = getParsedQuery().getTupleExpr();

        try {
            Publisher<? extends BindingSet> result;

            SemagrowSailConnection sailCon = (SemagrowSailConnection) getConnection().getSailConnection();

            result = sailCon.evaluateReactive(tupleExpr, getActiveDataset(), getBindings(),
                    getIncludeInferred(), getIncludeProvenanceData(), getIncludedSources(), getExcludedSources());

            //result = enforceMaxQueryTime(bindingsIter);

            CountDownLatch latch = new CountDownLatch(1);

            HandlerSubscriberAdapter subscriberAdapter = toSubscriber(handler, latch);

            result.subscribe(subscriberAdapter);

            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.error("awaiting a latch", e);
            }

            if (subscriberAdapter.errorOccured) {
                if (subscriberAdapter.errorThrown instanceof QueryEvaluationException)
                    throw (QueryEvaluationException) subscriberAdapter.errorThrown;
                else
                    throw new QueryEvaluationException(subscriberAdapter.errorThrown);
            }
        }
        catch (SailException e) {
            throw new QueryEvaluationException(e.getMessage(), e);
        }
        */
    }

    public void setIncludeProvenanceData(boolean includeProvenance) {
        includeProvenanceData = includeProvenance;
    }

    public boolean getIncludeProvenanceData() { return includeProvenanceData; }

    private HandlerSubscriberAdapter toSubscriber(TupleQueryResultHandler handler, CountDownLatch latch) {
        return new HandlerSubscriberAdapter(handler, latch);
    }


    private class HandlerSubscriberAdapter implements Subscriber<BindingSet>
    {
        private TupleQueryResultHandler handler;

        private boolean isStarted = false;

        private boolean errorOccured = false;

        private Throwable errorThrown = null;

        private CountDownLatch latch;

        public HandlerSubscriberAdapter(TupleQueryResultHandler handler, CountDownLatch latch) {
            this.handler = handler;
            this.latch = latch;
        }

        public void onSubscribe(Subscription subscription) {
            // FIXME: is it possible that tuplequeryhandler be slower than producer?
            subscription.request(Long.MAX_VALUE);
        }

        public void onNext(BindingSet bindings) {
            try {
                if (!isStarted) {
                    handler.startQueryResult(new ArrayList<>(bindings.getBindingNames()));
                    isStarted = true;
                }

                handler.handleSolution(bindings);
            } catch (TupleQueryResultHandlerException e) {
                logger.error("Tuple handle solution error", e);
            }
        }


        public void onError(Throwable throwable) {
            errorOccured = true;
            errorThrown = throwable;

            logger.error("Evaluation error", throwable);
            latch.countDown();

            if (isStarted) {
                try {
                    handler.endQueryResult();
                } catch (TupleQueryResultHandlerException e) {
                    logger.error("Tuple handle solution error", e);
                }
            } else {
                try {
                    handler.startQueryResult(Collections.emptyList());

                    handler.endQueryResult();
                } catch (TupleQueryResultHandlerException e) {
                    logger.error("Tuple handle solution error", e);
                }
            }

        }


        public void onComplete() {
            latch.countDown();
            if (isStarted) {
                try {
                    handler.endQueryResult();
                } catch (TupleQueryResultHandlerException e) {
                    logger.error("Tuple handle solution error", e);
                }
            } else {
                try {
                    handler.startQueryResult(Collections.emptyList());
                    handler.endQueryResult();
                } catch (TupleQueryResultHandlerException e) {
                    logger.error("Tuple handle solution error", e);
                }
            }
        }
    }
}
