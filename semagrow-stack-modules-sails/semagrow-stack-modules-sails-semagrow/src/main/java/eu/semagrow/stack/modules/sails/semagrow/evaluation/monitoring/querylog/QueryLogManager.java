package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.querylog;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;



/**
 * Created by kzam on 5/14/15.
 */
public class QueryLogManager {

    private static String logDir;
    private static String filePrefix;

    public QueryLogManager(String logDir, String filePrefix) {
        this.logDir = logDir;
        this.filePrefix = filePrefix;
    }

    public String getLastFile() throws QueryLogException {

        File[] listOfFiles = getListOfFiles();

        if(listOfFiles.length == 0) {
            String filename = logDir + filePrefix + "." + 0;

            return createFile(filename);
        }

        //Arrays.sort(listOfFiles);

        //return logDir + listOfFiles[listOfFiles.length - 1].getName();
        return logDir + getLastModified(listOfFiles);

    }

    private String getLastModified(File[] listOfFiles) {
        String filename = null;
        long max = 0;

        for(int i=0; i<listOfFiles.length; i++) {

            if(listOfFiles[i].lastModified() > max) {
                max = listOfFiles[i].lastModified();
                filename = listOfFiles[i].getName();
            }
        }
        return filename;
    }

    private String createFile(String filename)  throws QueryLogException {
        File file = new File(filename);

        try {
            if(file.createNewFile()) {
                return filename;
            }
        } catch (IOException e) {
            throw new QueryLogException(e);
        }

        throw new QueryLogException("Error in creating new qfr file");
    }

    private File[] getListOfFiles() {
        File folder = new File(logDir);

        File[] foundFiles = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(filePrefix);
            }
        });

        return foundFiles;
    }

}
