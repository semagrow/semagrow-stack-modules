package eu.semagrow.stack.modules.sails.semagrow.planner;

/**
 * Created by angel on 18/5/2015.
 */
class QueryEdge {

    private QueryVertex from;

    private QueryVertex to;

    /*
    private Collection<QueryVertex> from;

    private Collection<QueryVertex> to;
    */


    private QueryGraph.QueryPredicate predicate;

    public QueryEdge(QueryVertex from, QueryVertex to, QueryGraph.QueryPredicate predicate) {
        this.from = from;
        this.to = to;
        this.predicate = predicate;
    }

    public QueryVertex getFrom() {
        return from;
    }

    public QueryVertex getTo() {
        return to;
    }

    public QueryGraph.QueryPredicate getPredicate() {
        return predicate;
    }
}
