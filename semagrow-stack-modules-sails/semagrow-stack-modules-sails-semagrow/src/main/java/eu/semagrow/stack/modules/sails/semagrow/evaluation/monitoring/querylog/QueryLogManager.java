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

    private final static String baseDir = "/var/tmp/log/";
    private final static String filePrefix = "qfr";
    private final static String fileSuffix = ".log";

    public File createNewQfrFile() throws IOException {
        long ext = getCounter(getLastFile());

        return returnFile(filePrefix + ++ext + fileSuffix, true);
    }

    private File returnFile(String filename, boolean newFile) throws IOException {
        File file = new File(baseDir + filename);

        if(newFile) {
            if(! file.createNewFile()) {
                return null;
            }
        }

        return file;
    }

    public File getCurrentFile() throws IOException {
        return returnFile(getLastFile(), false);
    }

    private String getLastFile() {

        File[] listOfFiles = getListOfFiles();

        if(listOfFiles.length == 0) {
            return null;
        }

        Arrays.sort(listOfFiles);

        return listOfFiles[listOfFiles.length - 1].getName();
    }

    public String[] getQfrFileNames() {
        File folder = new File(baseDir);

        String[] foundFiles = folder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(filePrefix);
            }
        });

        foundFiles = (String[]) ArrayUtils.removeElement(foundFiles, foundFiles[foundFiles.length - 1]);

        return foundFiles;
    }

    private File[] getListOfFiles() {
        File folder = new File(baseDir);

        File[] foundFiles = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(filePrefix);
            }
        });

        return foundFiles;
    }

    private long getCounter(String fileName) {
        if(fileName != null) {
            String[] ss = fileName.split(filePrefix);

            long counter = Long.parseLong(ss[1].substring(0, ss[1].indexOf(fileSuffix)));

            return counter;
        } else {
            return 0;
        }
    }


    /**
     * get Creation time of the last file
     * @return date in milliseconds
     * @throws java.io.IOException
     */
    public long getCreationTime() throws IOException {
        String file = getLastFile();

        if(file != null) {
            Path path = Paths.get(baseDir + file);
            BasicFileAttributes attributes =
                    Files.readAttributes(path, BasicFileAttributes.class);
            FileTime creationTime = attributes.creationTime();

            return creationTime.toMillis();
        } else {
            // default unix timestamp
            return System.currentTimeMillis() / 1000L;
        }
    }

}
