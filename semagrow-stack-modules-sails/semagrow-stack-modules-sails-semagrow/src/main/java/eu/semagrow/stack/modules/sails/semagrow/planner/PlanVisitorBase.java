package eu.semagrow.stack.modules.sails.semagrow.planner;

import eu.semagrow.stack.modules.sails.semagrow.algebra.BindJoin;
import eu.semagrow.stack.modules.sails.semagrow.algebra.HashJoin;
import eu.semagrow.stack.modules.sails.semagrow.algebra.SourceQuery;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Created by angel on 21/4/2015.
 */
public class PlanVisitorBase<X extends Exception> extends QueryModelVisitorBase<X> {


    public PlanVisitorBase() {

    }

    public void meet(Plan plan) throws X {
        meetPlan(plan);
    }

    public void meet(HashJoin join) throws X {
        meet((Join) join);
    }

    public void meet(BindJoin join) throws X {
        meet((Join) join);
    }

    public void meet(SourceQuery query) throws X {
        meetNode(query);
    }

    public void meetPlan(Plan plan) throws X {
        meetNode(plan);
    }

    @Override
    public void meetOther(QueryModelNode node) throws X {
        if (node instanceof Plan)
            meetPlan((Plan)node);

        super.meetOther(node);
    }


}
