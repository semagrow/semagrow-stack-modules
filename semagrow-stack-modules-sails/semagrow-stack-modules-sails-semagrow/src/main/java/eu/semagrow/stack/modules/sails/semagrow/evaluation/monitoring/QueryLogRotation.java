package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring;

import eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.querylog.QueryLogManager;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by kzam on 5/14/15.
 */
public class QueryLogRotation {

    private final static int N = 2;
    private QueryLogManager manager;
    static final org.slf4j.Logger logger = LoggerFactory.getLogger(QueryLogRotation.class);


    public QueryLogRotation() {
        this.manager = new QueryLogManager();
    }

    public File checkFileChange() {

        try {
            // it is time to change the log file
            if(getCurrentDate() - manager.getCreationTime() > convertDayInMillsecs()) {
                logger.info("create new file");
                return manager.createNewQfrFile();
            } else {
                logger.info("get last file");
                return manager.getCurrentFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private long convertDayInMillsecs() {
        return N * 24 * 60 * 60 * 1000;
    }

    private static long getCurrentDate() {
        Calendar cal = Calendar.getInstance();

        return cal.getTimeInMillis();
    }
}
