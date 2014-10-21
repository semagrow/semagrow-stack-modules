package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.qfr;

import java.io.OutputStream;

/**
 * Created by angel on 10/21/14.
 */
public interface QueryRecordLogFactory {

    QueryRecordLogHandler getQueryRecordLogger(OutputStream out);

}
