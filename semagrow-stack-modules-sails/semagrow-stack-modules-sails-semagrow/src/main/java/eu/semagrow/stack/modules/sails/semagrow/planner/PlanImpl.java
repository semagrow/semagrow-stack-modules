package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.model.URI;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;

import java.util.*;

/**
 * Created by angel on 9/30/14.
 */
public class PlanImpl extends UnaryTupleOperator implements Plan
{
    private Map<String, Collection<URI>> schemas = new HashMap<String, Collection<URI>>();

    private PlanProperties properties;

    public PlanImpl(TupleExpr arg) { super(arg); properties = PlanProperties.defaultProperties(); }

    @Override
    public PlanProperties getProperties() { return properties; }

    @Override
    public void setProperties(PlanProperties properties) { this.properties = properties; }

    public <X extends Exception> void visit(QueryModelVisitor<X> xQueryModelVisitor) throws X {
        //getArg().visit(xQueryModelVisitor);
        xQueryModelVisitor.meetOther(this);
    }

    public String getSignature()
    {
        StringBuilder sb = new StringBuilder(128);

        sb.append(super.getSignature());
        sb.append("(cost=" + getProperties().getCost().toString() +", card=" + getProperties().getCardinality() +")");
        return sb.toString();
    }


    static public Plan createPlan(TupleExpr expr)
    {
        PlanImpl p = new PlanImpl(expr);
        updatePlanProperties(p);
        return p;
    }


    static public void updatePlanProperties(Plan plan)
    {
        TupleExpr e = plan.getArg();
        PlanProperties properties = PlanPropertiesUpdater.process(e, plan.getProperties());
        plan.setProperties(properties);
    }

}
