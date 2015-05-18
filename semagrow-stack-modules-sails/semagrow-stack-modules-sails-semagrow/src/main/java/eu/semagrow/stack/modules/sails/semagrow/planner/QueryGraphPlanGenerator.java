package eu.semagrow.stack.modules.sails.semagrow.planner;

import eu.semagrow.stack.modules.api.estimator.CardinalityEstimator;
import eu.semagrow.stack.modules.api.source.SourceSelector;
import eu.semagrow.stack.modules.sails.semagrow.estimator.CostEstimator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by angel on 18/5/2015.
 */
public class QueryGraphPlanGenerator extends PlanGeneratorImpl {

    public QueryGraphPlanGenerator(DecomposerContext ctx,
                                   SourceSelector selector,
                                   CostEstimator costEstimator,
                                   CardinalityEstimator cardinalityEstimator)
    {
        super(ctx, selector, costEstimator, cardinalityEstimator);
    }

    @Override
    public Collection<Plan> combine(Collection<Plan> p1, Collection<Plan> p2)
    {
        Collection<QueryPredicate> preds = getValidPredicatesFor(p1, p2);

        Collection<Plan> plans = new LinkedList<Plan>();

        for (QueryPredicate p : preds) {
            plans.addAll(combineWith(p1, p2, p));
        }

        return plans;
    }

    private Collection<QueryPredicate> getValidPredicatesFor(Collection<Plan> p1, Collection<Plan> p2) {
        return null;
    }

    public Collection<Plan> combineWith(Collection<Plan> p1, Collection<Plan> p2, QueryPredicate pred)
    {
        //getGenerators(pred);
        return Collections.emptyList();
    }

}
