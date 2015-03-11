package eu.semagrow.stack.modules.sails.semagrow.rx;

import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.internal.operators.BufferUntilSubscriber;

import java.util.List;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Created by angel on 11/25/14.
 */
public class OnSubscribeTupleResults implements Observable.OnSubscribe<BindingSet> {

    private static final Logger logger = LoggerFactory.getLogger(OnSubscribeTupleResults.class);

    private TupleQuery query;

    final private RepositoryConnection conn;

    public OnSubscribeTupleResults(TupleQuery query, RepositoryConnection conn) {
        this.query = query;
        this.conn = conn;
    }

    @Override
    public void call(Subscriber<? super BindingSet> subscriber) {
        subscriber.setProducer(new TupleQueryResultProducer(subscriber, query, conn));
    }

    public static final class TupleQueryResultProducer implements Producer, TupleQueryResultHandler {

        private Subscriber<? super BindingSet> subscriber;

        private TupleQuery query;

        private RepositoryConnection conn;

        private volatile long requested = 0;

        @SuppressWarnings("rawtypes")
        private static final AtomicLongFieldUpdater<TupleQueryResultProducer> REQUESTED_UPDATER = AtomicLongFieldUpdater.newUpdater(TupleQueryResultProducer.class, "requested");

        public TupleQueryResultProducer(Subscriber<? super BindingSet> o, TupleQuery query, RepositoryConnection conn) {
            this.subscriber = o;
            this.query = query;
            this.conn = conn;
        }

        @Override
        public void request(long l) {

            if (REQUESTED_UPDATER.get(this) == Long.MAX_VALUE) {
                // already started with fast-path
                return;
            }

            if (subscriber.isUnsubscribed())
                return;

            REQUESTED_UPDATER.set(this, Long.MAX_VALUE);

            try {
                logger.debug("Sending query " + query.toString() + " with " + query.getBindings().toString());
                query.evaluate(this);
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }


        @Override
        public void handleBoolean(boolean b) throws QueryResultHandlerException {

        }

        @Override
        public void handleLinks(List<String> strings) throws QueryResultHandlerException {

        }

        @Override
        public void startQueryResult(List<String> strings) throws TupleQueryResultHandlerException {

        }

        @Override
        public void endQueryResult() throws TupleQueryResultHandlerException {
            if (subscriber.isUnsubscribed())
                return;

            if (conn != null) {
                try {
                    if (conn.isOpen()) {
                        conn.close();
                        logger.debug("Connection " + conn.toString() + " closed");
                    }
                } catch (RepositoryException e) {
                    logger.debug("Connection cannot be closed", e);
                }
            }


            subscriber.onCompleted();
        }

        @Override
        public void handleSolution(BindingSet bindings) throws TupleQueryResultHandlerException {
            if (subscriber.isUnsubscribed())
                return;

            logger.debug(bindings.toString());
            subscriber.onNext(bindings);
        }
    }

}
