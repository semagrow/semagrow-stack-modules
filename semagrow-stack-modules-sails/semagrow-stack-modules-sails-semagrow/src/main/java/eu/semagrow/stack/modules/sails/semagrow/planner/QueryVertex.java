package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by angel on 18/5/2015.
 */
class QueryVertex {

    private TupleExpr load;

    public QueryVertex(TupleExpr load) {
        this.load = load;
    }

    public TupleExpr getPayload() {
        return load;
    }

}
