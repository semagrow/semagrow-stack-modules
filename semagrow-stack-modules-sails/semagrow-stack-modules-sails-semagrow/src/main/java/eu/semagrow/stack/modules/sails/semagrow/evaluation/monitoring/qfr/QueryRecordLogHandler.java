package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.qfr;

/**
 * Created by angel on 10/20/14.
 */
public interface QueryRecordLogHandler {

    void startQueryLog() throws QueryRecordLogException;

    void handleQueryRecord(QueryRecord queryRecord) throws QueryRecordLogException;

    void endQueryLog() throws QueryRecordLogException;
}
