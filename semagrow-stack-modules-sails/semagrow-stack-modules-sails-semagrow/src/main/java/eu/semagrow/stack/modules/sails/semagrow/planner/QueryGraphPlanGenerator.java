package eu.semagrow.stack.modules.sails.semagrow.planner;

import eu.semagrow.stack.modules.api.estimator.CardinalityEstimator;
import eu.semagrow.stack.modules.api.source.SourceSelector;
import eu.semagrow.stack.modules.sails.semagrow.estimator.CostEstimator;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.TupleExpr;

import java.util.*;

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

        if (p1.isEmpty() || p2.isEmpty())
            return Collections.emptyList();

        Set<TupleExpr> r1 = p1.iterator().next().getProperties().getRelations();
        Set<TupleExpr> r2 = p2.iterator().next().getProperties().getRelations();

        Collection<QueryPredicate> predicates = new LinkedList<>();

        Collection<QueryEdge> edges = ctx.queryGraph.getOutgoingEdges(r1);
        for (QueryEdge e : edges) {
            if (r2.contains(e.getTo())) {
                QueryPredicate p = e.getPredicate();
                Set<TupleExpr> r = new HashSet<>(r1);
                r.addAll(r2);
                if (p.canBeApplied(r))
                    predicates.add(p);
            }
        }
        return predicates;
    }

    public Collection<Plan> combineWith(Collection<Plan> p1, Collection<Plan> p2, QueryPredicate pred)
    {
        if (pred instanceof JoinPredicate)
            return combineWith(p1,p2, (JoinPredicate)pred);
        else if (pred instanceof LeftJoinPredicate)
            return combineWith(p1,p2, (LeftJoinPredicate)pred);
        else
            return Collections.emptyList();
    }


    public Collection<Plan> combineWith(Collection<Plan> p1, Collection<Plan> p2, JoinPredicate pred)
    {
        Collection<Plan> plans =  super.combine(p1,p2);
        plans.addAll(super.combine(p2,p1));
        return plans;
    }

    public Collection<Plan> combineWith(Collection<Plan> p1, Collection<Plan> p2, LeftJoinPredicate pred)
    {
        Collection<Plan> plans =  new LinkedList<Plan>();

        for (Plan pp1 : p1) {
            for (Plan pp2 : p2) {
                if (pp1.getProperties().getSite().isRemote() &&
                        pp2.getProperties().getSite().isRemote() &&
                        pp1.getProperties().getSite().equals(pp2.getProperties().getSite())) {

                    Plan ppp = createPlan(new LeftJoin(pp1, pp2));
                    plans.add(ppp);
                    System.out.println(ppp);
                }
                plans.add(createPlan(new LeftJoin(enforceLocalSite(pp1), enforceLocalSite(pp2))));
            }
        }
        return plans;
    }
}
