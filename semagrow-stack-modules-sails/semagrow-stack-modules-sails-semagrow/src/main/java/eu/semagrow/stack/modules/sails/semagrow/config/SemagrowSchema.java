package eu.semagrow.stack.modules.sails.semagrow.config;

import org.openrdf.model.Namespace;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;


/**
 * Created by angel on 11/1/14.
 */
public class SemagrowSchema {

    public static final String NAMESPACE = "http://schema.semagrow.eu/";

    public static final String PREFIX = "semagrow";

    public static Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // queryLog handler
    // source selection
    // decomposition algorithm
    //      cost estimation
    //              cardinality estimation
    // load initial metadata ?

    static  {
        ValueFactory vf = ValueFactoryImpl.getInstance();
    }
}
