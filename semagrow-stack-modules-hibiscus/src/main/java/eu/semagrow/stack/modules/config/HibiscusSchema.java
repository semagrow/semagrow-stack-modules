package eu.semagrow.stack.modules.config;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Created by angel on 16/6/2015.
 */
public class HibiscusSchema {


    public static final String NAMESPACE = "http://schema.hibiscus.aksw.org/";

    public static final String PREFIX = "hibiscus";

    public static Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    public static final URI SUMMARIES;

    public static final URI MODE ;

    public static final URI COMMONPREDTHREASHOLD ;


    static  {
        ValueFactory vf = ValueFactoryImpl.getInstance();
        SUMMARIES = vf.createURI(NAMESPACE, "summariesFile");
        COMMONPREDTHREASHOLD = vf.createURI(NAMESPACE, "commonPredThreshold");
        MODE = vf.createURI(NAMESPACE, "hibiscusMode");
    }
}
