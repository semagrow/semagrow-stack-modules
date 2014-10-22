package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.querylog;

/**
 * Created by angel on 10/20/14.
 */
public interface QueryLogHandler {

    void startQueryLog() throws QueryLogException;

    void handleQueryRecord(QueryLogRecord queryLogRecord) throws QueryLogException;

    void endQueryLog() throws QueryLogException;
}
