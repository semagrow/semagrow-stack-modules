package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.querylog;

/**
 * Created by kzam on 5/18/15.
 */
public class QueryLogConfigException extends Exception {

    public QueryLogConfigException(Exception e) {
        super(e);
    }

    public QueryLogConfigException(String msg) {
        super(msg);
    }
}
