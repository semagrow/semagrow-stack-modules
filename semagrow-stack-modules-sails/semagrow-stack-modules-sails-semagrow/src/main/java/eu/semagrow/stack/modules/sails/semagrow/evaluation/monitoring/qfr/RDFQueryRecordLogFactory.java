package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.qfr;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

import java.io.OutputStream;

/**
 * Created by angel on 10/21/14.
 */
public class RDFQueryRecordLogFactory implements QueryRecordLogFactory {

    private RDFWriterFactory writerFactory;

    public RDFQueryRecordLogFactory(RDFWriterFactory writerFactory) {
        this.writerFactory = writerFactory;
    }

    @Override
    public QueryRecordLogHandler getQueryRecordLogger(OutputStream out) {

        RDFWriter writer = writerFactory.getWriter(out);

        QueryRecordLogHandler handler = new RDFQueryRecordLogHandler(writer, ValueFactoryImpl.getInstance());
        try {
            handler.startQueryLog();
        } catch (QueryRecordLogException e) {
            e.printStackTrace();
        }
        return handler;
    }
}
