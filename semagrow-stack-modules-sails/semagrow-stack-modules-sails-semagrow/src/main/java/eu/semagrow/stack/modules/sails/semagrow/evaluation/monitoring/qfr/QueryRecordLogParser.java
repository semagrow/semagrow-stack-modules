package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.qfr;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by angel on 10/21/14.
 */
public interface QueryRecordLogParser {

    void setQueryRecordHandler(QueryLogHandler handler);

    void parseQueryLog(InputStream in) throws IOException, QueryLogException;

}
