package eu.semagrow.stack.modules.api.config;

/**
 * Created by angel on 11/1/14.
 */
public class SourceSelectorConfigException extends Exception {

    public SourceSelectorConfigException() { super(); }

    public SourceSelectorConfigException(Exception e) {
        super(e);
    }

    public SourceSelectorConfigException(String msg) {
        super(msg);
    }
}
