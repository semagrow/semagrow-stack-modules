package eu.semagrow.stack.modules.sails.semagrow.evaluation.file;

import org.openrdf.http.client.QueueCursor;
import org.openrdf.query.*;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultParser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Provides concurrent access to tuple results as they are being parsed.
 *
 * @author James Leigh
 */
public class BackgroundTupleResult extends TupleQueryResultImpl implements Runnable, TupleQueryResultHandler {

    private volatile boolean closed;

    private TupleQueryResultParser parser;

    private InputStream in;

    private org.openrdf.http.client.QueueCursor<BindingSet> queue;

    private List<String> bindingNames;

    private CountDownLatch bindingNamesReady = new CountDownLatch(1);

    public BackgroundTupleResult(TupleQueryResultParser parser, InputStream in) {
        this(new org.openrdf.http.client.QueueCursor<BindingSet>(10), parser, in);
    }

    public BackgroundTupleResult(QueueCursor<BindingSet> queue, TupleQueryResultParser parser, InputStream in)
    {
        super(Collections.<String>emptyList(), queue);
        this.queue = queue;
        this.parser = parser;
        this.in = in;
    }

    @Override
    protected synchronized void handleClose()
            throws QueryEvaluationException
    {
        try {
            try {
                closed = true;
                super.handleClose();
            } finally {
                in.close();
            }
        }
        catch (IOException e) {
            throw new QueryEvaluationException(e);
        }
    }

    @Override
    public List<String> getBindingNames() {
        try {
            bindingNamesReady.await();
            queue.checkException();
            return bindingNames;
        }
        catch (InterruptedException e) {
            throw new UndeclaredThrowableException(e);
        }
        catch (QueryEvaluationException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Override
    public void run() {
        try {
            parser.setQueryResultHandler(this);
            parser.parseQueryResult(in);
        }
        catch (QueryResultHandlerException e) {
            // parsing cancelled or interrupted
        }
        catch (QueryResultParseException e) {
            queue.toss(e);
        }
        catch (IOException e) {
            queue.toss(e);
        }
        finally {
            queue.done();
            bindingNamesReady.countDown();
        }
    }

    @Override
    public void startQueryResult(List<String> bindingNames)
            throws TupleQueryResultHandlerException
    {
        this.bindingNames = bindingNames;
        bindingNamesReady.countDown();
    }

    @Override
    public void handleSolution(BindingSet bindingSet)
            throws TupleQueryResultHandlerException
    {
        try {
            queue.put(bindingSet);
        }
        catch (InterruptedException e) {
            throw new TupleQueryResultHandlerException(e);
        }
        if (closed)
            throw new TupleQueryResultHandlerException("Result closed");
    }

    @Override
    public void endQueryResult()
            throws TupleQueryResultHandlerException
    {
        // no-op
    }

    @Override
    public void handleBoolean(boolean value)
            throws QueryResultHandlerException
    {
        throw new UnsupportedOperationException("Cannot handle boolean results");
    }

    @Override
    public void handleLinks(List<String> linkUrls)
            throws QueryResultHandlerException
    {
        // ignore
    }
}

