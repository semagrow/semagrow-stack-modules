package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by angel on 18/5/2015.
 */
class QueryEdge {

    private TupleExpr from;

    private TupleExpr to;

    /*
    private Collection<QueryVertex> from;

    private Collection<QueryVertex> to;
    */


    private QueryPredicate predicate;

    public QueryEdge(TupleExpr from, TupleExpr to, QueryPredicate predicate) {
        this.from = from;
        this.to = to;
        this.predicate = predicate;
    }

    public TupleExpr getFrom() {
        return from;
    }

    public TupleExpr getTo() {
        return to;
    }

    public QueryPredicate getPredicate() {
        return predicate;
    }
}
