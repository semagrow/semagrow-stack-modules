package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;

import java.util.Collection;

/**
 * Created by angel on 18/5/2015.
 */
public class QueryGraphPlanGeneratorImpl implements PlanGenerator<Plan> {



    public Collection<Plan> accessPlans(TupleExpr expr, BindingSet bindings, Dataset dataset) {
        return null;
    }


    public Collection<Plan> joinPlans(Collection<Plan> p1, Collection<Plan> p2) {
        return null;
    }


    public Collection<Plan> finalizePlans(Collection<Plan> plans, PlanProperties desiredProperties) {
        return null;
    }
}
