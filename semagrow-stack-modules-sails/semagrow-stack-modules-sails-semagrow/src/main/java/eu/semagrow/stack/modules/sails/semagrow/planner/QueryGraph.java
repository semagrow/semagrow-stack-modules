package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.algebra.*;

import java.util.*;


/**
 * Created by angel on 12/5/2015.
 */
public class QueryGraph
{
    private List<QueryEdge> edges = new LinkedList<QueryEdge>();

    private List<TupleExpr> vertices = new LinkedList<TupleExpr>();

    public QueryGraph() { }

    public Collection<TupleExpr> getVertices() { return vertices; }

    public Collection<QueryEdge> getEdges() { return edges; }

    public Collection<QueryEdge> getOutgoingEdges(TupleExpr v) {
        List<QueryEdge> outgoingEdges = new LinkedList<QueryEdge>();
        for (QueryEdge e : getEdges()) {
            if (e.getFrom().equals(v))
                outgoingEdges.add(e);
        }
        return outgoingEdges;
    }

    public Collection<QueryEdge> getOutgoingEdges(Collection<TupleExpr> v)
    {
        Collection<QueryEdge> outgoingEdges = new LinkedList<>();

        for (TupleExpr e : v)
        {
            outgoingEdges.addAll(getOutgoingEdges(e));
        }
        return outgoingEdges;
    }

    public Collection<TupleExpr> getAdjacent(TupleExpr v) {
        Collection<QueryEdge> outgoindEdges = getOutgoingEdges(v);
        Set<TupleExpr> vertices = new HashSet<TupleExpr>();
        for (QueryEdge e : outgoindEdges) {
            vertices.add(e.getTo());
        }
        return vertices;
    }



    public Collection<TupleExpr> getAdjacent(Collection<TupleExpr> v) { return null; }

    public void addEdge(TupleExpr v1, TupleExpr v2, QueryPredicate pred)
    {
        QueryEdge e = new QueryEdge(v1,v2,pred);
        edges.add(e);
    }

    public void addEdge(Collection<TupleExpr> v1, Collection<TupleExpr> v2, QueryPredicate pred) { }

    public void addVertex(TupleExpr expr) { this.vertices.add(expr); }

}
