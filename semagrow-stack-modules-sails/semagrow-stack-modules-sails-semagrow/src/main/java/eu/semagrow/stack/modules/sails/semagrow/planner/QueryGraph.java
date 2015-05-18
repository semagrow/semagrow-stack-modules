package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.algebra.*;

import java.util.*;


/**
 * Created by angel on 12/5/2015.
 */
public class QueryGraph
{
    private List<QueryEdge> edges = new LinkedList<QueryEdge>();

    private List<QueryVertex> vertices = new LinkedList<QueryVertex>();

    public QueryGraph() { }

    public Collection<QueryVertex> getVertices() { return vertices; }

    public Collection<QueryEdge> getEdges() { return edges; }

    public Collection<QueryEdge> getOutgoingEdges(QueryVertex v) {
        List<QueryEdge> outgoingEdges = new LinkedList<QueryEdge>();
        for (QueryEdge e : getEdges()) {
            if (e.getFrom().equals(v))
                outgoingEdges.add(e);
        }
        return outgoingEdges;
    }

    public Collection<QueryEdge> getOutgoingEdges(Collection<QueryVertex> v)
    {
        return null;
    }

    public Collection<QueryVertex> getAdjacent(QueryVertex v) {
        Collection<QueryEdge> outgoindEdges = getOutgoingEdges(v);
        Set<QueryVertex> vertices = new HashSet<QueryVertex>();
        for (QueryEdge e : outgoindEdges) {
            vertices.add(e.getTo());
        }
        return vertices;
    }

    public Collection<QueryVertex> getAdjacent(Collection<QueryVertex> v) { return null; }

    public void addEdge(QueryVertex v1, QueryVertex v2, QueryPredicate pred)
    {
        QueryEdge e = new QueryEdge(v1,v2,pred);
        edges.add(e);
    }

    public void addEdge(Collection<QueryVertex> v1, Collection<QueryVertex> v2, QueryPredicate pred) { }

    public void addVertex(TupleExpr expr) { }

    class QueryPredicate {

    }

    class JoinPredicate { private Var joinVariable; }

    class LeftJoinPredicate { private Var joinVariable; }

    class ConditionPredicate { private ValueExpr pred; }

}
