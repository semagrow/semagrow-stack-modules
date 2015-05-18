package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.algebra.TupleExpr;

import java.util.*;

/**
 * Created by angel on 9/30/14.
 */
public class PlanCollection<P extends Plan> {

    private Map<Set<TupleExpr>, Collection<P>> planMap;

    public PlanCollection() {
        planMap = new HashMap<Set<TupleExpr>, Collection<P>>();
    }

    public Set<TupleExpr> getExpressions() {
        Set<TupleExpr> allExpressions = new HashSet<TupleExpr>();
        for (Set<TupleExpr> s : planMap.keySet() )
            allExpressions.addAll(s);
        return allExpressions;
    }

    public void addPlan(Set<TupleExpr> e, Collection<P> plans) {
        if (planMap.containsKey(e))
            planMap.get(e).addAll(plans);
        else
            planMap.put(e, plans);
    }

    public Collection<P> get(Set<TupleExpr> set) {
        if (!planMap.containsKey(set)) {
            Collection<P> emptyPlans = new LinkedList<P>();
            planMap.put(set, emptyPlans);
        }

        return planMap.get(set);
    }

}
