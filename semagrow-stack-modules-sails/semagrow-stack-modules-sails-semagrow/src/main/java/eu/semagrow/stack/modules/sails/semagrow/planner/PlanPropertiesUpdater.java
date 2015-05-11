package eu.semagrow.stack.modules.sails.semagrow.planner;

import eu.semagrow.stack.modules.sails.semagrow.algebra.BindJoin;
import eu.semagrow.stack.modules.sails.semagrow.algebra.HashJoin;
import eu.semagrow.stack.modules.sails.semagrow.algebra.SourceQuery;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by angel on 21/4/2015.
 */
public class PlanPropertiesUpdater extends PlanVisitorBase<RuntimeException> {

    private PlanProperties properties;

    static public PlanProperties process(TupleExpr expr) {
        PlanPropertiesUpdater updater  = new PlanPropertiesUpdater();
        expr.visit(updater);
        return updater.properties;
    }

    @Override
    public void meet(Order order) throws RuntimeException  {
        Ordering o = new Ordering(order.getElements());
        order.getArg().visit(this);
        properties.setOrdering(o);
    }

    @Override
    public void meet(Filter filter) throws RuntimeException  {
        filter.getArg().visit(this);
    }

    @Override
    public void meet(BindJoin join) throws RuntimeException  {

    }

    @Override
    public void meet(HashJoin join) throws RuntimeException  {

        join.getLeftArg().visit(this);
        PlanProperties leftProperties = this.properties;

        join.getRightArg().visit(this);

        //this.properties;
    }

    @Override
    public void meet(Join join) throws RuntimeException {
        join.getRightArg().visit(this);
    }

    @Override
    public void meet(SourceQuery query) throws RuntimeException  {
        query.getArg().visit(this);
        this.properties.setSite(Site.LOCAL);
    }

    @Override
    public void meet(Plan plan) throws RuntimeException {
        this.properties = plan.getProperties();
    }

}