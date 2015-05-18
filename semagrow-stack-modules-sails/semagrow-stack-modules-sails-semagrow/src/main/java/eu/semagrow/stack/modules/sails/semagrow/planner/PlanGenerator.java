package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;

import java.util.Collection;

/**
 * Created by angel on 27/4/2015.
 */
public interface PlanGenerator<P extends Plan> {

    /**
     * Creates access plans for a given base expression
     * @param expr base expression
     * @param bindings potential bindings of variables
     * @param dataset not sure if needed
     * @return A collection of alternative execution plans
     */
    Collection<P> access(TupleExpr expr, BindingSet bindings, Dataset dataset);

    /**
     * Creates valid execution plans that is a combination of simpler plans.
     * @param p1 a collection of plans
     * @param p2 a collection of plans
     * @return A collection of plans that combine one plan from p1 and one plan from p2
     */
    Collection<P> combine(Collection<P> p1, Collection<P> p2);

    /**
     * Enhances given plans with operators in order to satisfy certain properties
     * @param plans a collection of plans
     * @param desiredProperties a set of desired properties
     * @return a collection of enhanced plans
     */
    Collection<P> finalize(Collection<P> plans, PlanProperties desiredProperties);

}
