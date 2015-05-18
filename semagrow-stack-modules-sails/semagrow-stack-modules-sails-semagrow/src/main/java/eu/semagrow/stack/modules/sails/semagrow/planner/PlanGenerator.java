package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;

import java.util.Collection;

/**
 * Created by angel on 27/4/2015.
 */
public interface PlanGenerator<P extends Plan> {

    Collection<P> accessPlans(TupleExpr expr, BindingSet bindings, Dataset dataset);

    Collection<P> joinPlans(Collection<P> p1, Collection<P> p2);

    Collection<P> finalizePlans(Collection<P> plans, PlanProperties desiredProperties);

}
