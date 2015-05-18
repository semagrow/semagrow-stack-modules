package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by angel on 17/5/2015.
 */
public class QueryGraphBuilder extends QueryModelVisitorBase<RuntimeException> {

    private HashMap<String, Set<StatementPattern>> varMap = new HashMap<>();

    private QueryGraph graph;

    private static Logger logger = LoggerFactory.getLogger(QueryGraphBuilder.class);

    private QueryGraphBuilder() {
        graph = new QueryGraph();
    }

    @Override
    public void meet(Join join)
    {
        Set<String> joinVars = commonVarNames(join.getLeftArg(), join.getRightArg());

        HashMap<String, Set<StatementPattern>> left = new HashMap<>();
        HashMap<String, Set<StatementPattern>> right = new HashMap<>();

        join.getLeftArg().visit(this);

        for (String v : joinVars)
            left.put(v, new HashSet<StatementPattern>(varMap.get(v)));

        join.getRightArg().visit(this);

        for (String v : joinVars) {
            Set<StatementPattern> s = new HashSet<StatementPattern>(varMap.get(v));
            s.removeAll(left.get(v));
            right.put(v, s);
        }

        for (String v : joinVars) {
            for (StatementPattern p1 : left.get(v)) {
                for (StatementPattern p2 : right.get(v)) {
                    //graph.addEdge(p1, p2, JoinPredicate(v))
                    logger.debug("Adding INNER JOIN edge from " + p1 +  " to " + p2);
                }
            }
        }
    }

    @Override
    public void meet(Filter filter) {
        filter.getArg().visit(this);
    }

    private Set<String> commonVarNames(TupleExpr leftArg, TupleExpr rightArg) {
        Set<String> leftVars = leftArg.getBindingNames();
        Set<String> rightVars = rightArg.getBindingNames();
        leftVars.retainAll(rightVars);
        return leftVars;
    }

    @Override
    public void meet(StatementPattern pattern) {

        Set<String> varNames = pattern.getBindingNames();

        for (String varName : varNames) {
            Set<StatementPattern> s = varMap.getOrDefault(varName, new HashSet<StatementPattern>());
            s.add(pattern);
            varMap.put(varName, s);
        }

    }

    @Override
    public void meet(LeftJoin leftJoin) {
        Set<String> joinVars = commonVarNames(leftJoin.getLeftArg(), leftJoin.getRightArg());

        HashMap<String, Set<StatementPattern>> left = new HashMap<>();
        HashMap<String, Set<StatementPattern>> right = new HashMap<>();

        leftJoin.getLeftArg().visit(this);

        for (String v : joinVars)
            left.put(v, new HashSet<StatementPattern>(varMap.get(v)));

        leftJoin.getRightArg().visit(this);

        for (String v : joinVars) {
            Set<StatementPattern> s = new HashSet<StatementPattern>(varMap.get(v));
            s.removeAll(left.get(v));
            right.put(v, s);
        }

        for (String v : joinVars) {
            for (StatementPattern p1 : left.get(v)) {
                for (StatementPattern p2 : right.get(v)) {
                    //graph.addEdge(p1, p2, LeftJoinPredicate(v))
                    logger.debug("Adding LEFT  JOIN edge from " + p1 +  " to " + p2);
                }
            }
        }
    }


    public static QueryGraph build(TupleExpr expr) {
        QueryGraphBuilder builder = new QueryGraphBuilder();
        expr.visit(builder);
        return builder.graph;
    }

}
