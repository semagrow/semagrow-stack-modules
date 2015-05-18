package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by angel on 27/4/2015.
 */
public interface PlanOptimizer {

    /**
     * Creates a Plan that is considered as optimal
     * @param expr the expression
     * @param bindings potential bindings of variables
     * @param dataset not sure if needed
     * @return an execution plan of the expression 'expr'
     */
    Plan getBestPlan(TupleExpr expr, BindingSet bindings, Dataset dataset);

}
