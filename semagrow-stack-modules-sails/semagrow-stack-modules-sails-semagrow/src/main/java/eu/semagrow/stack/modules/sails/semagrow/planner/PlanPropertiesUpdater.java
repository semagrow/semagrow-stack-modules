package eu.semagrow.stack.modules.sails.semagrow.planner;

import eu.semagrow.stack.modules.sails.semagrow.algebra.BindJoin;
import eu.semagrow.stack.modules.sails.semagrow.algebra.HashJoin;
import eu.semagrow.stack.modules.sails.semagrow.algebra.SourceQuery;
import org.openrdf.query.algebra.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by angel on 21/4/2015.
 */
public class PlanPropertiesUpdater extends PlanVisitorBase<RuntimeException> {

    private PlanProperties properties = PlanProperties.defaultProperties();

    static public PlanProperties process(TupleExpr expr) {
        return process(expr, PlanProperties.defaultProperties());
    }

    static public PlanProperties process(TupleExpr expr, PlanProperties initialProperties) {

        PlanPropertiesUpdater updater  = new PlanPropertiesUpdater();
        updater.properties = initialProperties;

        expr.visit(updater);

        if (updater.properties != null)
            return updater.properties;
        else
            return initialProperties;
    }

    @Override
    public void meet(Order order) throws RuntimeException  {
        Ordering o = new Ordering(order.getElements());
        order.getArg().visit(this);
        properties.setOrdering(o);
    }

    @Override
    public void meet(Group group) throws RuntimeException {
        group.getArg().visit(this);

        // compute ordering
    }

    @Override
    public void meet(Distinct distinct) throws RuntimeException {
        distinct.getArg().visit(this);
    }

    @Override
    public void meet(Projection projection) throws RuntimeException {
        projection.getArg().visit(this);

        //compute ordering
        Ordering o = this.properties.getOrdering();
        Set<String> sourceNames = new HashSet<String>();

        for (ProjectionElem elem: projection.getProjectionElemList().getElements()) {
            sourceNames.add(elem.getSourceName());
        }

        Ordering o2 = o.filter(sourceNames);
        this.properties.setOrdering(o2);
    }

    @Override
    public void meet(Filter filter) throws RuntimeException  {
        filter.getArg().visit(this);
    }

    @Override
    public void meet(BindJoin join) throws RuntimeException  {
        join.getLeftArg().visit(this);
        PlanProperties leftProperties = this.properties;
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
        join.getLeftArg().visit(this);
    }

    @Override
    public void meet(SourceQuery query) throws RuntimeException  {
        query.getArg().visit(this);
        this.properties.setSite(Site.LOCAL);
    }

    @Override
    public void meet(Union union) throws RuntimeException {
        union.getLeftArg().visit(this);
        union.getRightArg().visit(this);
    }

    @Override
    public void meet(Plan plan) throws RuntimeException {
        this.properties = plan.getProperties().clone();
    }

}