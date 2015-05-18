package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by angel on 18/5/2015.
 */
public interface Plan extends QueryModelNode, TupleExpr {

    PlanProperties getProperties();

    void setProperties(PlanProperties properties);
}
