package eu.semagrow.stack.modules.sails.semagrow.rx;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import rx.Observable;
import rx.Subscriber;
import rx.internal.operators.BufferUntilSubscriber;

import java.util.List;

/**
 * Created by angel on 11/25/14.
 */
public class OnSubscribeTupleResults implements Observable.OnSubscribe<BindingSet>, TupleQueryResultHandler {

    private Subscriber<? super BindingSet> subscriber;

    private BufferUntilSubscriber<BindingSet> bufferSubject = BufferUntilSubscriber.<BindingSet>create();

    @Override
    public void call(Subscriber<? super BindingSet> subscriber) {
        bufferSubject.subscribe(subscriber);
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
        bufferSubject.onCompleted();
    }

    @Override
    public void handleSolution(BindingSet bindings) throws TupleQueryResultHandlerException {
        bufferSubject.onNext(bindings);
    }
}
