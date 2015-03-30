package eu.semagrow.stack.modules.sails.semagrow.query;

import eu.semagrow.stack.modules.api.decomposer.QueryDecompositionException;
import eu.semagrow.stack.modules.api.query.SemagrowTupleQuery;
import eu.semagrow.stack.modules.sails.semagrow.SemagrowSailRepositoryConnection;
import eu.semagrow.stack.modules.sails.semagrow.SemagrowSailConnection;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailQuery;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.repository.sail.SailTupleQuery;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by angel on 6/9/14.
 */
public class SemagrowSailTupleQuery extends SemagrowSailQuery implements SemagrowTupleQuery {

    private boolean includeProvenanceData = false;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SemagrowSailTupleQuery(ParsedTupleQuery query, SailRepositoryConnection connection) {
        super(query,connection);
    }

    public TupleQueryResult evaluate() throws QueryEvaluationException {

        TupleExpr tupleExpr = getParsedQuery().getTupleExpr();

        try {
            CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter;

            SemagrowSailConnection sailCon = (SemagrowSailConnection) getConnection().getSailConnection();

            bindingsIter = sailCon.evaluate(tupleExpr, getActiveDataset(), getBindings(),
                    getIncludeInferred(), getIncludeProvenanceData(),
                    getIncludedSources(), getExcludedSources());

            bindingsIter = enforceMaxQueryTime(bindingsIter);

            return new TupleQueryResultImpl(new ArrayList<String>(tupleExpr.getBindingNames()), bindingsIter);
        }
        catch (SailException e) {
            throw new QueryEvaluationException(e.getMessage(), e);
        }
    }

    public void evaluate(TupleQueryResultHandler handler)
            throws QueryEvaluationException, TupleQueryResultHandlerException
    {
        /*
        TupleQueryResult queryResult = evaluate();
        QueryResults.report(queryResult, handler);
        */
        logger.info("SemaGrow query evaluate with handler");
        TupleExpr tupleExpr = getParsedQuery().getTupleExpr();

        try {
            Observable<? extends BindingSet> result;

            SemagrowSailConnection sailCon = (SemagrowSailConnection) getConnection().getSailConnection();

            result = sailCon.evaluateReactive(tupleExpr, getActiveDataset(), getBindings(),
                    getIncludeInferred(), getIncludeProvenanceData());

            //result = enforceMaxQueryTime(bindingsIter);

            result.subscribe(b -> { try { handler.handleSolution(b); } catch(Exception e) { } },
                    t -> { logger.error("Evaluation error",t);
                        try {
                            handler.endQueryResult();
                        } catch (TupleQueryResultHandlerException e) {
                            e.printStackTrace();
                        }
                    },
                    () -> { try { handler.endQueryResult(); } catch (Exception e) { } });
            //return new TupleQueryResultImpl(new ArrayList<String>(tupleExpr.getBindingNames()), bindingsIter);
        }
        catch (SailException e) {
            throw new QueryEvaluationException(e.getMessage(), e);
        }
    }

    public void setIncludeProvenanceData(boolean includeProvenance) {
        includeProvenanceData = includeProvenance;
    }

    public boolean getIncludeProvenanceData() { return includeProvenanceData; }

}
